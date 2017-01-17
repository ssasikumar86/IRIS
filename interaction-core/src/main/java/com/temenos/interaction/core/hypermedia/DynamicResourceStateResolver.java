package com.temenos.interaction.core.hypermedia;

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
import com.temenos.interaction.core.hypermedia.transition.TransitionPropertiesBuilder;
import com.temenos.interaction.core.resource.RESTResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MultivaluedMap;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Resolves a {@link DynamicResourceState} into real {@link ResourceState}
 * using a {@link ResourceLocatorProvider}.
 * Additional properties can be specified for resolving resource aliases.
 * An optional {@link ResourceParameterResolverProvider} can be used to resolve any parameters
 * associated with end {@link ResourceState}.
 * Path- and query parameters can be added that get updated with newly resolved parameters.
 *
 * @author ikarady
 */
public class DynamicResourceStateResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicResourceStateResolver.class);

    private DynamicResourceState dynamicState = null;
    private ResourceStateAndParameters result = null;
    private ResourceLocatorProvider resourceLocatorProvider = null;
    private ResourceParameterResolverProvider parameterResolverProvider = null;
    private Map<String, Object> properties = new HashMap<>();
    private MultivaluedMap<String, String> pathParameters = new MultivaluedMapImpl<>();
    private MultivaluedMap<String, String> queryParameters = new MultivaluedMapImpl<>();

    public DynamicResourceStateResolver(DynamicResourceState dynamicState, ResourceLocatorProvider resourceLocatorProvider) {
        this.dynamicState = dynamicState;
        this.resourceLocatorProvider = resourceLocatorProvider;
    }

    public DynamicResourceStateResolver addProperties(Map<String, Object> properties) {
        this.properties.putAll(properties);
        return this;
    }

    public DynamicResourceStateResolver addPathParameters(MultivaluedMap<String, String> pathParameters) {
        this.pathParameters.putAll(pathParameters);
        return this;
    }

    public DynamicResourceStateResolver addQueryParameters(MultivaluedMap<String, String> queryParameters) {
        this.queryParameters.putAll(queryParameters);
        return this;
    }

    public DynamicResourceStateResolver setParameterResolverProvider(ResourceParameterResolverProvider parameterResolverProvider) {
        this.parameterResolverProvider = parameterResolverProvider;
        return this;
    }

    /**
     * Returns path parameters containing any parameters associated with end {@link ResourceState}.
     *
     * @return  path parameters
     */
    public MultivaluedMap<String, String> getPathParameters() {
        return pathParameters;
    }

    /**
     * Returns query parameters containing any parameters associated with end {@link ResourceState}.
     *
     * @return  query parameters
     */
    public MultivaluedMap<String, String> getQueryParameters() {
        return queryParameters;
    }

    /**
     * Attempts to resolve its {@link DynamicResourceState} into a real {@link ResourceState}.
     * Uses its properties to resolve resource aliases.
     * If {@link ResourceParameterResolverProvider} is specified it also resolves any parameters
     * associated with end {@link ResourceState}. These parameters are then fed into its
     * path- and query parameters.
     *
     * @return {@link ResourceStateAndParameters}
     */
    public ResourceStateAndParameters resolve() {
        Object[] aliases = getResourceAliases(dynamicState).toArray();
        ResourceLocator locator = resourceLocatorProvider.get(dynamicState.getResourceLocatorName());
        ResourceState resourceState = locator.resolve(aliases);
        if (resourceState == null) {
            LOGGER.warn("Failed to resolve resource using resource locator ", dynamicState.getResourceLocatorName());
            return null;
        }

        result = new ResourceStateAndParameters();
        result.setState(resourceState);
        if (parameterResolverProvider != null) {
            try {
                ResourceParameterResolver parameterResolver = parameterResolverProvider.get(dynamicState.getResourceLocatorName());
                ResourceParameterResolverContext context = new ResourceParameterResolverContext(dynamicState.getEntityName());
                ParameterAndValue[] paramsAndValues = resolveParameterValues(parameterResolver.resolve(aliases, context));
                MultivaluedMap<String, String> stateParameters = toParameters(paramsAndValues);
                addPathParameters(filterParameters(stateParameters, pathParameters.keySet()));
                addQueryParameters(stateParameters);
                result.setParams(paramsAndValues);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Failed to find parameter resolver for resource locator ", dynamicState.getResourceLocatorName(), e);
            }
        }
        return result;
    }

    private List<Object> getResourceAliases(DynamicResourceState dynamicResourceState) {
        List<Object> aliases = new ArrayList<Object>();

        final Pattern pattern = Pattern.compile("\\{*([a-zA-Z0-9.]+)\\}*");

        for (String resourceLocatorArg : dynamicResourceState.getResourceLocatorArgs()) {
            Matcher matcher = pattern.matcher(resourceLocatorArg);
            matcher.find();
            String key = matcher.group(1);
            if (properties.containsKey(key)) {
                aliases.add(properties.get(key));
            } else {
                aliases.add(key);
            }
        }

        return aliases;
    }

    private ParameterAndValue[] resolveParameterValues(ParameterAndValue[] parameterAndValues) {
        if (parameterAndValues == null || parameterAndValues.length == 0) {
            return parameterAndValues;
        }
        ParameterAndValue[] result = new ParameterAndValue[parameterAndValues.length];
        for (int i = 0; i < parameterAndValues.length; i++) {
            String value = HypermediaTemplateHelper.templateReplace(parameterAndValues[i].getValue(), properties);
            result[i] = new ParameterAndValue(parameterAndValues[i].getParameter(), value);
        }
        return result;
    }

    private MultivaluedMap<String, String> filterParameters(MultivaluedMap<String, String> parameters, Set<String> filterKeys) {
        MultivaluedMap<String, String> filteredParameters = new MultivaluedMapImpl<>();
        if (filterKeys == null) {
            return filteredParameters;
        }
        for (String filterKey : filterKeys) {
            if (parameters.containsKey(filterKey)) {
                filteredParameters.put(filterKey, parameters.get(filterKey));
            }
        }
        return filteredParameters;
    }

    private MultivaluedMap<String, String> toParameters(ParameterAndValue[] paramsAndValues) {
        return ParameterAndValue.getParamAndValueAsMultiValueMap(paramsAndValues);
    }

}
