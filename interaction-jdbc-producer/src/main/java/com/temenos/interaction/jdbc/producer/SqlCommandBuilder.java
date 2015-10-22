package com.temenos.interaction.jdbc.producer;

/*
 * Utility class for building SQL commands.
 * 
 * If given a key constructs a command for a single row. 
 * 
 * If given a null key constructs a command to add all rows.
 * 
 * TODO maybe need variants for different databases.
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

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.temenos.interaction.authorization.command.data.AccessProfile;
import com.temenos.interaction.authorization.command.data.FieldName;
import com.temenos.interaction.authorization.command.data.RowFilter;

class SqlCommandBuilder {

	// Somewhere to store arguments
	private String tableName;
	private String keyValue;
	private AccessProfile accessProfile;
	private ColumnTypesMap colTypesMap;

	/*
	 * Constructor when there is not a key.
	 */
	public SqlCommandBuilder(String tableName, AccessProfile accessProfile, ColumnTypesMap colTypesMap) {
		this.tableName = tableName;
		this.accessProfile = accessProfile;
		this.colTypesMap = colTypesMap;
	}
	
	/*
	 * Constructor when there is a key.
	 */
	public SqlCommandBuilder(String tableName, String keyValue, AccessProfile accessProfile, ColumnTypesMap colTypesMap) {
		this(tableName, accessProfile, colTypesMap);
		this.keyValue = keyValue;
	}

	/*
	 * Method to build Sql command
	 */
	public String getCommand() {

		// Build an SQL command
		StringBuilder builder = new StringBuilder("SELECT ");
		addSelects(builder);
		addFrom(builder);
		addWhere(builder);

		return builder.toString();
	}

	private void addSelects(StringBuilder builder) {

		// Add columns to select
		Set<FieldName> names = accessProfile.getFieldNames();
		if (null == names) {
			throw (new SecurityException("Cannot generate Sql command for null field set."));
		}
		if (names.isEmpty()) {
			// Empty select list means "return all columns".
			builder.append("*");
		} else {
			// Add comma separated list of select terms. Need to detect the last
			// operation so use old style iterator.
			Iterator<FieldName> iterator = names.iterator();
			while (iterator.hasNext()) {
				FieldName name = iterator.next();
				addSelect(builder, name);

				// If not the last entry
				if (iterator.hasNext()) {
					builder.append(", ");
				}
			}
		}
	}

	private void addSelect(StringBuilder builder, FieldName name) {
		builder.append("\"" + name.getName() + "\"");
	}

	private void addFrom(StringBuilder builder) {
		builder.append(" FROM \"" + tableName + "\"");
	}

	/*
	 * add the "WHERE x AND y" etc clause. Adds filters and/or key.
	 */
	private void addWhere(StringBuilder builder) {
		
		// If there are no filters or key return;
		if (accessProfile.getRowFilters().isEmpty() && (null == keyValue)) {
			return;
		}

		builder.append(" WHERE ");
		
		if (null != keyValue) {
			if (null == colTypesMap.getPrimaryKeyName()) {
				throw (new SecurityException("No primary key column defined for \"" + tableName + "\". Cannot look up key."));
			}
			
			// Add key as a filter
			RowFilter keyFilter = new RowFilter(colTypesMap.getPrimaryKeyName(), RowFilter.Relation.EQ, keyValue);
			addFilter(builder, keyFilter);
		}

		if (!accessProfile.getRowFilters().isEmpty()) {
			// If we already had a key need to link with an 'AND'.
			if (null != keyValue) {
				addAnd(builder);
			}
			addFilters(builder);
		}	
	}
	
	private void addAnd(StringBuilder builder) {
		builder.append(" AND ");
	}

	private void addFilters(StringBuilder builder) {

		// Add row filters
		List<RowFilter> filters = accessProfile.getRowFilters();
		if (null == filters) {
			throw (new SecurityException("Cannot generate Sql command for null row filters list."));
		}

		// Add AND separated list of filter terms. Need to detect the last
		// operation so use old style iterator.
		Iterator<RowFilter> iterator = filters.iterator();
		while (iterator.hasNext()) {
			RowFilter filter = iterator.next();

			addFilter(builder, filter);

			// If not the last entry
			if (iterator.hasNext()) {
				addAnd(builder);
			}
		}
	}

	private void addFilter(StringBuilder builder,RowFilter filter) {
		builder.append("\"" + filter.getFieldName().getName() + "\"");
		builder.append(filter.getRelation().getSqlSymbol());

		// Extract the column type from the metadata. Text
		// must be quoted but not numerics.
		boolean numeric = colTypesMap.isNumeric(filter.getFieldName().getName());
		if (numeric) {
			String value = filter.getValue();
			if (!isJdbcNumeric(value)) {
				throw (new SecurityException("Jdbc column \"" + filter.getFieldName().getName() + "\" is numeric. Filter value \"" + value + "\" is non numeric."));
			}
			builder.append(value);
		} else {
			builder.append("'" + filter.getValue() + "'");
		}
	}
	
	/*
	 * Utility to check if a string is representable as a Jdbc numeric.
	 */
	private boolean isJdbcNumeric(String value) {
	    try {
	    	// Java "BigDecimal" appears to be the closest data type to Jdbc "numeric". 
	        new BigDecimal(value);
	    }catch (NumberFormatException e) {
	        return false;
	    }
	    return true;
	}
}
