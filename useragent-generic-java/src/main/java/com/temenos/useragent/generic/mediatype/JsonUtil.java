package com.temenos.useragent.generic.mediatype;

/*
 * #%L
 * useragent-generic-java
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

import static com.temenos.useragent.generic.mediatype.PropertyNameUtil.extractIndex;
import static com.temenos.useragent.generic.mediatype.PropertyNameUtil.extractPropertyName;
import static com.temenos.useragent.generic.mediatype.PropertyNameUtil.isPropertyNameWithIndex;
import static java.text.MessageFormat.format;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONObject;

import com.temenos.useragent.generic.Link;
import com.temenos.useragent.generic.internal.LinkImpl;


/**
 * Utility for JSON media-type
 *
 * @author sathisharulmani
 *
 */
public final class JsonUtil {

    private final static String LINKS = "_links";
    private final static String LINK_DESC = "name";
    private final static String LINK_HREF = "href";
    private final static String LINK_TITLE = "title";

    private static BiConsumer<String, String> CHECK_VALID_PATH_PART = (String pathPart, String propertyPath) -> {
        if (!isPropertyNameWithIndex(pathPart)) {
            throw new IllegalArgumentException(
                    format("Invalid part {0} in fully qualified property name {1}", pathPart, propertyPath));
        }
    };

    private static BiConsumer<Integer, String> THROW_INVALID_PATH_EXCEPTION = (Integer index, String propertyPath) -> {
        throw new IllegalArgumentException(
                format("Unable to resolve index [{0}] for property path \"{1}\"", index, propertyPath));
    };

    private static BiFunction<JSONObject, JSONArray, JSONObject> LINK_JSON_OBJECT = (optJson, optArray) -> {
        if (optJson != null) {
            return optJson;
        }
        if (optArray != null && optArray.length() > 0) {
            return optArray.optJSONObject(0);
        }
        return null;
    };

    private JsonUtil() {
    }

    public static List<Link> extractLinks(JSONObject jsonResponse) {
        List<Link> links = new ArrayList<>();
        JSONObject jsonLinks = jsonResponse.optJSONObject(LINKS);
        if (null == jsonLinks) {
            return links;
        }

        // create user-agent links
        String[] rels = JSONObject.getNames(jsonLinks);
        Stream.of(rels).forEach(rel -> {
            JSONObject item = LINK_JSON_OBJECT.apply(jsonLinks.optJSONObject(rel), jsonLinks.optJSONArray(rel));
            if (null == item) {
                return;
            }
            links.add(new LinkImpl.Builder(getValueorEmptyString(item, LINK_HREF)).rel(rel).title(
                    getValueorEmptyString(item, LINK_TITLE)).description(
                            getValueorEmptyString(item, LINK_DESC)).build());
        });
        return links;
    }

    public static Optional<JSONObject> navigateJsonObjectforPropertyPath(Optional<JSONObject> jsonResponse,
            List<String> pathParts, String fqPropertyName, boolean createChild) {
        if (pathParts.isEmpty() || !jsonResponse.isPresent()) {
            return jsonResponse;
        } else {
            String pathPart = pathParts.get(0);
            CHECK_VALID_PATH_PART.accept(pathPart, fqPropertyName);
            Optional<JSONObject> childObject = navigateChild(jsonResponse, extractPropertyName(pathPart),
                    extractIndex(pathPart), createChild);
            return navigateJsonObjectforPropertyPath(childObject, pathParts.subList(1, pathParts.size()),
                    fqPropertyName, createChild);
        }
    }

    public static Optional<JSONObject> navigateChild(Optional<JSONObject> parent, String propertyName, int index,
            boolean createChild) {
        if (parent.isPresent()) {
            // Check and get JSON object
            JSONObject parentJsonObj = parent.get();
            Optional<JSONObject> jsonObj = Optional.ofNullable(parentJsonObj.optJSONObject(propertyName));
            checkValidJsonIndex(jsonObj, index, createChild, propertyName);
            if (jsonObj.isPresent() && index == 0) {
                return jsonObj;
            }

            // Check and get JSON array -> JSON Object
            Optional<JSONArray> jsonArray = Optional.ofNullable(parentJsonObj.optJSONArray(propertyName));
            checkValidJsonIndex(jsonArray, index, createChild, propertyName);
            if (jsonArray.isPresent() && !createChild) {
                return Optional.ofNullable(jsonArray.get().optJSONObject(index));
            }

            // invalid path/object
            if (createChild && index != 0 && !jsonArray.isPresent() && !jsonObj.isPresent()) {
                THROW_INVALID_PATH_EXCEPTION.accept(index, propertyName);
            }

            // add to existing JSON array
            if (createChild && jsonArray.isPresent() && index == jsonArray.get().length()) {
                JSONObject newObject = new JSONObject();
                jsonArray.get().put(index, newObject);
                return Optional.of(newObject);
            }

            // get from existing JSON array
            if (createChild && jsonArray.isPresent() && index < jsonArray.get().length()) {
                return Optional.of((JSONObject) jsonArray.get().get(index));
            }

            // add new JSON item and update parent to JSON array
            if (createChild && jsonObj.isPresent() && index == 1) {
                JSONObject newObject = new JSONObject();
                JSONArray newArray = new JSONArray();
                newArray.put(jsonObj.get());
                newArray.put(newObject);
                parent.get().put(propertyName, newArray);
                return Optional.of(newObject);
            }

            if (createChild) {
                JSONObject newObject = new JSONObject();
                parentJsonObj.put(propertyName, newObject);
                return Optional.of(newObject);
            }

            Optional.empty();
        }
        return parent;

    }

    public static void checkValidJsonIndex(@SuppressWarnings("rawtypes") Optional optional, int index,
            boolean createChild, String propertyName) {
        if (optional.isPresent() && optional.get() instanceof JSONObject) {
            // While creating child, check if the index is not greater than 1
            if (createChild && index > 1) {
                THROW_INVALID_PATH_EXCEPTION.accept(index, propertyName);
            }
        }

        if (optional.isPresent() && optional.get() instanceof JSONArray) {
            // While creating child, check if the index is not greater than the array length by 1
            JSONArray jsonArray = (JSONArray) optional.get();
            if (createChild && index > jsonArray.length()) {
                THROW_INVALID_PATH_EXCEPTION.accept(index, propertyName);
            }
        }
    }

    private static String getValueorEmptyString(JSONObject jsonObject, String key) {
        return jsonObject.optString(key, "");
    }
}
