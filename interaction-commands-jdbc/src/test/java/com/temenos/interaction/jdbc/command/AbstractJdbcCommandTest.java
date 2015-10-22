package com.temenos.interaction.jdbc.command;

/*
 * Base class for the jdbc command tests.
 *  
 * This is a subset of the jdbc producer abstract test. However that is not exported from it's package and the tests are
 * likely to diverge in future. So accept some duplication here.
 */

/* 
 * #%L
 * interaction-command-jdbc
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

import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Before;

public class AbstractJdbcCommandTest {
	// H2 data source details
	protected static String H2_URL = "jdbc:h2:mem:JdbcCmdTest";
	protected static String H2_USER = "user";
	protected static String H2_PASSWORD = "password";

	// Number of rows in test table
	protected int TEST_ROW_COUNT = 3;

	// Name of test table
	protected static String TEST_TABLE_NAME = "data";

	// Field names for test table.
	protected static String KEY_FIELD_NAME = "key";
	protected static String VARCHAR_FIELD_NAME = "varchar";
	protected static String INTEGER_FIELD_NAME = "integer";

	// Dummy data for test table.
	protected static String TEST_KEY_DATA = "akey";
	protected static String TEST_VARCHAR_DATA = "avarchar";
	protected static int TEST_INTEGER_DATA = 1234;

	/*
	 * The SQL commands.
	 * 
	 * Notes : Different DBs have different conventions for the case of column
	 * and table names. If the name is always quoted , with "\"", it appears to
	 * be used exactly as given.
	 */

	// SQL create command
	protected static String create = "CREATE TABLE \"" + TEST_TABLE_NAME + "\" (\"" + KEY_FIELD_NAME
			+ "\" VARCHAR(255) PRIMARY KEY, \"" + VARCHAR_FIELD_NAME + "\" VARCHAR(1023), \"" + INTEGER_FIELD_NAME
			+ "\" INTEGER" + ")";

	// SQL prepared insert command.
	protected static String preparedInsert = "INSERT INTO \"" + TEST_TABLE_NAME + "\" (\"" + KEY_FIELD_NAME + "\", \""
			+ VARCHAR_FIELD_NAME + "\", \"" + INTEGER_FIELD_NAME + "\") VALUES (?, ?, ?)";

	// H2 components for test setup.
	private JdbcConnectionPool pool = null;
	private Connection conn = null;

	// Data source for the tests.
	protected JdbcDataSource dataSource;

	@Before
	public void startH2() throws SQLException {
		// Create a connection pool. This also causes the in memory database to
		// be created.
		pool = JdbcConnectionPool.create(H2_URL, H2_USER, H2_PASSWORD);

		// Open connection to in memory database
		conn = pool.getConnection();

		// Set up data source
		dataSource = new JdbcDataSource();
		dataSource.setUrl(H2_URL);
		dataSource.setUser(H2_USER);
		dataSource.setPassword(H2_PASSWORD);
	}

	@After
	public void stopH2() throws SQLException {
		// Forget data source.
		dataSource = null;
		
		// Close connection. Should cause the database to destruct.
		conn.close();

		pool.dispose();
	}

	/*
	 * Populate a test table with the default number of rows.
	 */
	protected void populateTestTable() {

		// Create a SQL table in the database.
		try {
			conn.createStatement().executeUpdate(create);
		} catch (Throwable ex) {
			fail("Create table threw exception " + ex);
		}

		// Populate the table with unique rows.
		try {
			PreparedStatement stmt = conn.prepareStatement(preparedInsert);

			for (int rowCount = 0; rowCount < TEST_ROW_COUNT; rowCount++) {
				stmt.setString(1, TEST_KEY_DATA + rowCount);
				stmt.setString(2, TEST_VARCHAR_DATA + rowCount);
				stmt.setInt(3, TEST_INTEGER_DATA + rowCount);
				stmt.executeUpdate();
			}

			conn.commit();
		} catch (SQLException ex) {
			fail("Insert threw " + ex);
		}
	}
}
