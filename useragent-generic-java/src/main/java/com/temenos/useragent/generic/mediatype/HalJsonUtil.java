package com.temenos.useragent.generic.mediatype;

/*
 * #%L
 * useragent-generic-java
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


import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.temenos.useragent.generic.Link;
import com.temenos.useragent.generic.PayloadHandler;
import com.temenos.useragent.generic.context.ContextFactory;
import com.temenos.useragent.generic.internal.DefaultPayloadWrapper;
import com.temenos.useragent.generic.internal.LinkImpl;
import com.temenos.useragent.generic.internal.Payload;
import com.temenos.useragent.generic.internal.PayloadHandlerFactory;
import com.temenos.useragent.generic.internal.PayloadWrapper;
import com.theoryinpractise.halbuilder.api.ReadableRepresentation;
import com.theoryinpractise.halbuilder.api.RepresentationFactory;
import com.theoryinpractise.halbuilder.json.JsonRepresentationReader;
import com.theoryinpractise.halbuilder.json.JsonRepresentationWriter;
import com.theoryinpractise.halbuilder.standard.StandardRepresentationFactory;

/**
 * Utility class for supporting HAL JSON media type handlers.
 * 
 * @author ssethupathi
 *
 */
public class HalJsonUtil {

	/**
	 * Extracts links from {@link ReadableRepresentation representation} and
	 * creates collection of {@link Link link}.
	 * 
	 * @param representation
	 * @return links
	 */
	public static List<Link> extractLinks(ReadableRepresentation representation) {
		if (representation == null) {
			throw new IllegalArgumentException("Invalid representation 'null'");
		}
		List<Link> links = new ArrayList<Link>();
		for (com.theoryinpractise.halbuilder.api.Link halLink : representation
				.getLinks()) {
			links.add(new LinkImpl.Builder(halLink.getHref())
					.title(halLink.getTitle()).rel(halLink.getRel()).build());
		}
		return links;
	}

    /**
     * Extracts embedded content from {@link ReadableRepresentation representation} 
     * and creates collection of {@link Link link}.
     * 
     * @param representation
     * @return links
     */
    public static List<Link> extractEmbeddedLinks(ReadableRepresentation representation) {
        if (representation == null) {
            throw new IllegalArgumentException("Invalid representation 'null'");
        }
        List<Link> links = new ArrayList<Link>();
        representation.getResources().forEach(entry -> {
           Link link = getEmbeddedSelfLink(entry.getKey(), entry.getValue());
           if(link != null){
               links.add(link);
           }
        });
        return links;
    }

	/**
	 * Initialises {@link RepresentationFactory representation factory} for
	 * hal+json media type.
	 * 
	 * @return representation factory
	 */
	public static RepresentationFactory initRepresentationFactory() {
		return new StandardRepresentationFactory().withReader(
				RepresentationFactory.HAL_JSON, JsonRepresentationReader.class)
				.withRenderer(RepresentationFactory.HAL_JSON,
						JsonRepresentationWriter.class);
	}

	/**
	 * Clones the last child of the given {@link JSONArray json array} and
	 * returns the array added with the cloned child.
	 * 
	 * <p>
	 * Cloned child would have all the nested json arrays added but with no json
	 * object with properties.
	 * </p>
	 * 
	 * @param jsonArray
	 * @return json array
	 */
	public static JSONArray cloneLastChild(JSONArray jsonArray) {
		int cloneableObjIdx = jsonArray.length() - 1;
		if (jsonArray.optJSONObject(cloneableObjIdx) != null) {
			JSONObject cloneableObj = jsonArray.optJSONObject(cloneableObjIdx);
			jsonArray.put(cloneJsonObject(cloneableObj));
		} else if (jsonArray.optJSONArray(cloneableObjIdx) != null) {
			JSONArray cloneableArr = jsonArray.optJSONArray(cloneableObjIdx);
			jsonArray.put(cloneJsonArray(cloneableArr));
		}
		return jsonArray;
	}

	// clones the passed in json object and returns the cloned
	private static JSONObject cloneJsonObject(JSONObject jsonObject) {
		String[] propNames = JSONObject.getNames(jsonObject);
		JSONObject newObj = new JSONObject();
		if (propNames != null) {
			for (String name : propNames) {
				if (jsonObject.optJSONObject(name) != null) {
					newObj.put(name,
							cloneJsonObject(jsonObject.optJSONObject(name)));
				} else if (jsonObject.optJSONArray(name) != null) {
					newObj.put(name,
							cloneJsonArray(jsonObject.optJSONArray(name)));
				}
			}
		}
		return newObj;
	}

	// clones the passed in json array and returns thhe cloned
	private static JSONArray cloneJsonArray(JSONArray cloneableArray) {
		JSONArray newArr = new JSONArray();
		for (int index = 0; index < cloneableArray.length(); index++) {
			if (cloneableArray.optJSONObject(index) != null) {
				newArr.put(cloneJsonObject(cloneableArray.optJSONObject(index)));
			} else if (cloneableArray.optJSONArray(index) != null) {
				newArr.put(cloneJsonArray(cloneableArray.optJSONArray(index)));
			}
		}
		return newArr;
	}

    private static Link getEmbeddedSelfLink(String parentRel, ReadableRepresentation embeddedContent) {
        if (embeddedContent != null
                && embeddedContent.getLinks().size() > 0
                && "self".equals(embeddedContent.getLinks().get(0).getRel())) {

            com.theoryinpractise.halbuilder.api.Link halLink = embeddedContent.getLinks().get(0);
            return new LinkImpl.Builder(halLink.getHref())
                    .title(halLink.getTitle())
                    .rel(parentRel)
                    .payload(buildEmbeddedPayload(embeddedContent.toString(RepresentationFactory.HAL_JSON)))
                    .build();
        }
        return null;
    }

    private static Payload buildEmbeddedPayload(String content) {
        PayloadHandlerFactory<? extends PayloadHandler> factory = ContextFactory.get().getContext().entityHandlersRegistry().getPayloadHandlerFactory(
                RepresentationFactory.HAL_JSON);
        PayloadHandler handler = factory.createHandler(content);
        PayloadWrapper wrapper = new DefaultPayloadWrapper();
        wrapper.setHandler(handler);
        return wrapper;
    }
}
