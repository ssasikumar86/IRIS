package com.temenos.interaction.core.command;

/*
 * #%L
 * interaction-core
 * %%
 * Copyright (C) 2012 - 2014 Temenos Holdings N.V.
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


import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple matcher 
 * The Expression must be passed in the property as follow :
 * 	 view: MatchCommand {
 *   	properties [ Expression="{entity}='verCustomer_Input'" ]
 *	 }
 * Only simple expression are valid (=, &gt;, &lt;, &lt;=, &gt;=, !=, startsWith, endsWith, contains)
 * The comparison is ALWAYS made on a String basis, so a '&lt;' will in fact be a 
 * left.compareTo(right) < 0
 * 
 * The &quot; and " are removed from the values prior to comparison.
 * The Values are trimmed prior to comparison
 * Example : "  hello" = 'hello  ' return true.
 * 
 * @author taubert
 * 
 */
public class MatchCommand implements InteractionCommand {
	private static final Logger LOGGER = LoggerFactory.getLogger(MatchCommand.class);

	/*
	 * important : the biggers (in chars) first
	 */
	private static final String[] supportedComparators = new String[]{"startsWith", "endsWith", "contains", "<=", ">=", "!=", "<", ">", "="};

	@Override
	public Result execute(InteractionContext ctx) throws InteractionException {
		/*
		 * Few assertions first ...
		 */
		try {
			assert ctx != null;
			assert ctx.getCurrentState() != null;
			assert ctx.getCurrentState().getEntityName() != null && !"".equals(ctx.getCurrentState().getEntityName());

			Properties properties = ctx.getCurrentState().getViewAction().getProperties();
            String expressionVal = properties.getProperty("Expression");
            if (expressionVal == null) {
                LOGGER.error("null expression passed to MatchCommand");
                return Result.FAILURE;
            }
            String[] sExpressions = expressionVal.split(" +\\| +");

            for (String sExpression : sExpressions) {
                if (evaluate(ctx, sExpression) == Result.SUCCESS) {
                    return Result.SUCCESS;
                }
            }
			return Result.FAILURE;
		} catch (Exception e) {
		    LOGGER.error("There was an issue while evaluating the expression", e);
			return Result.FAILURE;
		}
	}	
	
	private String resolveVariable(InteractionContext ctx, String s){
		if (s == null){
			return null;
		}
        s = s.trim();
        String ret = s;
		
		if (s.startsWith("'") && s.endsWith("'")){
			ret = s.substring(1, s.length()-1).trim();
		}else{
			if (s.startsWith("\"") && s.endsWith("\"")){
				ret = s.substring(1, s.length()-1).trim();
			}else if (s.startsWith("{") && s.endsWith("}")){
				s = s.substring(1, s.length()-1).trim();
				ret = ctx.getPathParameters().getFirst(s);
				if (ret == null) {
					ret = ctx.getQueryParameters().getFirst(s);
				}
				if (ret == null){
					ret = s; // the variable without the { } 
				}else{
					ret = ret.trim();
				}
			}
		}

		return ret;
	}

    private Result evaluate(InteractionContext ctx, String sExpression) {
        try {
            /*
             * So we have an expression. Currently, only simple expression are
             * valid (=, >, <, <=, >=, !=, startsWith, endsWith, contains)
             */

            String left = null;
            String right = null;
            String comparator = null;
            for (String sOneComparator : supportedComparators) {
                int pos = sExpression.indexOf(sOneComparator);
                if (pos > 0) {
                    left = sExpression.substring(0, pos);
                    right = sExpression.substring(pos + sOneComparator.length());
                    comparator = sOneComparator;
                    break;
                }
            }

            if (comparator == null) {
                LOGGER.error(
                        "Wrong expression passed to MatchCommand. Only simple expression are valid (=, >, <, <=, >=, !=, startsWith, endsWith, contains) ");
                return Result.FAILURE;
            }

            left = resolveVariable(ctx, left);
            right = resolveVariable(ctx, right);

            /*
             * Do the comparisons.
             */
            boolean bResult = false;
            if ("=".equals(comparator)) {
                bResult = left.equals(right);
            } else if (">".equals(comparator)) {
                bResult = left.compareTo(right) > 0;
            } else if ("<".equals(comparator)) {
                bResult = left.compareTo(right) < 0;
            } else if (">=".equals(comparator)) {
                bResult = left.compareTo(right) >= 0;
            } else if ("<=".equals(comparator)) {
                bResult = left.compareTo(right) <= 0;
            } else if ("!=".equals(comparator)) {
                bResult = !left.equals(right);
            } else if ("startsWith".equals(comparator)) {
                bResult = left.startsWith(right);
            } else if ("endsWith".equals(comparator)) {
                bResult = left.endsWith(right);
            } else if ("contains".equals(comparator)) {
                bResult = left.contains(right);
            }

            if (bResult) {
                return Result.SUCCESS;
            } else {
                return Result.FAILURE;
            }
        } catch (Exception e) {
            LOGGER.error("There was an issue while evaluating the expression", e);
            return Result.FAILURE;
        }
    }
}
