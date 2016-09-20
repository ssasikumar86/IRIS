package com.temenos.interaction.core.rim;

/*
 * #%L
 * interaction-core
 * %%
 * Copyright (C) 2012 - 2013 Temenos Holdings N.V.
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


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * This class contains a number of utility methods to manipulate
 * HTTP response headers via a system of ResponseBuilders.
 *
 * @author dgroves
 *
 */
public class HeaderHelper {
    
    private static final Logger logger = LoggerFactory.getLogger(HeaderHelper.class);
    private static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * Add an HTTP Allow header to the response.
     * @param rb
     * @param httpMethods
     * @return
     */
    public static ResponseBuilder allowHeader(ResponseBuilder rb, Set<String> httpMethods) {
    	if (httpMethods != null) {
        	StringBuilder result = new StringBuilder();
        	for (String method : httpMethods) {
                result.append(" ");
                result.append(method);
                result.append(",");
        	}
        	return rb.header("Allow", (result.length() > 0 ? result.substring(1, result.length() - 1) : ""));
    	}
    	return rb;
    }
    
    /**
     * Add an HTTP Location header to the response without query parameters.
     * @param rb
     * @param target
     * @return
     */
    public static ResponseBuilder locationHeader(ResponseBuilder rb, String target) {
    	return locationHeader(rb, target, null);
    }
    
    /**
     * Add an HTTP Location header to the response with query parameters.
     * @param rb
     * @param target
     * @param queryParam
     * @return
     */
    public static ResponseBuilder locationHeader(ResponseBuilder rb, String target, 
            MultivaluedMap<String, String> queryParam) {
        if (target != null && !isNullOrEmpty(queryParam) && target.indexOf("?") != -1) {
            return rb.header(HttpHeaders.LOCATION, target + "&" + encodeMultivalueQueryParameters(queryParam));
        }else if(target != null && !isNullOrEmpty(queryParam)){
            return rb.header(HttpHeaders.LOCATION, target + "?" + encodeMultivalueQueryParameters(queryParam));
        }else if(target != null){
            return rb.header(HttpHeaders.LOCATION, target);
        }else{
            return rb;
        }
    }

    /**
     * Add an E-Tag header
     * @param rb response builder
     * @param entityTag etag
     * @return response builder
     */
    public static ResponseBuilder etagHeader(ResponseBuilder rb, String entityTag) {
    	if (entityTag != null && !entityTag.isEmpty()) {
        	return rb.header(HttpHeaders.ETAG, entityTag);
    	}
    	return rb;
    }
    
    public static ResponseBuilder maxAgeHeader(ResponseBuilder rb, int maxAge) {
    	return rb.header(HttpHeaders.CACHE_CONTROL, "max-age=" + maxAge );
    }
    
    /**
     * Returns the first HTTP header entry for the specified header
     * @param headers HTTP headers
     * @param header header to return
     * @return first header entry
     */
    public static String getFirstHeader(HttpHeaders headers, String header) {
    	if (headers == null) {
    	    return null;
    	}
        List<String> headerList = headers.getRequestHeader(header);
        if(headerList != null && headerList.size() > 0) {
            return headerList.get(0);
        }
    	return null;
    }

    /**
     * Returns the first HTTP header entry for the specified header ignoring case,
     * that is, it does a case insensitive search.
     *
     * @param headers HTTP headers
     * @param header header to return
     * @return first header entry
     */
    public static String getFirstHeaderCaseInsensitive(HttpHeaders headers, String header) {
        if (headers == null) {
            return null;
        }
        MultivaluedMap<String, String> headerMap = headers.getRequestHeaders();
        if (headerMap == null) {
            return null;
        }
        for (String key : headerMap.keySet()) {
            if (key.equalsIgnoreCase(header)) {
                return headerMap.getFirst(key);
            }
        }
        return null;
    }

    /**
     * Encode a MultivaluedMap as URL query parameters, omitting any duplicate
     * key/value pairings.
     * @param requestParameters The query parameters to encode.
     * @return An encoded query string suitable for use with a URL.
     */
    public static String encodeMultivalueQueryParameters(
            MultivaluedMap<String, String> queryParam){
        if(isNullOrEmpty(queryParam)){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int outerIndex = 0;
        List<String> filter = new ArrayList<String>();
        for(Map.Entry<String, List<String>> parameterKeyAndValues : queryParam.entrySet()){
            filterDuplicateQueryKeyValuePairings(parameterKeyAndValues.getValue(), filter);
            String keyAndValue = constructQueryKeyValuePairing(parameterKeyAndValues.getKey(), filter, outerIndex < queryParam.size() - 1);
            sb.append(keyAndValue);
            filter.clear();
            outerIndex++;
        }
        return sb.toString();
    }
    
    private static boolean isNullOrEmpty(MultivaluedMap<String, String> queryParam){
        return queryParam == null || queryParam.size() == 0;
    }
    
    private static void filterDuplicateQueryKeyValuePairings(List<String> src, List<String> dest){
        for(String value : src){
            if(!dest.contains(value)){
                dest.add(value);
            }
        }
        if(dest.isEmpty()){
            dest.add(""); //interpret empty list as a key without a value (e.g. ?x=&y=)
        }
    }
    
    private static String constructQueryKeyValuePairing(String key, List<String> values, boolean appendAmpersand) {
        StringBuilder sb = new StringBuilder();
        int index = 0;
        for(String value : values){
            sb.append(encodeQueryParameter(key, DEFAULT_ENCODING));
            sb.append("=");
            sb.append(encodeQueryParameter(value, DEFAULT_ENCODING));
            if(index < values.size() - 1){
                sb.append("&");
            }
            index++;
        }
        if(appendAmpersand && sb.length() > 0){
            sb.append("&");
        }
        return sb.toString();
    }
    
    private static String encodeQueryParameter(String queryParam, String encoding){
        try{
            return URLEncoder.encode(queryParam, encoding);
        }catch(UnsupportedEncodingException uee){
            logger.error("Unsupported encoding type {} used to encode {}",
                    encoding, queryParam);
            throw new RuntimeException(uee);
        }
    }

}
