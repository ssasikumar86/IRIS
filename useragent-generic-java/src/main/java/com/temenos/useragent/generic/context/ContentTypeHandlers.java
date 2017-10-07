package com.temenos.useragent.generic.context;

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


import java.util.HashMap;
import java.util.Map;

import com.temenos.useragent.generic.PayloadHandler;
import com.temenos.useragent.generic.internal.PayloadHandlerFactory;

/**
 * This class contains the content handlers for the registered media types.
 * 
 * @author ssethupathi
 *
 */
public class ContentTypeHandlers {

	private Map<String, PayloadHandlerFactory<? extends PayloadHandler>> payloadHandlersFactory;

	public ContentTypeHandlers() {
		this.payloadHandlersFactory = new HashMap<String, PayloadHandlerFactory<? extends PayloadHandler>>();
	}

	public void registerForPayload(String contentType,
			final Class<? extends PayloadHandler> transformerClass) {
		payloadHandlersFactory.put(contentType,
				PayloadHandlerFactory.createFactory(transformerClass));
	}

    public void registerForPayloadifMissing(String contentType, final Class<? extends PayloadHandler> transformerClass) {
        if(!payloadHandlersFactory.containsKey(contentType)){
            registerForPayload(contentType, transformerClass);
        }
    }

	public PayloadHandlerFactory<? extends PayloadHandler> getPayloadHandlerFactory(
			String contentType) {
		return payloadHandlersFactory.get(contentType);
	}
}
