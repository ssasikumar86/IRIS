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


import static com.temenos.useragent.generic.mediatype.HalJsonUtil.initRepresentationFactory;
import static com.temenos.useragent.generic.mediatype.PropertyNameUtil.extractIndex;
import static com.temenos.useragent.generic.mediatype.PropertyNameUtil.extractPropertyName;
import static com.temenos.useragent.generic.mediatype.PropertyNameUtil.flattenPropertyName;
import static com.temenos.useragent.generic.mediatype.PropertyNameUtil.isPropertyNameWithIndex;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.temenos.useragent.generic.Link;
import com.temenos.useragent.generic.internal.EntityHandler;
import com.theoryinpractise.halbuilder.api.ReadableRepresentation;
import com.theoryinpractise.halbuilder.api.Representation;
import com.theoryinpractise.halbuilder.api.RepresentationFactory;

/**
 * An {@link EntityHandler entity handler} implementation for
 * <i>application/hal+json</i> media type.
 * <p>
 * 
 * @see <a href="https://tools.ietf.org/html/draft-kelly-json-hal-08"</a> JSON
 *      Hypertext Application Language
 *      </p>
 * @author ssethupathi
 *
 */

public class HalJsonEntityHandler implements EntityHandler {

	private ReadableRepresentation representation;
	private JSONObject jsonObject;

	@Override
	public String getId() {
		return "";
	}

	@Override
	public List<Link> getLinks() {
		return HalJsonUtil.extractLinks(representation);
	}

	@Override
	public String getValue(String fqPropertyName) {
		String[] pathParts = flattenPropertyName(fqPropertyName);
		JSONObject parent = identifyParentProperty(fqPropertyName, pathParts);
		if (parent != null) {
			String parentPropertyName = pathParts[pathParts.length - 1];
			return parent.optString(parentPropertyName, null);
		}
		return null;
	}

	@Override
	public void setValue(String fqPropertyName, String value) {
		checkAndInitialiseEntity();
		String[] pathParts = flattenPropertyName(fqPropertyName);
		JSONObject parent = checkAndCreateParent(fqPropertyName, pathParts);
		if (parent != null) {
			String parentPropertyName = pathParts[pathParts.length - 1];
			parent.put(parentPropertyName, value);
		}
	}

	@Override
	public int getCount(String fqPropertyName) {
		String[] pathParts = flattenPropertyName(fqPropertyName);
		JSONObject parent = identifyParentProperty(fqPropertyName, pathParts);
		if (parent != null) {
			String parentPropertyName = pathParts[pathParts.length - 1];
			if (parent.optJSONArray(parentPropertyName) != null) {
				return parent.optJSONArray(parentPropertyName).length();
			}
		}
		return 0;
	}

	@Override
	public void remove(String fqPropertyName) {
		String[] pathParts = flattenPropertyName(fqPropertyName);
		JSONObject parent = identifyParentProperty(fqPropertyName, pathParts);
		if (parent == null) {
			return;
		}
		String childName = pathParts[pathParts.length - 1];
		if (isPropertyNameWithIndex(childName)) {
			JSONArray jsonArr = parent
					.optJSONArray(extractPropertyName(childName));
			if (jsonArr != null) {
				jsonArr.remove(extractIndex(childName));
			}
		} else {
			parent.remove(childName);
		}
	}

	@Override
	public void setContent(InputStream stream) {
		if (stream == null) {
			throw new IllegalArgumentException("Input stream is null");
		}
		String content = null;
		try {
			content = IOUtils.toString(stream);
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
		RepresentationFactory factory = initRepresentationFactory();
		representation = factory.readRepresentation(
				RepresentationFactory.HAL_JSON, new StringReader(content));
		jsonObject = new JSONObject(content);
	}

	@Override
	public InputStream getContent() {
		RepresentationFactory factory = initRepresentationFactory();
		Representation r = factory.newRepresentation();
		Map<String,Object> root = new TreeMap<String,Object>();
		addAllChildren(factory, root, null, jsonObject);
		for (String key : root.keySet()) {
			r.withProperty(key, root.get(key));
		}
		
		return IOUtils.toInputStream(r.toString(
				RepresentationFactory.HAL_JSON,
				RepresentationFactory.PRETTY_PRINT));
	}

	private void addAllChildren(RepresentationFactory factory,
			Map<String, Object> parent, String name, Object jObject) {
		if (jObject != null && jObject instanceof JSONArray) {
			addJSONArrayChildren(factory, parent, name, (JSONArray) jObject);
		} else if (jObject != null && jObject instanceof JSONObject) {
			addJSONObjectChildren(factory, parent, name, (JSONObject) jObject);
		} else {
			parent.put(name, jObject);
		}
	}

	private void addJSONObjectChildren(RepresentationFactory factory,
			Map<String, Object> parent, String name, JSONObject jsonObject) {
		String[] fieldNames = JSONObject.getNames(jsonObject);
		if (fieldNames == null) {
			return;
		}
		Map<String, Object> children = new TreeMap<String, Object>();
		for (String fieldName : fieldNames) {
			addAllChildren(factory, children, fieldName,
					(jsonObject).get(fieldName));
		}
		if (name == null) {
			parent.putAll(children);
		} else {
			parent.put(name, children);
		}
	}

	private void addJSONArrayChildren(RepresentationFactory factory,
			Map<String, Object> parent, String name, JSONArray jsonArray) {
		List<Object> jObjects = new ArrayList<Object>();
		for (int i = 0; i < jsonArray.length(); i++) {
			Map<String, Object> children = new TreeMap<String, Object>();
			if (jsonArray.get(i) != null && jsonArray.get(i) instanceof JSONArray) {
				addAllChildren(factory, children, null, jsonArray.getJSONArray(i));
			} else {
				addAllChildren(factory, children, null, jsonArray.getJSONObject(i));
			}
			if (!children.isEmpty()) {
				jObjects.add(children);
			}
		}
		if (!jObjects.isEmpty()) {
			parent.put(name, jObjects);
		}
	}
		
	// identify the existing last parent in the path
	private JSONObject identifyParentProperty(String fqPropertyName,
			String[] pathParts) {
		int pathIndex = 0;
		JSONObject parent = jsonObject;
		while (pathIndex < (pathParts.length - 1)) {
			String pathPart = pathParts[pathIndex];
			ensurePropertyNameIsWithIndex(pathPart, fqPropertyName);
			parent = identifySpecificChild(parent,
					extractPropertyName(pathPart), extractIndex(pathPart));
			pathIndex++;
		}
		return parent;
	}

	// identify the existing specific/indexed child
	private JSONObject identifySpecificChild(Object parent,
			String propertyName, int index) {
		if (parent instanceof JSONArray) {
			return ((JSONArray) parent).optJSONObject(index);
		} else if (parent instanceof JSONObject) {
			JSONObject parentJsonObj = (JSONObject) parent;
			JSONObject jsonObj = parentJsonObj.optJSONObject(propertyName);
			if (jsonObj == null) {
				if (parentJsonObj.optJSONArray(propertyName) != null) {
					return parentJsonObj.optJSONArray(propertyName)
							.optJSONObject(index);
				}
			}
			return jsonObj;
		} else if (parent == null) {
			return null;
		}
		throw new RuntimeException("Unexpected type "
				+ parent.getClass().getName());
	}

	// identify the existing last parent or create new in the path
	private JSONObject checkAndCreateParent(String fqPropertyName,
			String[] pathParts) {
		int pathIndex = 0;
		JSONObject parent = jsonObject;
		while (pathIndex < (pathParts.length - 1)) {
			String pathPart = pathParts[pathIndex];
			ensurePropertyNameIsWithIndex(pathPart, fqPropertyName);
			parent = checkAndCreateChild(parent, extractPropertyName(pathPart),
					extractIndex(pathPart));
			pathIndex++;
		}
		return parent;
	}

	// identify the existing specific/indexed child or create new
	private JSONObject checkAndCreateChild(JSONObject parent,
			String propertyName, int index) {
		JSONArray jsonArr = getValidJsonArr(parent, propertyName, index);
		if (jsonArr.length() == 0) {
			jsonArr.put(new JSONObject());
		} else if (jsonArr.length() == index) {
			jsonArr = HalJsonUtil.cloneLastChild(jsonArr);
		}
		return jsonArr.getJSONObject(index);
	}
	
	private JSONArray getValidJsonArr(JSONObject parent, String propertyName, int index) {
		JSONArray jsonArr = parent.optJSONArray(propertyName);
		if (jsonArr == null) {
			parent.put(propertyName, new JSONArray());
		}
		jsonArr = parent.getJSONArray(propertyName);
		if (index > jsonArr.length()) {
			throw new IllegalArgumentException(
					"Invalid index '"
							+ index
							+ "' to set value to. Only supported indexs are between '0' and '"
							+ jsonArr.length() + "'");
		}
		return jsonArr;
	}

	private void ensurePropertyNameIsWithIndex(String elementName,
			String fqPropertyName) {
		if (!isPropertyNameWithIndex(elementName)) {
			throw new IllegalArgumentException("Invalid part '" + elementName
					+ "' in fully qualified property name '" + fqPropertyName
					+ "'");
		}
	}
	
	private void checkAndInitialiseEntity() {
		if (jsonObject == null) {
			jsonObject = new JSONObject();
		}
	}
}
