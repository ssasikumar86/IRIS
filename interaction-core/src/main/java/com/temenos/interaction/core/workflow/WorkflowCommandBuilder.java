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
import com.temenos.interaction.core.hypermedia.Action;

import java.util.List;


/**
 * A builder to create a workflow {@link InteractionCommand}
 * out of a list of {@link Action}s or a list of {@link InteractionCommand}s.
 *
 * @author ikarady
 */
public interface WorkflowCommandBuilder {

    /**
     * Builds a workflow {@link InteractionCommand} from
     * {@link InteractionCommand}s created from the list of {@link Action}s.
     *
     * @param actions   list of {@link Action}s
     *
     * @return workflow {@link InteractionCommand}
     */
    InteractionCommand build(List<Action> actions);

}
