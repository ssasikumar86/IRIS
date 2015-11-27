package com.temenos.interaction.authorization.command.util;

/*
 * Test class for the oData parser/printer orderby operations.
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

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class ODataParserOrderByTest {

    @Test
    public void testAscending() {
        testValid("col asc");
    }

    @Test
    public void testDescending() {
        testValid("col desc");
    }

    @Test
    public void testDefault() {
        testValid("col");
    }

    @Test
    public void testMultiple() {
        testValid("col asc, col2 desc, col3");
    }

    @Test
    public void testNull() {
        assertEquals(null, ODataParser.parseOrderBy(null));
    }

    /**
     * Test invalid order by throw.
     */
    @Test
    public void testBadOrderBy() {
        // Can't parse a null string.
        testInvalid(null);

        // Bad direction
        testInvalid("col rubbish");

        // Wrong number of element
        testInvalid("a b c");
    }

    // Test round trip for a valid Select
    private void testValid(String expected) {

        Exception e = null;

        String actual = null;
        boolean threw = false;
        try {
            actual = ODataParser.toOrderBy(ODataParser.parseOrderBy(expected));
        } catch (Exception caught) {
            threw = true;
            e = caught;
        }

        assertFalse("Threw : " + e, threw);

        // Convert to lists. The order is important.
        List<String> expectedList = Arrays.asList(expected.split("\\s*,\\s*"));
        List<String> actualList = Arrays.asList(actual.split("\\s*,\\s*"));

        assertEquals(expectedList.size(), actualList.size());

        for (int i = 0; i < expectedList.size(); i++) {
            assertTrue(match(expectedList.get(i), actualList.get(i)));
        }
    }

    /*
     * Check if two clauses match. In ascending case "asc" is optional.
     */
    private boolean match(String expected, String actual) {
        if (expected.equals(actual)) {
            return true;
        }

        // If either ends in "asc" remove it.
        if (expected.endsWith(" asc")) {
            expected = expected.replace(" asc", "");
        }
        if (actual.endsWith(" asc")) {
            actual = actual.replace(" asc", "");
        }
        return expected.equals(actual);
    }

    // Test invalid term throws
    private void testInvalid(String expected) {

        boolean threw = false;
        try {
            ODataParser.toOrderBy(ODataParser.parseOrderBy(expected));
        } catch (Exception e) {
            threw = true;
        }
        assertTrue(threw);
    }

}
