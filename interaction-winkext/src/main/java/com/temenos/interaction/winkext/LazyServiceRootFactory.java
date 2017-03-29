package com.temenos.interaction.winkext;

/*
 * #%L
 * interaction-winkext
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


import java.util.HashSet;
import java.util.Set;

import com.temenos.interaction.core.cache.Cache;
import com.temenos.interaction.core.command.CommandController;
import com.temenos.interaction.core.entity.Metadata;
import com.temenos.interaction.core.hypermedia.ResourceLocatorProvider;
import com.temenos.interaction.core.hypermedia.ResourceParameterResolverProvider;
import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.hypermedia.ResourceStateMachine;
import com.temenos.interaction.core.hypermedia.ResourceStateProvider;
import com.temenos.interaction.core.hypermedia.Transformer;
import com.temenos.interaction.core.rim.HTTPResourceInteractionModel;
import com.temenos.interaction.core.workflow.WorkflowCommandBuilderProvider;


/**
 * A resource factory that uses the beans and configuration files from the SpringDSL implementation
 * to construct all the resources required for an hypermedia server instance.
 * @author aphethean
 */
public class LazyServiceRootFactory implements ServiceRootFactory {

	private ResourceStateProvider resourceStateProvider;
	private ResourceStateMachine hypermediaEngine;
	
	// members passed to lazy resources
	private CommandController commandController;
	private Metadata metadata;
	private ResourceLocatorProvider resourceLocatorProvider;
	private ResourceParameterResolverProvider parameterResolverProvider;
	private WorkflowCommandBuilderProvider workflowCommandBuilderProvider;
	private Cache cacheImpl;
	private ResourceState exception;
	private Transformer transformer;

	@Override
	public Set<HTTPResourceInteractionModel> getServiceRoots() {		
		hypermediaEngine = new ResourceStateMachine.Builder()
				.initial(null)
				.exception(exception)
				.transformer(transformer)
				.resourceLocatorProvider(resourceLocatorProvider)
				.resourceStateProvider(resourceStateProvider)
				.parameterResolverProvider(parameterResolverProvider)
				.workflowCommandBuilderProvider(workflowCommandBuilderProvider)
				.responseCache(cacheImpl)
				.build();

		Set<HTTPResourceInteractionModel> services = new HashSet<HTTPResourceInteractionModel>();		
		Set<String> methods = new HashSet<String>();
		methods.add("GET");
        methods.add("POST");
        methods.add("PUT");
        methods.add("DELETE");
        methods.add("OPTIONS");        
		
        // Register a single Wink resource that is responsible for delegating requests to IRIS
		LazyResourceDelegate resource = new LazyResourceDelegate(hypermediaEngine,
                resourceStateProvider,
                commandController,
                metadata,
                "all", 
                "{var:.*}",
                methods);
		
		services.add(resource);
		
        return services;        
	}
	
	public CommandController getCommandController() {
		return commandController;
	}

	public void setCommandController(CommandController commandController) {
		this.commandController = commandController;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public void setMetadata(Metadata metadata) {
		this.metadata = metadata;
	}

	public void setResourceLocatorProvider(ResourceLocatorProvider resourceLocatorProvider) {
		this.resourceLocatorProvider = resourceLocatorProvider;
	}

	public void setResourceParameterResolverProvider(ResourceParameterResolverProvider parameterResolverProvider) {
		this.parameterResolverProvider = parameterResolverProvider;
	}

	public void setWorkflowCommandBuilderProvider(WorkflowCommandBuilderProvider workflowCommandBuilderProvider) {
		this.workflowCommandBuilderProvider = workflowCommandBuilderProvider;
	}

	public ResourceState getException() {
		return exception;
	}

	public void setException(ResourceState exception) {
		this.exception = exception;
	}

	public Transformer getTransformer() {
		return transformer;
	}

	public void setTransformer(Transformer transformer) {
		this.transformer = transformer;
	}

	public ResourceStateProvider getResourceStateProvider() {
		return resourceStateProvider;
	}

	public void setResourceStateProvider(ResourceStateProvider resourceStateProvider) {
		this.resourceStateProvider = resourceStateProvider;
	}

	public void setHypermediaEngine(ResourceStateMachine hypermediaEngine) {
		this.hypermediaEngine = hypermediaEngine;
	}
	
	public Cache getCacheImpl() {
		return cacheImpl;
	}
	
	public void setCacheImpl(Cache cache) {
		cacheImpl = cache;
	}
}
