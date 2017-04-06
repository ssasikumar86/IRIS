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

import java.util.EnumMap;
import java.util.Map;


/**
 * A factory to create a {@link WorkflowCommandBuilder}.
 *
 * @author ikarady
 */
public class WorkflowCommandBuilderFactory implements WorkflowCommandBuilderProvider {

    private Map<WorkflowType, WorkflowCommandBuilder> builderMap;

    public WorkflowCommandBuilderFactory(CommandController commandController) {
        builderMap = getDefaultBuilderMap(commandController);
    }

    public void setBuilderMap(Map<WorkflowType, WorkflowCommandBuilder> builderMap) {
        this.builderMap = builderMap;
    }

    @Override
    public WorkflowCommandBuilder getBuilder(WorkflowType workflowType) {
        return builderMap.get(workflowType);
    }

    private static Map<WorkflowType, WorkflowCommandBuilder> getDefaultBuilderMap(CommandController commandController) {
        Map<WorkflowType, WorkflowCommandBuilder> defaultBuilderMap = new EnumMap<>(WorkflowType.class);
        defaultBuilderMap.put(WorkflowType.INTERACTION, new InteractionWorkflowStrategyCommandBuilder(commandController));
        defaultBuilderMap.put(WorkflowType.TRANSITION, new TransitionWorkflowStrategyCommandBuilder(commandController));
        return defaultBuilderMap;
    }

}
