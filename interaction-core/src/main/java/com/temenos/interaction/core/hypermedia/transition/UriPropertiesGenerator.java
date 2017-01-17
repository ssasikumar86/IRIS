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


import com.temenos.interaction.core.hypermedia.HypermediaTemplateHelper;
import com.temenos.interaction.core.hypermedia.Transition;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;


/**
 * Generates properties out of URI parameters and {@link Transition} properties.
 *
 * @author ikarady
 */
public class UriPropertiesGenerator implements PropertiesGenerator {

    private Map<String, String> uriParameters;
    private Map<String, Object> transitionProperties;

    public UriPropertiesGenerator(Map<String, String> uriParameters, Map<String, Object> transitionProperties) {
        this.uriParameters = uriParameters;
        this.transitionProperties = transitionProperties;
    }

    /**
     * Generates properties out of its URI parameters using
     * {@link Transition} properties to replace any placeholders.
     *
     * @return properties
     */
    @Override
    public Map<String, Object> generate() {
        Map<String, Object> properties = new HashMap<>();
        if (uriParameters != null) {
            for (String key : uriParameters.keySet()) {
                String value = uriParameters.get(key);
                value = HypermediaTemplateHelper.templateReplace(value, transitionProperties);
                if (!StringUtils.isEmpty(value)) {
                    properties.put(key, value);
                }
            }
        }
        return properties;
    }
}
