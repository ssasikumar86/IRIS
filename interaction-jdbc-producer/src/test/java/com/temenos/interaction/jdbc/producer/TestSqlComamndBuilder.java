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
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.temenos.interaction.authorization.command.data.AccessProfile;
import com.temenos.interaction.authorization.command.data.FieldName;
import com.temenos.interaction.authorization.command.data.RowFilter;

/**
 * Test SqlCommandBuilder class.
 */
public class TestSqlComamndBuilder {

	private static final String TEST_TABLE_NAME = "testTable";

	/**
	 * Test a command with empty lists
	 */
	@Test
	public void testGetCommandEmpty() {

		// Build up an access profile
		List<RowFilter> filters = new ArrayList<RowFilter>();
		Set<FieldName> selects = new HashSet<FieldName>();
		AccessProfile accessProfile = new AccessProfile(filters, selects);

		// Create the builder
		SqlCommandBuilder builder = null;
		try {
			builder = new SqlCommandBuilder(TEST_TABLE_NAME, null, accessProfile, mock(ColumnTypesMap.class));
		} catch (Exception e) {
			fail();
		}

		// Get the command.
		String actualCommand = builder.getCommand();

		assertEquals("SELECT * FROM \"" + TEST_TABLE_NAME + "\"", actualCommand);
	}

	/**
	 * Test a simple command
	 */
	@Test
	public void testGetCommand() {

		// Build up an access profile. Use a mixture of relations and
		// numeric/text fields.
		List<RowFilter> filters = new ArrayList<RowFilter>();
		filters.add(new RowFilter("col1", RowFilter.Relation.EQ, "value1"));
		filters.add(new RowFilter("col2", RowFilter.Relation.NE, "2"));
		filters.add(new RowFilter("col3", RowFilter.Relation.LT, "value3"));
		filters.add(new RowFilter("col4", RowFilter.Relation.GT, "4"));
		filters.add(new RowFilter("col5", RowFilter.Relation.LE, "value5"));
		filters.add(new RowFilter("col6", RowFilter.Relation.GE, "value6"));

		// Build up some column metadata matching the above columns. The correct
		// fields must be numeric of textual.
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("col1", java.sql.Types.VARCHAR);
		map.put("col2", java.sql.Types.INTEGER);
		map.put("col3", java.sql.Types.NVARCHAR);
		map.put("col4", java.sql.Types.DOUBLE);
		map.put("col5", java.sql.Types.TIMESTAMP);
		map.put("col6", java.sql.Types.VARCHAR);
		ColumnTypesMap columnTypesMap = new ColumnTypesMap(map, null);

		Set<FieldName> selects = new HashSet<FieldName>();
		selects.add(new FieldName("col1"));
		selects.add(new FieldName("col2"));
		AccessProfile accessProfile = new AccessProfile(filters, selects);

		// Create the builder
		SqlCommandBuilder builder = null;
		try {
			builder = new SqlCommandBuilder(TEST_TABLE_NAME, null, accessProfile, columnTypesMap);
		} catch (Exception e) {
			fail();
		}

		// Get the command.
		String actualCommand = builder.getCommand();
		assertEquals(
				"SELECT \"col1\", \"col2\" FROM \""
						+ TEST_TABLE_NAME
						+ "\" WHERE \"col1\"='value1' AND \"col2\"<>2 AND \"col3\"<'value3' AND \"col4\">4 AND \"col5\"<='value5' AND \"col6\">='value6'",
				actualCommand);
	}

	/**
	 * Test a command with a key.
	 */
	@Test
	public void testGetCommandKey() {

		// Build up an access profile
		List<RowFilter> filters = new ArrayList<RowFilter>();
		Set<FieldName> selects = new HashSet<FieldName>();
		AccessProfile accessProfile = new AccessProfile(filters, selects);

		// Build up some column metadata with a primary key
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("col1", java.sql.Types.VARCHAR);
		map.put("col2", java.sql.Types.INTEGER);
		ColumnTypesMap columnTypesMap = new ColumnTypesMap(map, "col1");

		String keyValue = "aKeyValue";

		// Create the builder
		SqlCommandBuilder builder = null;
		try {
			builder = new SqlCommandBuilder(TEST_TABLE_NAME, keyValue, accessProfile, columnTypesMap);
		} catch (Exception e) {
			fail();
		}

		// Get the command.
		String actualCommand = builder.getCommand();

		assertEquals("SELECT * FROM \"" + TEST_TABLE_NAME + "\"" + " WHERE \"col1\"" + "='" + keyValue + "'",
				actualCommand);
	}

	/**
	 * Test failure if a key value is given but no primary key column is
	 * present.
	 */
	@Test(expected = SecurityException.class)
	public void testGetCommandNullKey() throws Exception {

		// Build up an access profile
		List<RowFilter> filters = new ArrayList<RowFilter>();
		Set<FieldName> selects = new HashSet<FieldName>();
		AccessProfile accessProfile = new AccessProfile(filters, selects);

		// Build up some column metadata with a primary key
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("col1", java.sql.Types.VARCHAR);
		map.put("col2", java.sql.Types.INTEGER);
		ColumnTypesMap columnTypesMap = new ColumnTypesMap(map, null);

		String keyValue = "aKeyValue";

		// Create the builder
		SqlCommandBuilder builder = null;
		try {
			builder = new SqlCommandBuilder(TEST_TABLE_NAME, keyValue, accessProfile, columnTypesMap);
		} catch (Exception e) {
			fail();
		}

		// Get the command. Should throw.
		builder.getCommand();
	}

	/**
	 * Test failure with null filter.
	 */
	@Test(expected = Exception.class)
	public void testGetCommandNullFilter() throws Exception {

		// Build up an access profile
		Set<FieldName> selects = new HashSet<FieldName>();
		AccessProfile accessProfile = new AccessProfile(null, selects);

		// Create the builder
		SqlCommandBuilder builder = null;
		try {
			builder = new SqlCommandBuilder(TEST_TABLE_NAME, null, accessProfile, mock(ColumnTypesMap.class));
		} catch (Exception e) {
			fail();
		}

		// Get the command. Should throw.
		builder.getCommand();
	}

	/**
	 * Test failure with null select.
	 */
	@Test(expected = Exception.class)
	public void testGetCommandNullSelect() throws Exception {
		// Build up an access profile
		List<RowFilter> filters = new ArrayList<RowFilter>();

		AccessProfile accessProfile = new AccessProfile(filters, null);

		// Create the builder
		SqlCommandBuilder builder = null;
		try {
			builder = new SqlCommandBuilder(TEST_TABLE_NAME, null, accessProfile, mock(ColumnTypesMap.class));
		} catch (Exception e) {
			fail();
		}

		// Get the command. Should throw.
		builder.getCommand();
	}
	
	/**
	 * Test failure with bad filter column name.
	 */
	@Test (expected = SecurityException.class)
	public void testBadColumnName() {

		// Build up an access profile.
		List<RowFilter> filters = new ArrayList<RowFilter>();
		filters.add(new RowFilter("badName", RowFilter.Relation.EQ, "value1"));

		// Build up some column metadata which does not match.
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("goodName", java.sql.Types.VARCHAR);
		ColumnTypesMap columnTypesMap = new ColumnTypesMap(map, null);
		
		Set<FieldName> selects = new HashSet<FieldName>();
		selects.add(new FieldName("goodName"));
		AccessProfile accessProfile = new AccessProfile(filters, selects);

		// Create the builder
		SqlCommandBuilder builder = null;
		try {
			builder = new SqlCommandBuilder(TEST_TABLE_NAME, null, accessProfile, columnTypesMap);
		} catch (Exception e) {
			fail();
		}

		// Get the command. Should throw
		builder.getCommand();
	}
}
