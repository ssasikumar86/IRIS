package com.temenos.useragent.generic.mediatype;

import static com.temenos.useragent.generic.mediatype.JsonUtil.navigateJsonObjectforPropertyPath;
import static com.temenos.useragent.generic.mediatype.PropertyNameUtil.flattenPropertyName;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import com.temenos.useragent.generic.Link;


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

/**
 * To test JSON Utility class
 *
 * @author sathisharulmani
 *
 */
public class JsonUtilTest {

    private final static Supplier<JSONObject> TEST_OBJECT = () -> {
        JSONObject test = new JSONObject();
        JSONObject links = new JSONObject();

        JSONObject link = new JSONObject();
        link.put("href", "self_link_href");
        links.put("self", link);

        link = new JSONObject();
        link.put("href", "input_link_href");
        link.put("name", "Input Customer");
        link.put("title", "Input");
        links.put("http://temenostech.temenos.com/rels/input", link);

        test.put("_links", links);

        test.put("customerId", 100200);
        test.put("balance", -100.40);
        JSONArray overridesArray = new JSONArray();
        overridesArray.put(new JSONObject("{text: \"TEST_OVERRIDE_1\", code: 101}"));
        overridesArray.put(new JSONObject("{text: \"TEST_OVERRIDE_2\", code: 102}"));
        overridesArray.put(new JSONObject("{text: \"TEST_OVERRIDE_3\", code: 103}"));
        overridesArray.put(new JSONObject("{text: \"TEST_OVERRIDE_4\", code: 104}"));
        test.put("overrides", overridesArray);
        test.put("address", new JSONObject("{street: \"TEST_STREET\", city: \"TEST_CITY\", postcode: 12345}"));
        return test;
    };

    private final static Function<String, Optional<JSONObject>> FETCH = (
            String fqPropertyName) -> navigateJsonObjectforPropertyPath(Optional.of(TEST_OBJECT.get()),
                    asList(flattenPropertyName(fqPropertyName)), fqPropertyName, false);

    private final static Function<String, Optional<JSONObject>> CREATE = (
            String fqPropertyName) -> navigateJsonObjectforPropertyPath(Optional.of(TEST_OBJECT.get()),
                    asList(flattenPropertyName(fqPropertyName)), fqPropertyName, true);

    @Test
    public void testExtractLinks() {
        List<Link> links = JsonUtil.extractLinks(TEST_OBJECT.get());
        assertThat(links, notNullValue());
        assertThat(links.size(), equalTo(2));

        Link self = links.get(0);
        assertThat(self.rel(), equalTo("self"));
        assertThat(self.href(), equalTo("self_link_href"));
        assertThat(self.title(), equalTo(""));

        Link input = links.get(1);
        assertThat(input.rel(), equalTo("http://temenostech.temenos.com/rels/input"));
        assertThat(input.href(), equalTo("input_link_href"));
        assertThat(input.title(), equalTo("Input"));
        assertThat(input.description(), equalTo("Input Customer"));
    }

    @Test
    public void testJSONObjectFetchforPropertyPath() {
        Optional<JSONObject> result = FETCH.apply("overrides(2)");
        assertThat(result.isPresent(), equalTo(true));
        assertThat(result.get().get("code"), equalTo(103));
        assertThat(result.get().get("text"), equalTo("TEST_OVERRIDE_3"));

        try {
            result = FETCH.apply("overrides(4)");
            assertThat(result.isPresent(), equalTo(false));
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), equalTo("Unable to resolve index [4] for property path \"overrides\""));
        }

        try {
            result = FETCH.apply("missingpath(4)");
            assertThat(result.isPresent(), equalTo(true));
        } catch (IllegalArgumentException e) {
            fail(e.getMessage());
        }

        result = FETCH.apply("address(0)");
        assertThat(result.isPresent(), equalTo(true));
        assertThat(result.get().get("street"), equalTo("TEST_STREET"));
        assertThat(result.get().get("postcode"), equalTo(12345));
    }

    @Test
    public void testJSONObjectCreateforPropertyPath() {
        Optional<JSONObject> result = CREATE.apply("overrides(2)");
        assertThat(result.isPresent(), equalTo(true));
        assertThat(result.get().get("code"), equalTo(103));
        assertThat(result.get().get("text"), equalTo("TEST_OVERRIDE_3"));

        // +1 to the array length gets created
        try {
            result = CREATE.apply("overrides(4)");
            assertThat(result.isPresent(), equalTo(true));
            assertThat(result.get().toString(), equalTo("{}"));
        } catch (IllegalArgumentException e) {
            fail(e.getMessage());
        }

        // greater than +1 of array length fails
        try {
            result = CREATE.apply("overrides(5)");
            assertThat(result.isPresent(), equalTo(false));
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), equalTo("Unable to resolve index [5] for property path \"overrides\""));
        }

        // +1 to the existing json gets created and parent turns into array
        try {
            result = CREATE.apply("address(1)");
            assertThat(result.isPresent(), equalTo(true));
            assertThat(result.get().toString(), equalTo("{}"));
        } catch (IllegalArgumentException e) {
            fail(e.getMessage());
        }

        // greater than +1 of json object fails
        try {
            result = CREATE.apply("address(2)");
            assertThat(result.isPresent(), equalTo(false));
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), equalTo("Unable to resolve index [2] for property path \"address\""));
        }

        try {
            result = CREATE.apply("missingpath(1)");
            assertThat(result.isPresent(), equalTo(false));
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), equalTo("Unable to resolve index [1] for property path \"missingpath\""));
        }

        // new path gets created
        try {
            result = CREATE.apply("missingpath(0)");
            assertThat(result.isPresent(), equalTo(true));
            assertThat(result.get().toString(), equalTo("{}"));
        } catch (IllegalArgumentException e) {
            fail(e.getMessage());
        }
    }

}
