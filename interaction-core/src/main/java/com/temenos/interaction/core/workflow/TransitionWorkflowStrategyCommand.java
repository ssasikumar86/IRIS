package com.temenos.interaction.core.workflow;

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


import com.temenos.interaction.core.command.InteractionCommand;
import com.temenos.interaction.core.command.InteractionContext;
import com.temenos.interaction.core.command.InteractionException;
import com.temenos.interaction.core.command.TransitionCommand;
import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.hypermedia.Transition;


/**
 * An implementation of a workflow {@link TransitionCommand}.
 *
 * @author ikarady
 */
public class TransitionWorkflowStrategyCommand extends NaiveWorkflowStrategyCommand implements TransitionCommand {

    @Override
    public Result execute(InteractionContext ctx) throws InteractionException {
        if (ctx == null) {
            throw new IllegalArgumentException("InteractionContext must be supplied");
        }
        Result result = null;
        for (InteractionCommand command : commands) {
            result = command.execute(ctx);
            if (result != null && !result.equals(Result.SUCCESS)) {
                break;
            }
        }
        return result;
    }

    /**
     * Returns true if all {@link TransitionCommand}s in this workflow are interim
     * otherwise false. It returns false if workflow has no {@link TransitionCommand}s.
     * A {@link TransitionCommand} is interim if the {@link Transition}
     * it represents can only be successful when one of the {@link Transition}s
     * of its target {@link ResourceState} is successful.
     *
     * @return true or false
     */
    @Override
    public boolean isInterim() {
        if (commands.isEmpty()) {
            return false;
        }
        for (InteractionCommand command : commands) {
            if (!((TransitionCommand) command).isInterim()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void addCommand(InteractionCommand command) {
        if (command instanceof TransitionCommand) {
            commands.add(command);
        }
    }

}
