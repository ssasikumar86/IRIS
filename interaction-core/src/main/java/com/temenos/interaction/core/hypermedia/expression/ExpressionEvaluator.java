package com.temenos.interaction.core.hypermedia.expression;

/*
 * #%L
 * interaction-core
 * %%
 * Copyright (C) 2012 - 2017 Temenos Holdings N.V.
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


import com.temenos.interaction.core.command.InteractionContext;
import com.temenos.interaction.core.resource.EntityResource;


/**
 * Evaluates an {@link Expression} using {@link InteractionContext} and {@link EntityResource}.
 *
 * @author ikarady
 */
public interface ExpressionEvaluator {

    /**
     * Evaluate an {@link Expression} using {@link InteractionContext} and {@link EntityResource}.
     * Return a boolean result.
     *
     * @param expression the {@link Expression} to evaluate
     * @param ctx the {@link InteractionContext} used for evaluation
     * @param resource the {@link EntityResource} used for evaluation

     * @return true or false
     */
    public boolean evaluate(Expression expression, InteractionContext ctx, EntityResource<?> resource);

}
