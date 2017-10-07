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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import com.temenos.useragent.generic.Entity;
import com.temenos.useragent.generic.internal.DefaultEntityWrapper;
import com.temenos.useragent.generic.internal.EntityWrapper;
import com.temenos.useragent.generic.internal.NullEntityWrapper;


public class JsonPayloadHandlerTest {

    private JsonPayloadHandler payloadHandler;

    @Test
    public void testforCollectionResponse() {
        initPayloadHandler("/json_collection_with_two_items.json");

        assertThat(payloadHandler.isCollection(), equalTo(true));
        assertThat(payloadHandler.entity(), instanceOf(NullEntityWrapper.class));
        assertThat(payloadHandler.entities().size(), equalTo(2));
        assertThat(payloadHandler.links().size(), equalTo(0));
    }

    @Test
    public void testforItemResponse() {
        initPayloadHandler("/haljson_item_with_all_properties.json");

        assertThat(payloadHandler.isCollection(), equalTo(false));
        assertThat(payloadHandler.entity(), instanceOf(DefaultEntityWrapper.class));
        assertThat(payloadHandler.entities().size(), equalTo(0));
        assertThat(payloadHandler.links().size(), equalTo(4));
        assertEquals("2002", payloadHandler.entity().get("AccountOfficer"));
    }

    @Test
    public void testEntities() {
        initPayloadHandler("/json_collection_with_two_items.json");
        List<EntityWrapper> entities = payloadHandler.entities();
        assertEquals(2, entities.size());
        Entity firstEntity = entities.get(0);
        assertEquals("2002", firstEntity.get("AccountOfficer"));
        assertEquals(2, firstEntity.count("OverrideGroup"));
        assertEquals("FORM XTP NOT RECEIVED", firstEntity.get("OverrideGroup(0)/Override"));
        assertEquals("MEMORANDUM NOT RECEIVED", firstEntity.get("OverrideGroup(1)/Override"));

        Entity secondEntity = entities.get(1);
        assertEquals("2001", secondEntity.get("AccountOfficer"));
        assertEquals(3, secondEntity.count("OverrideGroup"));
        assertEquals("PASSPORT NOT VERIFIED", secondEntity.get("OverrideGroup(0)/Override"));
        assertEquals("FORM NOT RECEIVED", secondEntity.get("OverrideGroup(1)/Override"));
        assertEquals("MEMORANDUM NOT RECEIVED", secondEntity.get("OverrideGroup(2)/Override"));

    }

    private void initPayloadHandler(String jsonFileName) {
        payloadHandler = new JsonPayloadHandler();
        try {
            payloadHandler.setPayload(IOUtils.toString(JsonPayloadHandlerTest.class.getResourceAsStream(jsonFileName)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
