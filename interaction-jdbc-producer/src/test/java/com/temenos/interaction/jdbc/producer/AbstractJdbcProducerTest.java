package com.temenos.interaction.jdbc.producer;

/*
 * Base class for the jdbc producer tests.
 */

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

import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Before;

public class AbstractJdbcProducerTest {
    // H2 data source details
    protected static String H2_URL = "jdbc:h2:mem:JdbcProducertest";
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
    private static String create = "CREATE TABLE \"" + TEST_TABLE_NAME + "\" (\"" + KEY_FIELD_NAME
            + "\" VARCHAR(255) PRIMARY KEY, \"" + VARCHAR_FIELD_NAME + "\" VARCHAR(1023), \"" + INTEGER_FIELD_NAME
            + "\" INTEGER" + ")";

    // SQL create command with primary key missing
    private static String createNoPrimaryKey = "CREATE TABLE \"" + TEST_TABLE_NAME + "\" (\"" + KEY_FIELD_NAME
            + "\" VARCHAR(255), \"" + VARCHAR_FIELD_NAME + "\" VARCHAR(1023), \"" + INTEGER_FIELD_NAME + "\" INTEGER"
            + ",\"RECID\" INTEGER)";

    // SQL Query command
    protected static String query = "SELECT \"" + KEY_FIELD_NAME + "\", \"" + VARCHAR_FIELD_NAME + "\", \""
            + INTEGER_FIELD_NAME + "\" FROM \"" + TEST_TABLE_NAME + "\"";

    // SQL prepared insert command.
    private static String preparedInsert = "INSERT INTO \"" + TEST_TABLE_NAME + "\" (\"" + KEY_FIELD_NAME + "\", \""
            + VARCHAR_FIELD_NAME + "\", \"" + INTEGER_FIELD_NAME + "\") VALUES (?, ?, ?)";

    // Commands for setting H2 into different server emulation modes,
    private static String setMSSQLMode = "SET MODE MSSQLServer";
    private static String setOracleMode = "SET MODE Oracle";

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
        populateTestTable(TEST_ROW_COUNT, true);
    }

    // Version where the number of rows is passed in. ALso a flag indicating if
    // a primary key should be created.
    protected void populateTestTable(int rowNumber) {
        populateTestTable(rowNumber, true);
    }

    // Version where the number of rows is passed in. ALso a flag indicating if
    // a primary key should be created.
    protected void populateTestTable(int rowNumber, boolean primayKeyRequired) {
        // Create a SQL table in the database.
        try {
            if (primayKeyRequired) {
                conn.createStatement().executeUpdate(create);
            } else {
                conn.createStatement().executeUpdate(createNoPrimaryKey);
            }
        } catch (Throwable ex) {
            fail("Create table threw exception " + ex);
        }

        // Populate the table with unique rows.
        try {
            PreparedStatement stmt = conn.prepareStatement(preparedInsert);

            for (int rowCount = 0; rowCount < rowNumber; rowCount++) {
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

    // Utilities to set server emulation modes.
    protected void setMSSQLMode() {
        setServerMode(setMSSQLMode);
    }

    protected void setOracleMode() {
        setServerMode(setOracleMode);
    }

    private void setServerMode(String mode) {
        try {
            conn.createStatement().execute(mode);
        } catch (SQLException ex) {
            fail("Cound not set serer emulation server mode. " + ex);
        }
    }
}
