package com.temenos.interaction.core;

/*
 * #%L
 * interaction-core
 * %%
 * Copyright (C) 2012 - 2016 Temenos Holdings N.V.
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

import java.net.URI;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple class just to be able to provide the path decoded from utf8 to the providers
 *
 * @author clopes
 *
 */
public class UriInfoImpl implements UriInfo {
    private static final Logger LOGGER = LoggerFactory.getLogger(UriInfoImpl.class);
    private UriInfo uriInfo;
    private MultivaluedMap<String,String> pathParameters = new MultivaluedMapImpl<String>();
    
    public UriInfoImpl(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    @Override
    public String getPath() {
        return uriInfo.getPath();
    }

    @Override
    public String getPath(boolean decode) {
        return uriInfo.getPath(decode);
    }

    @Override
    public List<PathSegment> getPathSegments() {
        return uriInfo.getPathSegments();
    }

    @Override
    public List<PathSegment> getPathSegments(boolean decode) {
        return uriInfo.getPathSegments(decode);
    }

    @Override
    public URI getRequestUri() {
        return uriInfo.getRequestUri();
    }

    @Override
    public UriBuilder getRequestUriBuilder() {
        return uriInfo.getRequestUriBuilder();
    }

    @Override
    public URI getAbsolutePath() {
        return uriInfo.getAbsolutePath();
    }

    @Override
    public UriBuilder getAbsolutePathBuilder() {
        return uriInfo.getAbsolutePathBuilder();
    }

    @Override
    public URI getBaseUri() {
        return uriInfo.getBaseUri();
    }

    @Override
    public UriBuilder getBaseUriBuilder() {
        return uriInfo.getBaseUriBuilder();
    }

    /**
     * Returns the map of path parameters which are un-decoded. This is directly
     * opposite to the behavior of the method it overrides which returns the map
     * of parameters which are decoded.
     * <p>
     * This map <b>should not be</b> added with decoded parameters.
     * </p>
     */
    public MultivaluedMap<String, String> getPathParameters() {
        return this.pathParameters;
    }

    @Override
    public MultivaluedMap<String, String> getPathParameters(boolean decode) {
        MultivaluedMap<String, String> map = new MultivaluedMapImpl<>();
        try {
            Iterator<Entry<String, List<String>>> pathParamIter = this.pathParameters.entrySet().iterator();
            while (pathParamIter.hasNext()) {
                Entry<String, List<String>> entry = pathParamIter.next();
                for (String paramVal : entry.getValue()) {
                    // true => decodes; false => uses original
                    map.add(entry.getKey(), decode ? URLDecoder.decode(paramVal, "UTF-8") : paramVal);
                }
            }
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Failed to decode parameter", e);
            }
        }
        return map;
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters() {
        return uriInfo.getQueryParameters();
    }

    @Override
    public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
        return uriInfo.getQueryParameters(decode);
    }

    @Override
    public List<String> getMatchedURIs() {
        return uriInfo.getMatchedURIs();
    }

    @Override
    public List<String> getMatchedURIs(boolean decode) {
        return uriInfo.getMatchedURIs(decode);
    }

    @Override
    public List<Object> getMatchedResources() {
        return uriInfo.getMatchedResources();
    }
}
