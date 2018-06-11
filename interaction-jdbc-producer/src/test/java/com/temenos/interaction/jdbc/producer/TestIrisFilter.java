package com.temenos.interaction.jdbc.producer;

/* 
 * #%L
 * interaction-jdbc-producer
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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.temenos.interaction.core.MultivaluedMapImpl;
import com.temenos.interaction.core.command.InteractionContext;
import com.temenos.interaction.core.entity.Metadata;
import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.jdbc.SqlRelation;
import com.temenos.interaction.odataext.odataparser.ODataParser;

public class TestIrisFilter extends AbstractJdbcProducerTest {

    /**
     * Test access to database using Iris parameters with a $filter term.
     */
    @Test
    public void testIrisFilterQuery() {

        // Create the producer
        JdbcProducer producer = null;
        try {
            producer = new JdbcProducer(dataSource);
        } catch (Exception e) {
            fail();
        }

        // Build up an InteractionContext
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl<String>();
        queryParams.add(ODataParser.FILTER_KEY, VARCHAR_FIELD_NAME + " " + SqlRelation.EQ.getoDataString() + " '"
                + TEST_VARCHAR_DATA + "2'");
        MultivaluedMap<String, String> pathParams = new MultivaluedMapImpl<String>();
        InteractionContext ctx = new InteractionContext(mock(UriInfo.class), mock(HttpHeaders.class), pathParams,
                queryParams, mock(ResourceState.class), mock(Metadata.class));

        // Run a query
        SqlRowSet rs = null;
        try {
            rs = producer.query(TEST_TABLE_NAME, null, ctx);
        } catch (Exception e) {
            fail();
        }

        // Check the results. Should get all fields of the single row we
        // filtered for.
        assertFalse(null == rs);

        int rowCount = 0;
        while (rs.next()) {
            assertEquals(TEST_KEY_DATA + 2, rs.getString(KEY_FIELD_NAME));
            assertEquals(TEST_VARCHAR_DATA + 2, rs.getString(VARCHAR_FIELD_NAME));
            assertEquals(TEST_INTEGER_DATA + 2, rs.getInt(INTEGER_FIELD_NAME));
            rowCount++;
        }
        assertEquals(1, rowCount);
    }

    /**
     * Test access to database using Iris parameters with a numeric $filter
     * term.
     */
    @Test
    public void testIrisNumericFilterQuery() {

        // Create the producer
        JdbcProducer producer = null;
        try {
            producer = new JdbcProducer(dataSource);
        } catch (Exception e) {
            fail();
        }

        // Build up an InteractionContext
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl<String>();
        queryParams.add(ODataParser.FILTER_KEY, INTEGER_FIELD_NAME + " " + SqlRelation.EQ.getoDataString() + " "
                + (TEST_INTEGER_DATA + 2));
        MultivaluedMap<String, String> pathParams = new MultivaluedMapImpl<String>();
        InteractionContext ctx = new InteractionContext(mock(UriInfo.class), mock(HttpHeaders.class), pathParams,
                queryParams, mock(ResourceState.class), mock(Metadata.class));

        // Run a query
        SqlRowSet rs = null;
        try {
            rs = producer.query(TEST_TABLE_NAME, null, ctx);
        } catch (Exception e) {
            fail();
        }

        // Check the results. Should get all fields of the single row we
        // filtered for.
        assertFalse(null == rs);

        int rowCount = 0;
        while (rs.next()) {
            assertEquals(TEST_KEY_DATA + 2, rs.getString(KEY_FIELD_NAME));
            assertEquals(TEST_VARCHAR_DATA + 2, rs.getString(VARCHAR_FIELD_NAME));
            assertEquals(TEST_INTEGER_DATA + 2, rs.getInt(INTEGER_FIELD_NAME));
            rowCount++;
        }
        assertEquals(1, rowCount);
    }

    /**
     * Test access to database using Iris parameters with a non numeric value
     * for a numeric $filter term.
     */
    @Test
    public void testIrisBadNumericFilterQuery() throws Exception {

        // Create the producer
        JdbcProducer producer = null;
        try {
            producer = new JdbcProducer(dataSource);
        } catch (Exception e) {
            fail();
        }

        // Build up an InteractionContext
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl<String>();
        queryParams.add(ODataParser.FILTER_KEY, INTEGER_FIELD_NAME + " " + SqlRelation.EQ.getoDataString() + " "
                + "bad");
        MultivaluedMap<String, String> pathParams = new MultivaluedMapImpl<String>();
        InteractionContext ctx = new InteractionContext(mock(UriInfo.class), mock(HttpHeaders.class), pathParams,
                queryParams, mock(ResourceState.class), mock(Metadata.class));

        // Run a query. Should fail and throw.
        boolean threw = false;
        try {
            producer.query(TEST_TABLE_NAME, null, ctx);
        } catch (SecurityException e) {
            // Old parser detected bad column names and threw this exception.
            threw = true;
        } catch (BadSqlGrammarException e) {
            // New parser does not know about column names. But bad names will
            // fail during query.
            threw = true;
        }

        if (!threw) {
            fail();
        }
    }

    /**
     * Test access to database using 'concat' in a $filter term.
     */
    @Test
    public void testConcatFilterQuery() {

        // Create the producer
        JdbcProducer producer = null;
        try {
            producer = new JdbcProducer(dataSource);
        } catch (Exception e) {
            fail();
        }

        // Build up an InteractionContext
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl<String>();

        // Create name by concatenating two strings at runtime
        queryParams.add(ODataParser.FILTER_KEY, VARCHAR_FIELD_NAME + " " + SqlRelation.EQ.getoDataString() + " "
                + SqlRelation.CONCAT.getoDataString() + "('" + TEST_VARCHAR_DATA + "', '2')");
        MultivaluedMap<String, String> pathParams = new MultivaluedMapImpl<String>();
        InteractionContext ctx = new InteractionContext(mock(UriInfo.class), mock(HttpHeaders.class), pathParams,
                queryParams, mock(ResourceState.class), mock(Metadata.class));

        // Run a query
        SqlRowSet rs = null;
        try {
            rs = producer.query(TEST_TABLE_NAME, null, ctx);
        } catch (Exception e) {
            fail();
        }

        // Check the results. Should get all fields of the single row we
        // filtered for.
        assertFalse(null == rs);

        int rowCount = 0;
        while (rs.next()) {
            assertEquals(TEST_KEY_DATA + 2, rs.getString(KEY_FIELD_NAME));
            assertEquals(TEST_VARCHAR_DATA + 2, rs.getString(VARCHAR_FIELD_NAME));
            assertEquals(TEST_INTEGER_DATA + 2, rs.getInt(INTEGER_FIELD_NAME));
            rowCount++;
        }
        assertEquals(1, rowCount);
    }

    /**
     * Test access to database using 'substr' in a $filter term.
     */
    @Test
    public void testSubstrFilterQuery() {

        // Create the producer
        JdbcProducer producer = null;
        try {
            producer = new JdbcProducer(dataSource);
        } catch (Exception e) {
            fail();
        }

        // Build up an InteractionContext
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl<String>();

        // Create name by taking a substring at runtime
        queryParams.add(ODataParser.FILTER_KEY, VARCHAR_FIELD_NAME + " " + SqlRelation.EQ.getoDataString() + " "
                + SqlRelation.SUBSTR.getoDataString() + "('" + "JUNK" + TEST_VARCHAR_DATA + "2', '5')");
        MultivaluedMap<String, String> pathParams = new MultivaluedMapImpl<String>();
        InteractionContext ctx = new InteractionContext(mock(UriInfo.class), mock(HttpHeaders.class), pathParams,
                queryParams, mock(ResourceState.class), mock(Metadata.class));

        // Run a query
        SqlRowSet rs = null;
        try {
            rs = producer.query(TEST_TABLE_NAME, null, ctx);
        } catch (Exception e) {
            fail();
        }

        // Check the results. Should get all fields of the single row we
        // filtered for.
        assertFalse(null == rs);

        int rowCount = 0;
        while (rs.next()) {
            assertEquals(TEST_KEY_DATA + 2, rs.getString(KEY_FIELD_NAME));
            assertEquals(TEST_VARCHAR_DATA + 2, rs.getString(VARCHAR_FIELD_NAME));
            assertEquals(TEST_INTEGER_DATA + 2, rs.getInt(INTEGER_FIELD_NAME));
            rowCount++;
        }
        assertEquals(1, rowCount);
    }

    /**
     * Test access to database using 3 argument version of 'substr' in a $filter
     * term.
     */
    @Test
    public void testSubstr3ArgFilterQuery() {

        // Create the producer
        JdbcProducer producer = null;
        try {
            producer = new JdbcProducer(dataSource);
        } catch (Exception e) {
            fail();
        }

        // Build up an InteractionContext
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl<String>();

        // Create name by taking a substring at runtime
        queryParams.add(ODataParser.FILTER_KEY, VARCHAR_FIELD_NAME + " " + SqlRelation.EQ.getoDataString() + " "
                + SqlRelation.SUBSTR.getoDataString() + "('" + "JUNK" + TEST_VARCHAR_DATA + "2JUNK', '5', '"
                + (TEST_VARCHAR_DATA.length() + 1) + "')");
        MultivaluedMap<String, String> pathParams = new MultivaluedMapImpl<String>();
        InteractionContext ctx = new InteractionContext(mock(UriInfo.class), mock(HttpHeaders.class), pathParams,
                queryParams, mock(ResourceState.class), mock(Metadata.class));

        // Run a query
        SqlRowSet rs = null;
        try {
            rs = producer.query(TEST_TABLE_NAME, null, ctx);
        } catch (Exception e) {
            fail();
        }

        // Check the results. Should get all fields of the single row we
        // filtered for.
        assertFalse(null == rs);

        int rowCount = 0;
        while (rs.next()) {
            assertEquals(TEST_KEY_DATA + 2, rs.getString(KEY_FIELD_NAME));
            assertEquals(TEST_VARCHAR_DATA + 2, rs.getString(VARCHAR_FIELD_NAME));
            assertEquals(TEST_INTEGER_DATA + 2, rs.getInt(INTEGER_FIELD_NAME));
            rowCount++;
        }
        assertEquals(1, rowCount);
    }

    /**
     * Test access to database using 'replace' in a $filter term.
     */
    @Test
    public void testReplaceFilterQuery() {

        // Create the producer
        JdbcProducer producer = null;
        try {
            producer = new JdbcProducer(dataSource);
        } catch (Exception e) {
            fail();
        }

        // Build up an InteractionContext
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl<String>();

        // Create name by replacing one string with another at runtime
        queryParams.add(ODataParser.FILTER_KEY, VARCHAR_FIELD_NAME + " " + SqlRelation.EQ.getoDataString() + " "
                + SqlRelation.REPLACE.getoDataString() + "('JUNK2', 'JUNK', '" + TEST_VARCHAR_DATA + "')");
        MultivaluedMap<String, String> pathParams = new MultivaluedMapImpl<String>();
        InteractionContext ctx = new InteractionContext(mock(UriInfo.class), mock(HttpHeaders.class), pathParams,
                queryParams, mock(ResourceState.class), mock(Metadata.class));

        // Run a query
        SqlRowSet rs = null;
        try {
            rs = producer.query(TEST_TABLE_NAME, null, ctx);
        } catch (Exception e) {
            fail();
        }

        // Check the results. Should get all fields of the single row we
        // filtered for.
        assertFalse(null == rs);

        int rowCount = 0;
        while (rs.next()) {
            assertEquals(TEST_KEY_DATA + 2, rs.getString(KEY_FIELD_NAME));
            assertEquals(TEST_VARCHAR_DATA + 2, rs.getString(VARCHAR_FIELD_NAME));
            assertEquals(TEST_INTEGER_DATA + 2, rs.getInt(INTEGER_FIELD_NAME));
            rowCount++;
        }
        assertEquals(1, rowCount);
    }

    /**
     * Test access to database using 'floor' in a $filter term.
     * 
     * TODO Also write 'ceiling' and 'round' versions.
     */
    @Test
    // Manual testing of this mechanism against SQL server appears to work. But
    // this test gets no data. Possibly a difference between H2 emulation and
    // SQL server.
    @Ignore
    public void testFloorFilterQuery() {

        // Create the producer
        JdbcProducer producer = null;
        try {
            producer = new JdbcProducer(dataSource);
        } catch (Exception e) {
            fail();
        }

        // Build up an InteractionContext
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl<String>();

        // Create name by using a numeric 'floor' at runtime
        queryParams.add(ODataParser.FILTER_KEY,
                VARCHAR_FIELD_NAME + " " + SqlRelation.EQ.getoDataString() + " " + SqlRelation.CONCAT.getoDataString()
                        + "('" + TEST_VARCHAR_DATA + "', " + SqlRelation.FLOOR.getoDataString() + "('2.67')" + ")");
        MultivaluedMap<String, String> pathParams = new MultivaluedMapImpl<String>();
        InteractionContext ctx = new InteractionContext(mock(UriInfo.class), mock(HttpHeaders.class), pathParams,
                queryParams, mock(ResourceState.class), mock(Metadata.class));

        // Run a query
        SqlRowSet rs = null;
        try {
            rs = producer.query(TEST_TABLE_NAME, null, ctx);
        } catch (Exception e) {
            fail();
        }

        // Check the results. Should get all fields of the single row we
        // filtered for.
        assertFalse(null == rs);

        int rowCount = 0;
        while (rs.next()) {
            assertEquals(TEST_KEY_DATA + 2, rs.getString(KEY_FIELD_NAME));
            assertEquals(TEST_VARCHAR_DATA + 2, rs.getString(VARCHAR_FIELD_NAME));
            assertEquals(TEST_INTEGER_DATA + 2, rs.getInt(INTEGER_FIELD_NAME));
            rowCount++;
        }
        assertEquals(1, rowCount);
    }

    /**
     * Test access to database using 'trim' in a $filter term.
     */
    @Test
    public void testTrimFilterQuery() {

        // Create the producer
        JdbcProducer producer = null;
        try {
            producer = new JdbcProducer(dataSource);
        } catch (Exception e) {
            fail();
        }

        // Build up an InteractionContext
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl<String>();

        // Create name with white space around it.
        queryParams.add(ODataParser.FILTER_KEY, VARCHAR_FIELD_NAME + " " + SqlRelation.EQ.getoDataString() + " "
                + SqlRelation.TRIM.getoDataString() + "('   " + TEST_VARCHAR_DATA + "2   ')");
        MultivaluedMap<String, String> pathParams = new MultivaluedMapImpl<String>();
        InteractionContext ctx = new InteractionContext(mock(UriInfo.class), mock(HttpHeaders.class), pathParams,
                queryParams, mock(ResourceState.class), mock(Metadata.class));

        // Run a query
        SqlRowSet rs = null;
        try {
            rs = producer.query(TEST_TABLE_NAME, null, ctx);
        } catch (Exception e) {
            fail();
        }

        // Check the results. Should get all fields of the single row we
        // filtered for.
        assertFalse(null == rs);

        int rowCount = 0;
        while (rs.next()) {
            assertEquals(TEST_KEY_DATA + 2, rs.getString(KEY_FIELD_NAME));
            assertEquals(TEST_VARCHAR_DATA + 2, rs.getString(VARCHAR_FIELD_NAME));
            assertEquals(TEST_INTEGER_DATA + 2, rs.getInt(INTEGER_FIELD_NAME));
            rowCount++;
        }
        assertEquals(1, rowCount);
    }

    /**
     * Test access to database using 'indexof' in a $filter term.
     */
    @Test
    public void testIndexOfFilterQuery() {

        // Create the producer
        JdbcProducer producer = null;
        try {
            producer = new JdbcProducer(dataSource);
        } catch (Exception e) {
            fail();
        }

        // Build up an InteractionContext
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl<String>();

        // Create name with a number based on 'indexof' (looks like characters
        // are indexed from 1).
        queryParams.add(ODataParser.FILTER_KEY,
                VARCHAR_FIELD_NAME + " " + SqlRelation.EQ.getoDataString() + " " + SqlRelation.CONCAT.getoDataString()
                        + "('" + TEST_VARCHAR_DATA + "', " + SqlRelation.INDEXOF.getoDataString()
                        + "('xtestyyy', 'test'))");
        MultivaluedMap<String, String> pathParams = new MultivaluedMapImpl<String>();
        InteractionContext ctx = new InteractionContext(mock(UriInfo.class), mock(HttpHeaders.class), pathParams,
                queryParams, mock(ResourceState.class), mock(Metadata.class));

        // Run a query
        SqlRowSet rs = null;
        try {
            rs = producer.query(TEST_TABLE_NAME, null, ctx);
        } catch (Exception e) {
            fail();
        }

        // Check the results. Should get all fields of the single row we
        // filtered for.
        assertFalse(null == rs);

        int rowCount = 0;
        while (rs.next()) {
            assertEquals(TEST_KEY_DATA + 2, rs.getString(KEY_FIELD_NAME));
            assertEquals(TEST_VARCHAR_DATA + 2, rs.getString(VARCHAR_FIELD_NAME));
            assertEquals(TEST_INTEGER_DATA + 2, rs.getInt(INTEGER_FIELD_NAME));
            rowCount++;
        }
        assertEquals(1, rowCount);
    }
}
