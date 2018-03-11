package com.temenos.interaction.springdsl;

/*
 * #%L
 * interaction-springdsl
 * %%
 * Copyright (C) 2012 - 2018 Temenos Holdings N.V.
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
import java.util.concurrent.ConcurrentMap;

import com.temenos.interaction.core.hypermedia.MethodNotAllowedException;
import com.temenos.interaction.core.hypermedia.ResourceState;

/**
 * Simple interface which decides which ResourceSystemStateLoaded should be
 * called based on the spring property.
 *
 * @author mohamednazir
 *
 */
public interface ResourceLoader {
    
    public String getResourceStateId(String httpMethod, String url) throws MethodNotAllowedException;

    public void initialise(Properties beanMap, ConcurrentMap<String, ResourceState> resources, ResourceState result);
    
    public void load(String state);

    public boolean isLoaded();

    public ResourceState loaded();

    public void setIrisConfigDirPath(String location);
    
}
