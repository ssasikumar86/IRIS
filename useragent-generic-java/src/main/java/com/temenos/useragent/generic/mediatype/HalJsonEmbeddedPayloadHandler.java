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

import static com.temenos.useragent.generic.mediatype.HalJsonUtil.extractEmbeddedLinks;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.temenos.useragent.generic.Link;
import com.temenos.useragent.generic.PayloadHandler;
import com.temenos.useragent.generic.internal.EntityWrapper;
import com.temenos.useragent.generic.internal.NullEntityWrapper;
import com.theoryinpractise.halbuilder.api.ReadableRepresentation;
import com.theoryinpractise.halbuilder.api.RepresentationFactory;


/**
 * hal+json embedded content payload handler
 *
 * @author sathisharulmani
 *
 */
public class HalJsonEmbeddedPayloadHandler implements PayloadHandler {

    private RepresentationFactory representationFactory = HalJsonUtil.initRepresentationFactory();
    private ReadableRepresentation representation = representationFactory.newRepresentation();

    @Override
    public boolean isCollection() {
        return false;
    }

    @Override
    public List<Link> links() {
        return extractEmbeddedLinks(representation);
    }

    @Override
    public List<EntityWrapper> entities() {
        return new ArrayList<EntityWrapper>();
    }

    @Override
    public EntityWrapper entity() {
        return new NullEntityWrapper();
    }

    @Override
    public void setPayload(String payload) {
        if (payload == null) {
            return;
        }
        ReadableRepresentation jsonRepresentation = representationFactory.readRepresentation(
                RepresentationFactory.HAL_JSON, new InputStreamReader(IOUtils.toInputStream(payload)));
        representation = jsonRepresentation;
    }

    @Override
    public void setParameter(String parameter) {
    }

}
