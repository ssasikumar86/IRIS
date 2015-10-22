package com.temenos.interaction.jdbc.exceptions;

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
import static org.junit.Assert.fail;

import javax.ws.rs.core.Response.Status;

import org.junit.Test;

/**
 * Test JdbcException class.
 */
public class TestJdbcException {

	private static Status expectedStatus = Status.FORBIDDEN;
	private static String expectedMessage = "a message";
	private static Exception expectedCause = new Exception("a cause");

	@Test
	public void testConstructorStatus() {

		JdbcException actualException = null;
		try {
			actualException = new JdbcException(expectedStatus);
		} catch (Exception e) {
			fail();
		}

		// Check result
		assertEquals(expectedStatus, actualException.getHttpStatus());
	}

	@Test
	public void testConstructorStatusMessage() {
		JdbcException actualException = null;
		try {
			actualException = new JdbcException(expectedStatus, expectedMessage);
		} catch (Exception e) {
			fail();
		}

		// Check result
		assertEquals(expectedStatus, actualException.getHttpStatus());
		assertEquals(expectedMessage, actualException.getMessage());
	}

	@Test
	public void testConstructorStatusMessageCause() {
		JdbcException actualException = null;
		try {
			actualException = new JdbcException(expectedStatus, expectedMessage, expectedCause);
		} catch (Exception e) {
			fail();
		}

		// Check result
		assertEquals(expectedStatus, actualException.getHttpStatus());
		assertEquals(expectedMessage, actualException.getMessage());
		assertEquals(expectedCause, actualException.getCause());
	}

	@Test
	public void testConstructorStatusCause() {
		JdbcException actualException = null;
		try {
			actualException = new JdbcException(expectedStatus, expectedCause);
		} catch (Exception e) {
			fail();
		}

		// Check result
		assertEquals(expectedStatus, actualException.getHttpStatus());
		assertEquals(expectedCause, actualException.getCause());
	}
}
