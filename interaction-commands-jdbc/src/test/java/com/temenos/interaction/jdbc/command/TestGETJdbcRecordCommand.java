package com.temenos.interaction.jdbc.command;

/* 
 * #%L
 * interaction-commands-jdbc
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

import org.junit.Test;

import com.temenos.interaction.authorization.command.util.ODataParser;
import com.temenos.interaction.core.MultivaluedMapImpl;
import com.temenos.interaction.core.command.InteractionCommand;
import com.temenos.interaction.core.command.InteractionCommand.Result;
import com.temenos.interaction.core.command.InteractionContext;
import com.temenos.interaction.core.entity.Entity;
import com.temenos.interaction.core.entity.Metadata;
import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.resource.EntityResource;
import com.temenos.interaction.jdbc.producer.JdbcProducer;

/**
 * Test the GETRawCommand class.
 */
public class TestGETJdbcRecordCommand extends AbstractJdbcCommandTest {

	/**
	 * Test constructor
	 */
	@Test
	public void testConstructor() {
		try {
			new GETJdbcRecordCommand(mock(JdbcProducer.class));
		} catch (Throwable e) {
			fail();
		}
	}

	/*
	 * Test command execution with valid key.
	 */
	@Test
	// Don't warn about the dodgy result type cast.
	@SuppressWarnings("unchecked")
	public void testExecute() {

		// Populate the database.
		populateTestTable();

		// Create a producer
		JdbcProducer producer = null;
		try {
			producer = new JdbcProducer(dataSource);
		} catch (Exception e) {
			fail();
		}

		// Create a command based on the producer.
		GETJdbcRecordCommand command = null;
		try {
			command = new GETJdbcRecordCommand(producer);
		} catch (Exception e) {
			fail();
		}

		// Create an interaction context.
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl<String>();
		
		// Select two columns.
		queryParams.add(ODataParser.SELECT_KEY, VARCHAR_FIELD_NAME + "," + INTEGER_FIELD_NAME);

		// Set up the path with a valid key.
		MultivaluedMap<String, String> pathParams = new MultivaluedMapImpl<String>();
		int expectedKey = 2;
		pathParams.add(InteractionContext.DEFAULT_ID_PATH_ELEMENT, TEST_KEY_DATA + expectedKey);
		
		// Fake up a resource state.
		ResourceState state = new ResourceState(TEST_TABLE_NAME, "rubbish", null, "rubbish");	
			
		InteractionContext ctx = new InteractionContext(mock(UriInfo.class), mock(HttpHeaders.class), pathParams,
				queryParams, state, mock(Metadata.class));

		// Execute the command
		try {
			InteractionCommand.Result result = command.execute(ctx);

			assertEquals(Result.SUCCESS, result);
		} catch (Exception e) {
			fail();
		}

		// Check the results.
		EntityResource<Entity> resource = (EntityResource<Entity>) ctx.getResource();
		assertFalse(null == resource);

		// Should be two results VARCHAR and INTEGER but not KEY.
		assertEquals(2, resource.getEntity().getProperties().getProperties().size());
		
		// Check property values
		assertEquals(TEST_VARCHAR_DATA + expectedKey, resource.getEntity().getProperties().getProperties().get(VARCHAR_FIELD_NAME).getValue());
		assertEquals(TEST_INTEGER_DATA + expectedKey, resource.getEntity().getProperties().getProperties().get(INTEGER_FIELD_NAME).getValue());
	}
	
	/*
	 * Test command execution with invalid key.
	 */
	@Test
	public void testExecuteBadKey() {

		// Populate the database.
		populateTestTable();

		// Create a producer
		JdbcProducer producer = null;
		try {
			producer = new JdbcProducer(dataSource);
		} catch (Exception e) {
			fail();
		}

		// Create a command based on the producer.
		GETJdbcRecordCommand command = null;
		try {
			command = new GETJdbcRecordCommand(producer);
		} catch (Exception e) {
			fail();
		}

		// Create an interaction context.
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl<String>();

		// If set up the path with an invalid key.
		MultivaluedMap<String, String> pathParams = new MultivaluedMapImpl<String>();
		pathParams.add(InteractionContext.DEFAULT_ID_PATH_ELEMENT, "BadKey");
		
		// Fake up a resource state.
		ResourceState state = new ResourceState(TEST_TABLE_NAME, "rubbish", null, "rubbish");	
			
		InteractionContext ctx = new InteractionContext(mock(UriInfo.class), mock(HttpHeaders.class), pathParams,
				queryParams, state, mock(Metadata.class));

		// Execute the command. Since the key is not present should fail but not throw.
		try {
			InteractionCommand.Result result = command.execute(ctx);

			assertEquals(Result.FAILURE, result);
		} catch (Exception e) {
			fail();
		}
	}
}
