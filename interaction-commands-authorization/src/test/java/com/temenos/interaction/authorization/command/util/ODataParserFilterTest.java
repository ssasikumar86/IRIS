package com.temenos.interaction.authorization.command.util;

/*
 * Test class for the oData parser/printer filter operations.
 * 
 * Not too concerned with the intermediate format of data but it must survive the 'round trip' into intermediate format
 * and back to a string.
 */

/* 
 * #%L
 * interaction-commands-authorization
 * %%
 * Copyright (C) 2012 - 2013 Temenos Holdings N.V.
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.temenos.interaction.authorization.command.data.RowFilter;

public class ODataParserFilterTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test valid filters work
     */
    @Test
    public void testSimpleFilter() {
        testValid("a eq b");
        testValid("a ne b");
        testValid("a lt b");
        testValid("a gt b");
        testValid("a le b");
        testValid("a ge b");
    }

    /**
     * Test gel Sql symbol
     */
    @Test
    public void testGetSqlSymbolFilter() {
        List<RowFilter> filter = null;
        try {
            filter = ODataParser.parseFilter("a eq b");
        } catch (Exception e) {
            fail();
        }     
        assertEquals("=", filter.get(0).getRelation().getSqlSymbol());
    }

    /**
     * Test empty filters.
     */
    @Test
    public void testEmptyFilter() {
        testValid("");
    }

    /*
     * Test filter containing multiple terms
     */
    @Test
    public void testMultipleFilter() {
        testValid("a eq b and bb eq cc");
    }

    /*
     * Test filter containing quoted space elements.
     */
    @Test
    public void testQuotesSpaceFilter() {
        testValid("'a b' eq 'b c'");
    }

    /*
     * Test filter containing quoted dot elements.
     */
    @Test
    public void testQuotesDotFilter() {
        testValid("'a.b' eq 'b.c'");
    }

    /**
     * Test invalid filters throw.
     */
    @Test
    public void testBadFilter() {
        // Bad condition
        testInvalid("a xx b");

        // Can't parse a null string.
        testInvalid(null);

        // Wrong number of element
        testInvalid("a");
        testInvalid("a b");
        testInvalid("a b c");
    }

    /**
     * Test null intermediate filter.
     */
    @Test
    public void testNullFilter() {

        String actual = null;
        boolean threw = false;
        try {
            actual = ODataParser.toFilter(null);
        } catch (Exception e) {
            threw = true;
        }

        assertTrue("Didn't throw. Expected \"" + null + "\"Actual is \"" + actual + "\"", threw);
    }

    // Test round trip for a valid filter
    private void testValid(String expected) {

        String actual = null;
        boolean threw = false;
        try {
            actual = ODataParser.toFilter(ODataParser.parseFilter(expected));
        } catch (Exception e) {
            threw = true;
        }

        assertFalse(threw);
        assertEquals(expected, actual);
    }

    // Test invalid filter throws
    private void testInvalid(String expected) {
        String actual = null;
        boolean threw = false;
        try {
            actual = ODataParser.toFilter(ODataParser.parseFilter(expected));
        } catch (Exception e) {
            threw = true;
        }
        assertTrue("Didn't throw. Expected \"" + expected + "\"Actual is \"" + actual + "\"", threw);
    }

}
