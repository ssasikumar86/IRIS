package com.temenos.interaction.authorization.command.data;


/*
 * Classes containing information about row filters.
 */

/*
 * #%L
 * interaction-commands-Authorization
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

public class RowFilter {
	// Requested relation a fields and its values.
	// Extend with additional relationships as they become available.
	public enum Relation {
		EQ("eq","="),
		NE("ne", "<>"),
		LT("lt", "<"),
		GT("gt", ">"),
		LE("le", "<="),
		GE("ge", ">=");
		
		// OData equivalent
		private final String oDataString;
		private String sqlSymbol;
		
		Relation(String oDataString, String sqlSymbol) {
			this.oDataString = oDataString;
			this.sqlSymbol = sqlSymbol;
		}

		public String getoDataString() {
			return (oDataString);
		}
		
		public String getSqlSymbol() {
			return (sqlSymbol);
		}
	}

	// Somewhere to store the data.
	private FieldName name;
	private Relation relation;
	private String value;

	public RowFilter(FieldName name, Relation relation, String value) {
		this.name = name;
		this.relation = relation;
		this.value = value;
	}

	// Constructor for callers that don't have a FieldName.
	public RowFilter(String name, Relation relation, String value) {
		this.name = new FieldName(name);
		this.relation = relation;
		this.value = value;
	}

	public FieldName getFieldName() {
		return (name);
	}

	public Relation getRelation() {
		return (relation);
	}

	public String getValue() {
		return (value);
	}
	
}
