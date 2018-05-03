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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;

import com.temenos.interaction.core.hypermedia.Event;
import com.temenos.interaction.core.hypermedia.MethodNotAllowedException;
import com.temenos.interaction.core.hypermedia.PathTree;
import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.hypermedia.ResourceStateProvider;
import com.temenos.interaction.core.resource.AbstractConfigLoaders;
import com.temenos.interaction.core.resource.ConfigLoader;

public class SpringDSLResourceStateProvider  implements ResourceStateProvider, DynamicRegistrationResourceStateProvider {
    
    private final Logger logger = LoggerFactory.getLogger(SpringDSLResourceStateProvider.class);

    protected ConcurrentMap<String, ResourceState> resources = new ConcurrentHashMap<String, ResourceState>();

    protected StateRegisteration stateRegisteration;
    
	private AbstractConfigLoaders configLoader = new ConfigLoader();

    /**
     * Map of ResourceState bean names, to paths.
     */
    protected Properties beanMap;
    
    protected boolean initialised = false;
    
    protected String state;
    protected List<String> attempts = new ArrayList<String>(2);
    protected String foundFile;
    
    /**
     * Map of paths to state names
     */
    protected Map<String, Set<String>> resourceStatesByPath = new HashMap<String, Set<String>>();
    /**
     * Map of request to state names
     */
    protected Map<String, String> resourceStatesByRequest = new HashMap<String, String>();
    /**
     * Map of resource methods where state name is the key
     */
    protected Map<String, Set<String>> resourceMethodsByState = new HashMap<String, Set<String>>();
    /**
     * Map to a resource path where the state name is the key
     */
    protected Map<String, String> resourcePathsByState = new HashMap<String, String>();
    
    ResourceLoader resourceLoader;
    
    @Autowired(required = false)
    public void setConfigLoader(ConfigLoader configLoader) {
        this.configLoader = configLoader;
    }
    
    PathTree pathTree = new PathTree();
    
    public SpringDSLResourceStateProvider() {}
    
    public SpringDSLResourceStateProvider(Properties beanMap) {
        this.beanMap = beanMap;
    }
    
    public SpringDSLResourceStateProvider(Properties beanMap, ResourceLoader resourceLoader) {
        this.beanMap = beanMap;
        this.resourceLoader = resourceLoader;
    }
    
    public void setResourceMap(Properties beanMap) {
        this.beanMap = beanMap;
    }
    
    protected void initialise() {
        if (initialised)
            return;
        for (Object stateObj : beanMap.keySet()) {
            storeState(stateObj, null);
        }
                
        initialised = true;
    }

    protected void storeState(Object stateObj, String binding) {
        String stateName = stateObj.toString();
        
        // binding is [GET,PUT /thePath]
        if (binding == null){
            binding = beanMap.getProperty(stateName);
        }
        
        // split into methods and path
        String[] strs = binding.split(" ");
        String methodPart = strs[0];
        String path = strs[1];
        // methods
        String[] methodsStrs = methodPart.split(",");
        // path
        resourcePathsByState.put(stateName, path);
        // methods
        Set<String> methodSet = resourceMethodsByState.get(stateName);
        
        if (methodSet == null) {
            methodSet = new HashSet<String>();
        }
        
        for(String methodStr: methodsStrs) {
            methodSet.add(methodStr);
            
            pathTree.put(path, methodStr, stateName);
        }
        
        resourceMethodsByState.put(stateName, methodSet);
        
        for (String method : methodSet) {
            String request = method + " " + path;
            logger.debug("Binding ["+stateName+"] to ["+request+"]");
            String found = resourceStatesByRequest.get(request);
            if (found != null) {
                logger.debug("Multiple states bound to the same request ["+request+"], overriding ["+found+"] with ["+stateName+"]");
            }
            resourceStatesByRequest.put(request, stateName);
        }

        Set<String> stateNames = resourceStatesByPath.get(path);
        if (stateNames == null) {
            stateNames = new HashSet<String>();
        }
        stateNames.add(stateName);
        resourceStatesByPath.put(path, stateNames);     
    }
    
    

    public void addState(String stateObj, Properties properties) {
        if (initialised) {
            String stateName = stateObj.toString();

            // binding is [GET,PUT /thePath]
            String binding = properties.getProperty(stateName);

            // split into methods and path
            String[] strs = binding.split(" ");
            String methodPart = strs[0];
            String path = strs[1];

            // methods
            String[] methods = methodPart.split(",");

            logger.info("Attempting to register state: " + stateName + " methods: " + methods + " path: " + path);

            // preemptive loading
            ResourceState state = getResourceState(stateName);
            
            if (state != null){
                storeState(stateName, binding);
                
                Set<String> methodSet = new HashSet<String>();
                
                for(String methodStr: methods) {
                    methodSet.add(methodStr);
                }
            }
        }
    }
    
    public void unload(String name) {       
        this.resources.remove(name);
    }

    @Override
    public boolean isLoaded(String name) {
        return this.resources.containsKey(name);
    }

    @Override
    public ResourceState getResourceState(String resourceStateName) {
        ResourceState result = null;

        try {
            if (resourceStateName != null) {
                // Try to retrieve the resource state
                result = resources.get(resourceStateName);

                if (result == null) {
                    // Resource state has not already been loaded so attempt to load it
                    resourceLoader.initialise(beanMap, resources, result, pathTree);
                    resourceLoader.load(resourceStateName);
                    if ( resourceLoader.isLoaded() ) {
                        result = resourceLoader.loaded();                     
                    } else {
                        logger.error( resourceLoader.toString() );
                    }
                }
            }
        } catch (BeansException e) {
            logger.error("Failed to load ["+resourceStateName+"]", e);
        }

        return result;
    }

    @Override
    public ResourceState determineState(Event event, String resourcePath) {
        initialise();
        String request = event.getMethod() + " " + resourcePath;
        String stateName = resourceStatesByRequest.get(request);
        if (stateName != null){
            logger.debug("Found state ["+stateName+"] for ["+request+"]");
            return getResourceState(stateName);
        }else{
            logger.warn("NOT Found state ["+stateName+"] for ["+request+"]");
            return null;
        }
    }

    @Override
    public Map<String, Set<String>> getResourceStatesByPath() {
        initialise();
        return resourceStatesByPath;
    }

    public Map<String, Set<String>> getResourceMethodsByState() {
        initialise();
        return resourceMethodsByState;
    }

    public Map<String, String> getResourcePathsByState() {
        initialise();
        return resourcePathsByState;
    }

    protected Map<String, Set<String>> getResourceStatesByPath(Properties beanMap) {
        initialise();
        return resourceStatesByPath;
    }

    @Override
    public void setStateRegisteration(StateRegisteration registerState) {
        this.stateRegisteration = registerState;
    }
    
    @Override
    public ResourceState getResourceState(String httpMethod, String url) throws MethodNotAllowedException {
        String resourceStateId = getResourceStateId(httpMethod, url);
        if(resourceStateId == null) {
            if(pathTree.get(url) != null) {
                Set<String> allowedMethods = pathTree.get(url).keySet();
                throw new MethodNotAllowedException(allowedMethods);
            } else {
                return null;
            }
        }
        return getResourceState(resourceStateId);
    }
    
    public String getResourceStateId(String httpMethod, String url) throws MethodNotAllowedException {
        
       resourceLoader.initialise(beanMap, resources, null, pathTree);
       return resourceLoader.getResourceStateId(httpMethod, url);
    }
    

    /**
     * @return the resourceLoader
     */
    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    /**
     * @param resourceLoader the resourceLoader to set
     */
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }


}