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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.sql.Connection;

import javax.sql.DataSource;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.Test;
import org.springframework.jdbc.InvalidResultSetAccessException;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.temenos.interaction.core.MultivaluedMapImpl;
import com.temenos.interaction.core.command.InteractionContext;
import com.temenos.interaction.core.entity.Entity;
import com.temenos.interaction.core.entity.Metadata;
import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.resource.CollectionResource;
import com.temenos.interaction.core.resource.EntityResource;
import com.temenos.interaction.jdbc.ServerMode;
import com.temenos.interaction.jdbc.context.LocalContext;
import com.temenos.interaction.jdbc.context.LocalContextFactory;
import com.temenos.interaction.jdbc.exceptions.JdbcException;
import com.temenos.interaction.odataext.odataparser.ODataParser;

/**
 * Test JdbcProducer class.
 */
public class TestLocalJdbcProducer extends AbstractJdbcProducerTest {


    // Jndi name for the data source
    String DATA_SOURCE_JNDI_NAME = "H2Datasource";
    private Connection con = null;
    
    public static LocalContext ctx=null;
    
    /**
     * Initiate a context to lookup the datasource
     * @throws Exception 
     */
    public void setupContext() 
    {
        try{
            if(ctx == null){
                ctx = LocalContextFactory.createLocalContext("org.h2.Driver");
                ctx.addDataSource(DATA_SOURCE_JNDI_NAME,"jdbc:h2:mem:JdbcProducertest", "user", "password");
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    /**
     * Test constructor
     */
    @Test
    public void testConstructor() {
        JdbcProducer producer = null;
        try {
            producer = new JdbcProducer(dataSource);
        } catch (Exception e) {
            fail();
        }

        // Should contain DataSource
        assertEquals(dataSource, producer.getDataSource());
    }

    /**
     * Test basic access to database.
     */
    @Test
    public void testQuery() {

        // Create the producer
        JdbcProducer producer = null;
        try {
            producer = new JdbcProducer(dataSource);
        } catch (Exception e) {
            fail();
        }
        // Run a query
        SqlRowSet rs = producer.query(query);

        // Check the results
        assertFalse(null == rs);

        int rowCount = 0;
        while (rs.next()) {
            assertEquals(TEST_KEY_DATA + rowCount, rs.getString(KEY_FIELD_NAME));
            assertEquals(TEST_VARCHAR_DATA + rowCount, rs.getString(VARCHAR_FIELD_NAME));
            assertEquals(TEST_INTEGER_DATA + rowCount, rs.getInt(INTEGER_FIELD_NAME));
            rowCount++;
        }
        assertEquals(TEST_ROW_COUNT, rowCount);
    }

   
    @Test    
    public void testLocalContext(){
        // Check returned data.
        try{
            setupContext();
            DataSource ds = (DataSource) ctx.lookup(DATA_SOURCE_JNDI_NAME);
            con = ds.getConnection();
            assertEquals(dataSource.getUrl(), con.getMetaData().getURL());
            assertEquals(dataSource.getUser(), con.getMetaData().getUserName());
        }catch(Exception e){
            fail();
        }
    }
    
   
    /**
     * Test access to database with Jndi lookup of datasource
     */
    @Test
    public void testJndiQuery() {
        setupContext(); 
       // Create the jdbc producer
        JdbcProducer producer = null;
        try {
            producer = new JdbcProducer(ctx,DATA_SOURCE_JNDI_NAME);
        } catch (Exception e) {
            fail();
        }

        // Run a query
        SqlRowSet rs = producer.query(query);

        // Check the results
        assertFalse(null == rs);
        int rowCount = 0;
        while (rs.next()) {
            assertEquals(TEST_KEY_DATA + rowCount, rs.getString(KEY_FIELD_NAME));
            assertEquals(TEST_VARCHAR_DATA + rowCount, rs.getString(VARCHAR_FIELD_NAME));
            assertEquals(TEST_INTEGER_DATA + rowCount, rs.getInt(INTEGER_FIELD_NAME));
            rowCount++;
        }
        assertEquals(TEST_ROW_COUNT, rowCount);

    }


    /**
     * Test access to database using Iris parameter passing.
     */
    @Test
    public void testIrisQuery() {
        // Create the producer
        JdbcProducer producer = null;
        try {
            producer = new JdbcProducer(dataSource);
        } catch (Exception e) {
            fail();
        }

        // Build up an InteractionContext
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl<String>();
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

        // Check the results
        assertFalse(null == rs);

        int rowCount = 0;
        while (rs.next()) {
            assertEquals(TEST_KEY_DATA + rowCount, rs.getString(KEY_FIELD_NAME));
            assertEquals(TEST_VARCHAR_DATA + rowCount, rs.getString(VARCHAR_FIELD_NAME));
            assertEquals(TEST_INTEGER_DATA + rowCount, rs.getInt(INTEGER_FIELD_NAME));
            rowCount++;
        }
        assertEquals(TEST_ROW_COUNT, rowCount);
    }

    /**
     * Test access to database using Iris parameter passing and returning a
     * single entity.
     */
    @Test
    public void testIrisQueryEntity() {
        // Create the producer
        JdbcProducer producer = null;
        try {
            producer = new JdbcProducer(dataSource);
        } catch (Exception e) {
            fail();
        }

        // Build up an InteractionContext
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl<String>();
        MultivaluedMap<String, String> pathParams = new MultivaluedMapImpl<String>();
        InteractionContext ctx = new InteractionContext(mock(UriInfo.class), mock(HttpHeaders.class), pathParams,
                queryParams, mock(ResourceState.class), mock(Metadata.class));

        // Run a query
        EntityResource<Entity> entityResource = null;
        String expectedType = "returnEntityType";
        String key = TEST_KEY_DATA + 1;
        try {
            entityResource = producer.queryEntity(TEST_TABLE_NAME, key, ctx, expectedType);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        // Check the results
        assertFalse(null == entityResource);

        Entity entity = (Entity) entityResource.getEntity();

        assertEquals(expectedType, entity.getName());
        assertEquals(TEST_KEY_DATA + 1, entity.getProperties().getProperty(KEY_FIELD_NAME).getValue());
        assertEquals(TEST_VARCHAR_DATA + 1, entity.getProperties().getProperty(VARCHAR_FIELD_NAME).getValue());
        assertEquals(TEST_INTEGER_DATA + 1, entity.getProperties().getProperty(INTEGER_FIELD_NAME).getValue());

    }

    /**
     * Test access to database using Iris parameter passing and returning a
     * single entity. When the entry is not present in the database.
     */
    @Test(expected = JdbcException.class)
    public void testIrisQueryEntityMissing() throws Exception {

        // Create the producer
        JdbcProducer producer = null;
        try {
            producer = new JdbcProducer(dataSource);
        } catch (Exception e) {
            fail();
        }

        // Build up an InteractionContext
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl<String>();
        MultivaluedMap<String, String> pathParams = new MultivaluedMapImpl<String>();
        InteractionContext ctx = new InteractionContext(mock(UriInfo.class), mock(HttpHeaders.class), pathParams,
                queryParams, mock(ResourceState.class), mock(Metadata.class));

        // Run a query. Should throw.
        String expectedType = "returnEntityType";
        String key = "badEntityKey";
        producer.queryEntity(TEST_TABLE_NAME, key, ctx, expectedType);
    }

    /**
     * Test access to database using Iris parameter passing and returning a
     * collection of entities.
     */
    @Test
    public void testIrisQueryEntities() {

        // Create the producer
        JdbcProducer producer = null;
        try {
            producer = new JdbcProducer(dataSource);
        } catch (Exception e) {
            fail();
        }

        // Build up an InteractionContext
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl<String>();
        MultivaluedMap<String, String> pathParams = new MultivaluedMapImpl<String>();
        InteractionContext ctx = new InteractionContext(mock(UriInfo.class), mock(HttpHeaders.class), pathParams,
                queryParams, mock(ResourceState.class), mock(Metadata.class));

        // Run a query
        CollectionResource<Entity> entities = null;
        String expectedType = "returnEntityType";
        try {
            entities = producer.queryEntities(TEST_TABLE_NAME, ctx, expectedType);
        } catch (Exception e) {
            fail();
        }

        // Check the results
        assertFalse(null == entities);

        int entityCount = 0;
        for (EntityResource<Entity> entityResource : entities.getEntities()) {
            Entity actualEntity = (Entity) entityResource.getEntity();

            assertEquals(expectedType, actualEntity.getName());
            assertEquals(TEST_KEY_DATA + entityCount, actualEntity.getProperties().getProperty(KEY_FIELD_NAME)
                    .getValue());
            assertEquals(TEST_VARCHAR_DATA + entityCount, actualEntity.getProperties().getProperty(VARCHAR_FIELD_NAME)
                    .getValue());
            assertEquals(TEST_INTEGER_DATA + entityCount, actualEntity.getProperties().getProperty(INTEGER_FIELD_NAME)
                    .getValue());
            entityCount++;
        }
        assertEquals(TEST_ROW_COUNT, entityCount);
    }

    /**
     * Test access to database using Iris parameters with a $select term.
     */
    @Test
    public void testIrisSelectQuery() {

        // Create the producer
        JdbcProducer producer = null;
        try {
            producer = new JdbcProducer(dataSource);
        } catch (Exception e) {
            fail();
        }

        // Build up an InteractionContext
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl<String>();
        queryParams.add(ODataParser.SELECT_KEY, INTEGER_FIELD_NAME);
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

        // Check the results
        assertFalse(null == rs);

        int rowCount = 0;
        while (rs.next()) {
            boolean threw = false;
            try {
                rs.getString(KEY_FIELD_NAME);
            } catch (InvalidResultSetAccessException e) {
                threw = true;
            }
            // Not expecting this field so should throw.
            assertTrue(threw);

            threw = false;
            try {
                rs.getString(VARCHAR_FIELD_NAME);
            } catch (InvalidResultSetAccessException e) {
                threw = true;
            }
            // Not expecting this field so should throw.
            assertTrue(threw);

            // We are expecting this field.
            assertEquals(TEST_INTEGER_DATA + rowCount, rs.getInt(INTEGER_FIELD_NAME));
            rowCount++;
        }
        assertEquals(TEST_ROW_COUNT, rowCount);
    }

    /**
     * Test access to database using Iris with null tablename.
     */
    @Test(expected = JdbcException.class)
    public void testIrisQueryNullTable() throws Exception {

        // Create the producer
        JdbcProducer producer = null;
        try {
            producer = new JdbcProducer(mock(JdbcDataSource.class), ServerMode.MSSQL);
        } catch (Exception e) {
            fail();
        }

        // Build up an InteractionContext
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl<String>();
        MultivaluedMap<String, String> pathParams = new MultivaluedMapImpl<String>();
        InteractionContext ctx = new InteractionContext(mock(UriInfo.class), mock(HttpHeaders.class), pathParams,
                queryParams, mock(ResourceState.class), mock(Metadata.class));

        // Run a query. Should throw.
        producer.query(null, null, ctx);
    }
}
