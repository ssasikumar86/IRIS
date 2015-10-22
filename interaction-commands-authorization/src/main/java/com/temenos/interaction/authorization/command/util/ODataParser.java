package com.temenos.interaction.authorization.command.util;

/*
 * Utilities for converting between oData $select and $filters parameters and the authorization systems internal 
 * representations of the equivalent information.
 */

/*
 * #%L
 * interaction-authorization
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

import org.odata4j.expression.AndExpression;
import org.odata4j.expression.BoolCommonExpression;
import org.odata4j.expression.BooleanLiteral;
import org.odata4j.expression.CommonExpression;
import org.odata4j.expression.EntitySimpleProperty;
import org.odata4j.expression.EqExpression;
import org.odata4j.expression.GeExpression;
import org.odata4j.expression.GtExpression;
import org.odata4j.expression.LeExpression;
import org.odata4j.expression.LiteralExpression;
import org.odata4j.expression.LtExpression;
import org.odata4j.expression.NeExpression;
import org.odata4j.producer.EntityQueryInfo;
import org.odata4j.producer.resources.OptionsQueryParser;

import com.temenos.interaction.authorization.command.data.FieldName;
import com.temenos.interaction.authorization.command.data.RowFilter;
import com.temenos.interaction.authorization.command.data.RowFilter.Relation;
import com.temenos.interaction.core.command.InteractionContext;

public class ODataParser {

	// Odata option keys. Must comply with the OData standard.
	public static final String FILTER_KEY = "$filter";
	public static final String SELECT_KEY = "$select";

	/*
	 * Obtain the odata query information from the context's query parameters.
	 * This parses the incoming parameters into an oData4j EntityQueryInfo
	 * object. Further work is than required to convert this into our internal
	 * representation.
	 */
	public static EntityQueryInfo getEntityQueryInfo(InteractionContext ctx) {
		MultivaluedMap<String, String> queryParams = ctx.getQueryParameters();

		// Unpack parameters
		String filter = queryParams.getFirst(FILTER_KEY);
		String select = queryParams.getFirst(SELECT_KEY);

		return new EntityQueryInfo(OptionsQueryParser.parseFilter(filter), null, null,
				OptionsQueryParser.parseSelect(select));
	}

	// Convert an OData filter into a list of authorization framework
	// RowFilters. A complete implementation of this would be complex. For now
	// only parse simple filters and throw on failure.
	public static List<RowFilter> parseFilter(BoolCommonExpression expression)
			throws UnsupportedQueryOperationException {

		List<RowFilter> filter = new ArrayList<RowFilter>();

		if (null != expression) {
			filter = parseExpression(expression, filter);
		}

		return (filter);
	}

	// Convert an OData string parameter into a list of authorization framework
	// RowFilters. A complete implementation of this would be complex. For now
	// only parse simple filters and throw on failure.
	public static List<RowFilter> parseFilter(String filterStr) throws UnsupportedQueryOperationException {
		if (filterStr.isEmpty()) {
			// Won't parse. Return an empty filter list
			return (new ArrayList<RowFilter>());
		}
		BoolCommonExpression expression = OptionsQueryParser.parseFilter(filterStr);
		return (parseFilter(expression));
	}

	private static List<RowFilter> parseExpression(BoolCommonExpression expression, List<RowFilter> filter)
			throws UnsupportedQueryOperationException {

		if (expression == null) {
			throw new UnsupportedQueryOperationException("Unable to parse null Expression.");
		}
		if (expression instanceof AndExpression) {
			AndExpression e = (AndExpression) expression;
			parseExpression(e.getLHS(), filter);
			parseExpression(e.getRHS(), filter);
		} else if (expression instanceof EqExpression) {
			EqExpression expr = (EqExpression) expression;
			filter.add(new RowFilter(getExpressionValue(expr.getLHS()), Relation.EQ, getExpressionValue(expr.getRHS())));
		} else if (expression instanceof NeExpression) {
			NeExpression expr = (NeExpression) expression;
			filter.add(new RowFilter(getExpressionValue(expr.getLHS()), Relation.NE, getExpressionValue(expr.getRHS())));
		} else if (expression instanceof GtExpression) {
			GtExpression expr = (GtExpression) expression;
			filter.add(new RowFilter(getExpressionValue(expr.getLHS()), Relation.GT, getExpressionValue(expr.getRHS())));
		} else if (expression instanceof LtExpression) {
			LtExpression expr = (LtExpression) expression;
			filter.add(new RowFilter(getExpressionValue(expr.getLHS()), Relation.LT, getExpressionValue(expr.getRHS())));
		} else if (expression instanceof GeExpression) {
			GeExpression expr = (GeExpression) expression;
			filter.add(new RowFilter(getExpressionValue(expr.getLHS()), Relation.GE, getExpressionValue(expr.getRHS())));
		} else if (expression instanceof LeExpression) {
			LeExpression expr = (LeExpression) expression;
			filter.add(new RowFilter(getExpressionValue(expr.getLHS()), Relation.LE, getExpressionValue(expr.getRHS())));
		} else {
			throw new UnsupportedQueryOperationException("Unsupported expression " + expression);
		}
		return (filter);
	}

	private static String getExpressionValue(CommonExpression expression) throws UnsupportedQueryOperationException {
		if (expression instanceof BooleanLiteral) {
			return Boolean.toString(((BooleanLiteral) expression).getValue());
		} else if (expression instanceof EntitySimpleProperty) {
			return ((EntitySimpleProperty) expression).getPropertyName();
		} else if (expression instanceof LiteralExpression) {
			return org.odata4j.expression.Expression.literalValue((LiteralExpression) expression).toString();
		}
		throw new UnsupportedQueryOperationException("Unsupported expression " + expression);
	}

	// Convert an OData select into a list of authorization framework
	// field names.
	public static Set<FieldName> parseSelect(List<EntitySimpleProperty> propList) {

		if (null == propList) {
			return (null);
		}

		Set<FieldName> select = new HashSet<FieldName>();
		for (EntitySimpleProperty prop : propList) {
			select.add(new FieldName(prop.getPropertyName()));
		}

		return (select);
	}

	// Convert an OData select string parameter into a set of authorization
	// framework field names.
	public static Set<FieldName> parseSelect(String selectStr) {

		if (null == selectStr) {
			return (null);
		}

		Set<FieldName> select = new HashSet<FieldName>();

		List<EntitySimpleProperty> expression = OptionsQueryParser.parseSelect(selectStr);

		// Split up comma separated list
		for (EntitySimpleProperty prop : expression) {
			select.add(new FieldName(prop.getPropertyName()));
		}

		return (select);
	}

	// Convert filter to an oData parameter
	public static String toFilter(List<RowFilter> filters) {

		String filterStr = new String();

		boolean first = true;
		for (RowFilter filter : filters) {
			if (first) {
				first = false;
			} else {
				filterStr = filterStr.concat(" and ");
			}
			filterStr = filterStr.concat(toFilter(filter));
		}

		return (filterStr);
	}

	private static String toFilter(RowFilter filter) {
		String name = filter.getFieldName().getName();
		if (requiresQuotes(name)) {
			// Need to quote it
			name = new String("'" + name + "'");
		}

		String value = filter.getValue();
		if (requiresQuotes(value)) {
			// Need to quote it
			value = new String("'" + value + "'");
		}

		String filterStr = new String(name + " " + filter.getRelation().getoDataString() + " " + value);
		return (filterStr);
	}
	
	// Detects if a term needs quoted.
	private static boolean requiresQuotes(String term) {
		return (term.contains(" ") || term.contains("."));	
	}

	// Convert select to an oData parameter
	public static String toSelect(Set<FieldName> selects) {
		String selectStr = new String();

		boolean first = true;
		for (FieldName select : selects) {
			if (first) {
				first = false;
			} else {
				selectStr = selectStr.concat(",");
			}
			// If there are spaces need to quote it.
			if (requiresQuotes(select.getName())) {
				selectStr = selectStr.concat("'" + select.getName() + "'");
			} else {
				selectStr = selectStr.concat(select.getName());
			}
		}

		return (selectStr);
	}

	// Errors thrown by parsing
	public static class UnsupportedQueryOperationException extends Exception {
		private static final long serialVersionUID = 1L;

		public UnsupportedQueryOperationException(String message) {
			super(message);
		}
	}

}