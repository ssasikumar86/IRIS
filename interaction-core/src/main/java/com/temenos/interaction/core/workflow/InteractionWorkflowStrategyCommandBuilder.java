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


import com.temenos.interaction.core.command.CommandController;
import com.temenos.interaction.core.command.InteractionCommand;
import com.temenos.interaction.core.hypermedia.Action;

import java.util.List;


/**
 * An {@link InteractionWorkflowStrategyCommand} builder that
 * uses a {@link CommandController} to create {@link InteractionCommand}s
 * out of a list of {@link Action}s and feed them into the
 * {@link InteractionWorkflowStrategyCommand} it builds.
 *
 * @author ikarady
 */
public class InteractionWorkflowStrategyCommandBuilder implements WorkflowCommandBuilder {

    private CommandController commandController;

    public InteractionWorkflowStrategyCommandBuilder(CommandController commandController) {
        this.commandController = commandController;
    }

    /**
     * Builds an {@link InteractionWorkflowStrategyCommand} from
     * {@link InteractionCommand}s created from the list of {@link Action}s.
     *
     * @param actions   list of {@link Action}s
     *
     * @return {@link InteractionWorkflowStrategyCommand}
     */
    @Override
    public InteractionWorkflowStrategyCommand build(List<Action> actions) {
        InteractionWorkflowStrategyCommand workflow = new InteractionWorkflowStrategyCommand();
        for (Action action : actions) {
            assert action != null;
            workflow.addCommand(commandController.fetchCommand(action.getName()));
        }
        return workflow;
    }

    /**
     * Builds a {@link InteractionWorkflowStrategyCommand} from
     * a list of {@link InteractionCommand}s.
     *
     * @param commands  list of {@link InteractionCommand}s
     *
     * @return {@link InteractionWorkflowStrategyCommand}
     */
    @Override
    public WorkflowCommand build(InteractionCommand[] commands) {
        InteractionWorkflowStrategyCommand workflow = new InteractionWorkflowStrategyCommand();
        for (InteractionCommand command : commands) {
            if (command != null) {
                workflow.addCommand(command);
            }
        }
        return workflow;
    }

}
