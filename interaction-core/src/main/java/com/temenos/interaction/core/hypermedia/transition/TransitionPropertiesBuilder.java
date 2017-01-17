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


import com.temenos.interaction.core.MultivaluedMapImpl;
import com.temenos.interaction.core.hypermedia.Transformer;
import com.temenos.interaction.core.hypermedia.Transition;
import com.temenos.interaction.core.resource.CollectionResource;
import com.temenos.interaction.core.resource.EntityResource;
import com.temenos.interaction.core.resource.RESTResource;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Builds {@link Transition} properties out of path parameters, query parameters,
 * {@link RESTResource}s, entity objects and {@link Transition}s.
 *
 * It uses a {@link Transformer} to build the properties out of
 * {@link RESTResource}s and entity objects.
 *
 * @author ikarady
 */
public class TransitionPropertiesBuilder {

    private Transformer transformer = null;
    private MultivaluedMap<String, String> pathParameters = new MultivaluedMapImpl<>();
    private MultivaluedMap<String, String> queryParameters = new MultivaluedMapImpl<>();
    private List<RESTResource> restResources = new ArrayList<>();
    private List<Object> entities = new ArrayList<>();
    private List<Transition> transitions = new ArrayList<>();
    private Map<String, Object> entityProperties = new HashMap<>();
    private Map<String, Object> uriProperties = new HashMap<>();

    public TransitionPropertiesBuilder(Transformer transformer) {
        this.transformer = transformer;
    }

    TransitionPropertiesBuilder (TransitionPropertiesBuilder other) {
        setTransformer(other.getTransformer());
        addPathParameters(other.getPathParameters());
        addQueryParameters(other.getQueryParameters());
        addRESTResources(other.getRESTResources());
        addEntities(other.getEntities());
        addTransitions(other.getTransitions());
    }

    /**
     * Adds path parameters.
     *
     * @param pathParameters    the path parameters to add
     */
    public TransitionPropertiesBuilder addPathParameters(MultivaluedMap<String, String> pathParameters) {
        if (pathParameters != null) {
            this.pathParameters.putAll(pathParameters);
        }
        return this;
    }

    /**
     * Adds query parameters.
     *
     * @param queryParameters    the query parameters to add
     */
    public TransitionPropertiesBuilder addQueryParameters(MultivaluedMap<String, String> queryParameters) {
        if (queryParameters != null) {
            this.queryParameters.putAll(queryParameters);
        }
        return this;
    }

    /**
     * Adds a {@link RESTResource} object.
     *
     * @param restResource    {@link RESTResource} object to add
     */
    public TransitionPropertiesBuilder addRESTResource(RESTResource restResource) {
        if (restResource != null) {
            this.restResources.add(restResource);
        }
        return this;
    }

    /**
     * Adds an entity object.
     *
     * @param entity    entity object to add
     */
    public TransitionPropertiesBuilder addEntity(Object entity) {
        if (entity != null) {
            this.entities.add(entity);
        }
        return this;
    }

    /**
     * Adds a {@link Transition}.
     *
     * @param transition    {@link Transition} to add
     */
    public TransitionPropertiesBuilder addTransition(Transition transition) {
        if (transition != null) {
            this.transitions.add(transition);
        }
        return this;
    }

    /**
     * Returns path parameters associated with this transition properties.
     *
     * @return  path parameters
     */
    public MultivaluedMap<String, String> getPathParameters() {
        return pathParameters;
    }

    /**
     * Returns query parameters associated with this transition properties.
     *
     * @return  query parameters
     */
    public MultivaluedMap<String, String> getQueryParameters() {
        return queryParameters;
    }

    /**
     * Builds {@link Transition} properties out of any path parameters, query parameters,
     * {@link RESTResource}s, entity objects and {@link Transition}s it has configured.
     *
     * @return  {@link Transition} properties
     */
    public Map<String, Object> build() {
        Map<String, Object> transitionProperties = new HashMap<>();
        transitionProperties.putAll(buildPathProperties());
        transitionProperties.putAll(buildQueryProperties());
        transitionProperties.putAll(buildEntityProperties());
        transitionProperties.putAll(buildUriProperties(transitionProperties));
        return transitionProperties;
    }

    private Transformer getTransformer() {
        return transformer;
    }

    private void setTransformer(Transformer transformer) {
        this.transformer = transformer;
    }

    private List<RESTResource> getRESTResources() {
        return restResources;
    }

    private void addRESTResources(List<RESTResource> restResources) {
        this.restResources.addAll(restResources);
    }

    private List<Object> getEntities() {
        return entities;
    }

    private void addEntities(List<Object> entities) {
        this.entities.addAll(entities);
    }

    private List<Transition> getTransitions() {
        return transitions;
    }

    private void addTransitions(List<Transition> transitions) {
        this.transitions.addAll(transitions);
    }

    private Map<String, Object> buildPathProperties() {
        return toProperties(pathParameters);
    }

    private Map<String, Object> buildQueryProperties() {
        return toProperties(queryParameters);
    }

    private Map<String, Object> buildEntityProperties() {
        for (RESTResource restResource : restResources) {
            if (restResource instanceof EntityResource) {
                buildEntityProperties(((EntityResource) restResource).getEntity());
            } else if (restResource instanceof CollectionResource) {
                for (EntityResource<?> entityResource : ((CollectionResource<?>) restResource).getEntities()) {
                    buildEntityProperties(entityResource.getEntity());
                }
            }
        }
        for (Object entity : entities) {
            buildEntityProperties(entity);
        }
        return entityProperties;
    }

    private Map<String, Object> buildEntityProperties(Object entity) {
        entityProperties.putAll(new EntityPropertiesGenerator(transformer, entity).generate());
        return entityProperties;
    }

    private Map<String, Object> buildUriProperties(Map<String, Object> transitionProperties) {
        for (Transition transition : transitions) {
            uriProperties.putAll(new UriPropertiesGenerator(transition.getCommand().getUriParameters(), transitionProperties).generate());
        }
        return uriProperties;
    }

    private Map<String, Object> toProperties(MultivaluedMap<String, String> parameters) {
        Map<String, Object> properties = new HashMap<>();
        for (String key : parameters.keySet()) {
            properties.put(key, parameters.getFirst(key));
        }
        return properties;
    }

}
