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

import static com.temenos.useragent.generic.mediatype.JsonUtil.extractLinks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.temenos.useragent.generic.Link;
import com.temenos.useragent.generic.PayloadHandler;
import com.temenos.useragent.generic.internal.DefaultEntityWrapper;
import com.temenos.useragent.generic.internal.EntityWrapper;
import com.temenos.useragent.generic.internal.NullEntityWrapper;


public class JsonPayloadHandler implements PayloadHandler {

    private Optional<Object> jsonResponse = Optional.empty();

    @Override
    public boolean isCollection() {
        Object response = getJsonResponse(Object.class);
        if (response instanceof JSONArray) {
            return true;
        }
        return false;
    }

    @Override
    public List<Link> links() {
        if (!isCollection()) {
            return extractLinks(getJsonResponse(JSONObject.class));
        }
        return Collections.emptyList();
    }

    @Override
    public List<EntityWrapper> entities() {
        List<EntityWrapper> wrappers = new ArrayList<EntityWrapper>();
        if (!isCollection()) {
            return wrappers;
        }
        JSONArray jsonArray = getJsonResponse(JSONArray.class);
        jsonArray.forEach(item -> wrappers.add(createEntityWrapper((JSONObject) item)));
        return wrappers;
    }

    @Override
    public EntityWrapper entity() {
        return !isCollection() ? createEntityWrapper(getJsonResponse(JSONObject.class)) : new NullEntityWrapper();
    }

    @Override
    public void setPayload(String payload) {
        if (payload == null) {
            throw new IllegalArgumentException("Payload is null");
        }
        jsonResponse = Optional.ofNullable((Object) new JSONTokener(payload).nextValue());
    }

    @Override
    public void setParameter(String parameter) {
        // TODO Auto-generated method stub
    }

    private <T> T getJsonResponse(Class<T> klass) {
        return klass.cast(jsonResponse.orElseThrow(() -> new IllegalArgumentException("Invalid JSON response")));
    }

    private EntityWrapper createEntityWrapper(JSONObject jsonObject) {
        EntityWrapper entityWrapper = new DefaultEntityWrapper();
        JsonEntityHandler jsonEntity = new JsonEntityHandler();
        jsonEntity.setContent(IOUtils.toInputStream(jsonObject.toString()));
        entityWrapper.setHandler(jsonEntity);
        return entityWrapper;
    }
}
