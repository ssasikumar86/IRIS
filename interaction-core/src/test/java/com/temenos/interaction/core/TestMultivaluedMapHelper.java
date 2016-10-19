package com.temenos.interaction.core;

/*
 * #%L
 * interaction-core
 * %%
 * Copyright (C) 2012 - 2016 Temenos Holdings N.V.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.junit.Before;
import org.junit.Test;

import com.temenos.interaction.core.MultivaluedMapHelper.Strategy;

/**
 * Unit tests for class MultivaluedMapHelper.
 *
 * @author dgroves
 *
 */
public class TestMultivaluedMapHelper {
    
    private MultivaluedMap<String, String> src, dest;
    
    @Before
    public void setUp(){
        src = new MultivaluedMapImpl<String>();
        dest = new MultivaluedMapImpl<String>();
    }
    
    @Test
    public void testMergeWithNoDuplicateKeysOrValues(){
        //given {source map contains a single unique entry}
        src.put("alpha", createListOfValues("1", "2", "3"));
        
        //and {destination map contains two entries}
        dest.put("beta", createListOfValues("4", "5", "6"));
        dest.put("gamma", createListOfValues("7", "8", "9"));
        
        //when {the maps are merged}
        MultivaluedMapHelper.merge(src, dest, Strategy.UNION);
        
        //then {destination map must contain unique entry from source map}
        assertThat(dest.get("alpha"), contains("1", "2", "3"));
        assertThat(dest.get("beta"), contains("4", "5", "6"));
        assertThat(dest.get("gamma"), contains("7", "8", "9"));
        assertThat(dest.size(), equalTo(3));
        
        //and {source values must not be altered}
        assertThat(src.get("alpha"), contains("1", "2", "3"));
        assertThat(src.size(), equalTo(1));
    }
    
    @Test
    public void testMergeMultipleEntriesWithNoDuplicateKeysOrValues(){
        //given {source map contains two unique entries}
        src.put("alpha", createListOfValues("1", "2", "3"));
        src.put("delta", createListOfValues("10", "11", "12"));
        
        //and {destination map contains two entries}
        dest.put("beta", createListOfValues("4", "5", "6"));
        dest.put("gamma", createListOfValues("7", "8", "9"));
        
        //when {the maps are merged}
        MultivaluedMapHelper.merge(src, dest, Strategy.UNION);
        
        //then {destination map must contain unique entries from source map}
        assertThat(dest.get("alpha"), contains("1", "2", "3"));
        assertThat(dest.get("beta"), contains("4", "5", "6"));
        assertThat(dest.get("gamma"), contains("7", "8", "9"));
        assertThat(dest.get("delta"), contains("10", "11", "12"));
        assertThat(dest.size(), equalTo(4));
        
        //and {source values must not be altered}
        assertThat(src.get("alpha"), contains("1", "2", "3"));
        assertThat(src.get("delta"), contains("10", "11", "12"));
        assertThat(src.size(), equalTo(2));
    }
    
    @Test
    public void testMergeWithDuplicateKeysStrategyUnion(){
        //given {source map contains one entry}
        src.put("alpha", createListOfValues("1", "2", "3"));
        
        //and {destination map contains a duplicate key and two other entries}
        dest.put("alpha", createListOfValues("4"));
        dest.put("beta", createListOfValues("4", "5", "6"));
        dest.put("gamma", createListOfValues("7", "8", "9"));
        
        //when {the maps are merged}
        MultivaluedMapHelper.merge(src, dest, Strategy.UNION);
        
        //then {values from matching source map entry 
        //must be appended to the destination map entry}
        assertThat(dest.get("alpha"), contains("4", "1", "2", "3"));
        assertThat(dest.get("beta"), contains("4", "5", "6"));
        assertThat(dest.get("gamma"), contains("7", "8", "9"));
        assertThat(dest.size(), equalTo(3));
        
        //and {source values must not be altered}
        assertThat(src.get("alpha"), contains("1", "2", "3"));
        assertThat(src.size(), equalTo(1));
    }
    
    @Test
    public void testMergeWithDuplicateKeysFavourSrc(){
        //given {source map contains one entry}
        src.put("alpha", createListOfValues("1", "2", "3"));
        
        //and {destination map contains a duplicate key and two other entries}
        dest.put("alpha", createListOfValues("4"));
        dest.put("beta", createListOfValues("4", "5", "6"));
        dest.put("gamma", createListOfValues("7", "8", "9"));
        
        //when {the maps are merged}
        MultivaluedMapHelper.merge(src, dest, Strategy.FAVOUR_SRC);
        
        //then {values from matching source map entry 
        //must be appended to the destination map entry}
        assertThat(dest.get("alpha"), contains("1", "2", "3"));
        assertThat(dest.get("beta"), contains("4", "5", "6"));
        assertThat(dest.get("gamma"), contains("7", "8", "9"));
        assertThat(dest.size(), equalTo(3));
        
        //and {source values must not be altered}
        assertThat(src.get("alpha"), contains("1", "2", "3"));
        assertThat(src.size(), equalTo(1));
    }
    
    @Test
    public void testMergeWithDuplicateKeysFavourDest(){
        //given {source map contains one entry}
        src.put("alpha", createListOfValues("1", "2", "3"));
        
        //and {destination map contains a duplicate key and two other entries}
        dest.put("alpha", createListOfValues("4"));
        dest.put("beta", createListOfValues("4", "5", "6"));
        dest.put("gamma", createListOfValues("7", "8", "9"));
        
        //when {the maps are merged}
        MultivaluedMapHelper.merge(src, dest, Strategy.FAVOUR_DEST);
        
        //then {values from matching source map entry 
        //must be appended to the destination map entry}
        assertThat(dest.get("alpha"), contains("4"));
        assertThat(dest.get("beta"), contains("4", "5", "6"));
        assertThat(dest.get("gamma"), contains("7", "8", "9"));
        assertThat(dest.size(), equalTo(3));
        
        //and {source values must not be altered}
        assertThat(src.get("alpha"), contains("1", "2", "3"));
        assertThat(src.size(), equalTo(1));
    }
    
    @Test
    public void testMergeWithDuplicateValues(){
        //given {source map contains one entry}
        src.put("alpha", createListOfValues("1", "2", "3"));
        
        //and {destination map contains a duplicate key/value pairing 
        //and two other entries}
        dest.put("alpha", createListOfValues("1"));
        dest.put("beta", createListOfValues("4", "5", "6"));
        dest.put("gamma", createListOfValues("7", "8", "9"));
        
        //when {the maps are merged}
        MultivaluedMapHelper.merge(src, dest, Strategy.UNION);
        
        //then {duplicate values must not be appended to the destination map entry}
        assertThat(dest.get("alpha"), contains("1", "2", "3"));
        assertThat(dest.get("beta"), contains("4", "5", "6"));
        assertThat(dest.get("gamma"), contains("7", "8", "9"));
        assertThat(dest.size(), equalTo(3));

        
        //and {source values must not be altered}
        assertThat(src.get("alpha"), contains("1", "2", "3"));
        assertThat(src.size(), equalTo(1));
    }
    
    @Test
    public void testMergeWithNullValuesInSourceMapList(){
        //given {source map has a unique entry containing a null element}
        src.put("alpha", createListOfValues("1", "2", null, "3"));
        
        //and {destination map contains two entries}
        dest.put("beta", createListOfValues("4", "5", "6"));
        dest.put("gamma", createListOfValues("7", "8", "9"));
        
        //when {the maps are merged}
        MultivaluedMapHelper.merge(src, dest, Strategy.UNION);
        
        //then {values in the destination map entry 
        //must be identical to those in the source map}
        assertThat(dest.get("alpha"), contains("1", "2", null, "3"));
        assertThat(dest.get("beta"), contains("4", "5", "6"));
        assertThat(dest.get("gamma"), contains("7", "8", "9"));
        assertThat(dest.size(), equalTo(3));
        
        //and {source values must not be altered}
        assertThat(src.get("alpha"), contains("1", "2", null, "3"));
        assertThat(src.size(), equalTo(1));
    }
    
    @Test
    public void testMergeWithNullValuesInDestMapList(){
        //given {source map has one unique entry}
        src.put("alpha", createListOfValues("1", "2", "3"));
        
        //and {destination map has two entries, one containing a null value}
        dest.put("beta", createListOfValues("4", "5", null, "6"));
        dest.put("gamma", createListOfValues("7", "8", "9"));
        
        //when {the maps are merged}
        MultivaluedMapHelper.merge(src, dest, Strategy.UNION);
        
        //then {null values should not be omitted}
        assertThat(dest.get("alpha"), contains("1", "2", "3"));
        assertThat(dest.get("beta"), contains("4", "5", null, "6"));
        assertThat(dest.get("gamma"), contains("7", "8", "9"));
        assertThat(dest.size(), equalTo(3));

        
        //and {source values must not be altered}
        assertThat(src.get("alpha"), contains("1", "2", "3"));
        assertThat(src.size(), equalTo(1));
    }
    
    @Test
    public void testMergeWithNullValuesInSrcAndDestMapLists(){
        //given {source map has one unique entry and another entry containing a null value}
        src.put("alpha", createListOfValues("1", "2", "3"));
        src.put("beta", createListOfValues("4", "5", null, "6"));
        
        //and {destination map has one unique entry and another duplicate entry 
        //also containing a null value}
        dest.put("beta", createListOfValues("4", "5", null, "6"));
        dest.put("gamma", createListOfValues("7", "8", "9"));
        
        //when {the maps are merged}
        MultivaluedMapHelper.merge(src, dest, Strategy.UNION);
        
        //then {duplicate null values should be omitted}
        assertThat(dest.get("alpha"), contains("1", "2", "3"));
        assertThat(dest.get("beta"), contains("4", "5", null, "6"));
        assertThat(dest.get("gamma"), contains("7", "8", "9"));
        assertThat(dest.size(), equalTo(3));
        
        //and {source values must not be altered}
        assertThat(src.get("alpha"), contains("1", "2", "3"));
        assertThat(src.get("beta"), contains("4", "5", null, "6"));
        assertThat(src.size(), equalTo(2));
    }
    
    @Test
    public void testMergeWithOnlyNullValuesInSrcMapList(){
        //given {source map has one unique entry comprising solely of null values}
        src.put("alpha", createListOfValues(null, null, null));
        
        //and {destination map has two unique entries}
        dest.put("beta", createListOfValues("4", "5", "6"));
        dest.put("gamma", createListOfValues("7", "8", "9"));
        
        //when {the maps are merged}
        MultivaluedMapHelper.merge(src, dest, Strategy.UNION);
        
        //then {null elements should not be altered}
        assertThat(dest.get("alpha"), contains((String)null, null, null));
        assertThat(dest.get("beta"), contains("4", "5", "6"));
        assertThat(dest.get("gamma"), contains("7", "8", "9"));
        assertThat(dest.size(), equalTo(3));
        
        //and {source values must not be altered}
        assertThat(src.get("alpha"), contains((String)null, null, null));
        assertThat(src.size(), equalTo(1));
    }
    
    @Test
    public void testMergeWithNullValueInSrcMapAndDuplicateValues(){
        //given {source map has one entry containing a unique value and a null value}
        src.put("alpha", createListOfValues("4", null));
        
        //and {destination map has one unique entry and one duplicate key}
        dest.put("alpha", createListOfValues("1", "2", "3"));
        dest.put("beta", createListOfValues("4", "5", "6"));
        
        //when {the maps are merged}
        MultivaluedMapHelper.merge(src, dest, Strategy.UNION);
        
        //then {null values not present in the destination map should be appended}
        assertThat(dest.get("alpha"), contains("1", "2", "3", "4", null));
        assertThat(dest.get("beta"), contains("4", "5", "6"));
        
        //and {source values must not be altered}
        assertThat(src.get("alpha"), contains("4", null));
        assertThat(src.size(), equalTo(1));
    }
    
    @Test
    public void testMergeWithNullSrcMapValue(){
        //given {source map has one entry with a null value}
        src.put("alpha", null);
        
        //and {destination map has one unique entry and one duplicate key}
        dest.put("alpha", createListOfValues("1", "2", "3"));
        dest.put("beta", createListOfValues("4", "5", "6"));
        
        //when {the maps are merged}
        MultivaluedMapHelper.merge(src, dest, Strategy.UNION);
        
        //then {null values should not be appended}
        assertThat(dest.get("alpha"), contains("1", "2", "3"));
        assertThat(dest.get("beta"), contains("4", "5", "6"));
        assertThat(dest.size(), equalTo(2));
        
        //and {source values must not be altered}
        assertThat(src.get("alpha"), nullValue());
        assertThat(src.size(), equalTo(1));
    }
    
    @Test
    public void testMergeWithNullDestMapValue(){
        //given {source map has one entry}
        src.put("alpha", createListOfValues("1", "2", "3"));
        
        //and {destination map has one duplicate key with 
        //a null value and one unique entry}
        dest.put("alpha", null);
        dest.put("beta", createListOfValues("4", "5", "6"));
        
        //when {the maps are merged}
        MultivaluedMapHelper.merge(src, dest, Strategy.UNION);
        
        //then {null value should be overwritten}
        assertThat(dest.get("alpha"), contains("1", "2", "3"));
        assertThat(dest.get("beta"), contains("4", "5", "6"));
        assertThat(dest.size(), equalTo(2));
        
        //and {source values must not be altered}
        assertThat(src.get("alpha"), contains("1", "2", "3"));
        assertThat(src.size(), equalTo(1));
    }
    
    @Test
    public void testMergeWithNullSrcAndDestMapValues(){
        //given {source map contains one entry with a null value}
        src.put("alpha", null);
        
        //and {destination map contains a duplicate key/value}
        dest.put("alpha", null);
        dest.put("beta", createListOfValues("4", "5", "6"));
        
        //when {maps are merged}
        MultivaluedMapHelper.merge(src, dest, Strategy.UNION);
        
        //then {value in the destination map should still be null}
        assertThat(dest.get("alpha"), nullValue());
        assertThat(dest.get("beta"), contains("4", "5", "6"));
        assertThat(dest.size(), equalTo(2));
        
        //and {source values must not be altered}
        assertThat(src.get("alpha"), nullValue());
        assertThat(src.size(), equalTo(1));
    }
    
    @Test
    public void testMergeWithNullSrcMap(){
        //given {source multivalued map is null}
        src = null;
        dest.put("alpha", createListOfValues("1", "2", "3"));
        
        //when {merge is invoked}
        MultivaluedMapHelper.merge(src, dest, Strategy.UNION);
        
        //then {source should be null}
        assertThat(src, equalTo(null));
        
        //and {the destination map should not be affected}
        assertThat(dest.get("alpha"), contains("1", "2", "3"));
        assertThat(dest.size(), equalTo(1));
    }
    
    @Test
    public void testMergeWithNullDestMap(){
        //given {destination multivalued map is null}
        dest = null;
        src.put("alpha", createListOfValues("1", "2", "3"));
        
        //when {merge is invoked}
        MultivaluedMapHelper.merge(src, dest, Strategy.UNION);
        
        //then {destination should be null}
        assertThat(dest, nullValue());
        
        //and {source values must not be altered}
        assertThat(src.get("alpha"), contains("1", "2", "3"));
        assertThat(src.size(), equalTo(1));
    }
    
    private List<String> createListOfValues(String... values){
        return new ArrayList<String>(Arrays.asList(values));
    }
    
}
