package com.temenos.interaction.core.command;

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

import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.hypermedia.Transition;


/**
 * An interim {@link TransitionCommand} that represents a {@link Transition}
 * that can only be successful when one of the {@link Transition}s
 * of its target {@link ResourceState} is successful.
 *
 * @author ikarady
 */
public class InterimTransitionCommand implements TransitionCommand {

    @Override
    public Result execute(InteractionContext ctx) throws InteractionException {
        return Result.SUCCESS;
    }

    @Override
    public boolean isInterim() {
        return true;
    }
}
