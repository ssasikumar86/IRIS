package com.temenos.interaction.core.hypermedia.transition;

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


/**
 * An {@link Exception} that represent the exceptional
 * scenario when a {@link ResourceState} is being used
 * more than once in a process where the uniqueness of
 * {@link ResourceState} is required for it to make sense.
 *
 * @author ikarady
 */
public class ResourceStateRevisitedException extends Exception {

    private static final long serialVersionUID = 1L;

    public ResourceStateRevisitedException() {
    }

    public ResourceStateRevisitedException(String arg0) {
        super(arg0);
    }

    public ResourceStateRevisitedException(Throwable arg0) {
        super(arg0);
    }

    public ResourceStateRevisitedException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }
}
