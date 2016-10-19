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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for manipulating and transforming MultivaluedMap objects.
 *
 * @author dgroves
 *
 */
public class MultivaluedMapHelper {
    
    private MultivaluedMapHelper(){}
    
    /**
     * A MultivaluedMapHelper Strategy determines how values will be merged into the target map
     * if there are duplicate keys.
     * 
     * @author dgroves
     */
    public static enum Strategy {
        /**Overwrite duplicate keys' values.*/
        FAVOUR_SRC, 
        /**Do not alter duplicate keys' values.*/
        FAVOUR_DEST, 
        /**Append unique values to duplicate keys.*/
        UNION
    }
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MultivaluedMapHelper.class);
    
    /**
     * Merge two multivalued maps into a single map. Unique values will be handled in accordance to
     * the chosen merge strategy, while unique entries will be replicated in the destination map. 
     * Null entry values will be replaced if the other map's entry value is not null, 
     * but null collection elements will be preserved. 
     * 
     * @param from The source map.
     * @param to The target map that values will be merged into.
     */
    public static <K, V> void merge(MultivaluedMap<K, V> from, 
            MultivaluedMap<K, V> to, Strategy transformation){
        if(from == null || to == null){
            LOGGER.error("Attempted to merge a null map");
            return;
        }
        //use a LinkedList for faster insertion and removal
        List<V> copiedValues = new LinkedList<V>();
        for(Map.Entry<K, List<V>> entry : from.entrySet()){
            if(!to.containsKey(entry.getKey())){
                to.put(entry.getKey(), entry.getValue());
            }else if(to.get(entry.getKey()) == null || transformation == Strategy.FAVOUR_SRC){
                to.put(entry.getKey(), entry.getValue());
            }else if(transformation == Strategy.UNION){
                addAllIgnoreNullValue(entry.getValue(), copiedValues);
                removeAllIgnoreNullValue(to.get(entry.getKey()), copiedValues);
                addAllIgnoreNullValue(copiedValues, to.get(entry.getKey()));
                copiedValues.clear();
            }
        }
    }
    
    private static <V> void addAllIgnoreNullValue(List<V> src, List<V> dest){
        if(src != null && dest != null){
            dest.addAll(src);
        }
    }
    
    private static <V> void removeAllIgnoreNullValue(List<V> src, List<V> dest){
        if(src != null && dest != null){
            dest.removeAll(src);
        }
    }    
}
