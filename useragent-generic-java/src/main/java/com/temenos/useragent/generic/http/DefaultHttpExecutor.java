package com.temenos.useragent.generic.http;

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

import java.io.IOException;

import org.apache.commons.io.IOUtils;

import com.temenos.useragent.generic.PayloadHandler;
import com.temenos.useragent.generic.context.ContextFactory;
import com.temenos.useragent.generic.internal.DefaultPayloadWrapper;
import com.temenos.useragent.generic.internal.PayloadHandlerFactory;
import com.temenos.useragent.generic.internal.PayloadResponse;
import com.temenos.useragent.generic.internal.PayloadWrapper;
import com.temenos.useragent.generic.internal.RequestData;
import com.temenos.useragent.generic.internal.ResponseData;

/**
 * Implements a {@link HttpMethodExecutor http method executor} using Apache
 * HttpComponents {@linkplain https://hc.apache.org/}
 * 
 * @author ssethupathi
 *
 */
public class DefaultHttpExecutor implements HttpMethodExecutor {

	private HttpClient httpClient;
	private String url;
	private RequestData input;

	public DefaultHttpExecutor(HttpClient httpClient, String url,
			RequestData input) {
		this.httpClient = httpClient;
		this.url = url;
		this.input = input;
	}

	@Override
	public ResponseData execute(HttpMethod method) {
		switch (method) {
		case GET:
			return get();

		case POST:
			return post();

		case PUT:
			return put();
			
		case DELETE:
	        return delete();
		}
		throw new RuntimeException("Http method '" + method + "' not supported");

	}

	private ResponseData get() {
		HttpRequest request = new HttpRequestImpl(input.header());
		HttpResponse response = httpClient.get(url, request);
		return buildResponse(response);
	}

	private ResponseData post() {
		HttpRequest request = new HttpRequestImpl(input.header(),
				getPayload(input));
		HttpResponse response = httpClient.post(url, request);
		return buildResponse(response);
	}

	private ResponseData put() {
		HttpRequest request = new HttpRequestImpl(input.header(),
				getPayload(input));
		HttpResponse response = httpClient.put(url, request);
		return buildResponse(response);
	}
	
	private ResponseData delete(){
	    HttpRequest request = new HttpRequestImpl(input.header(),
                getPayload(input));
	    HttpResponse response = httpClient.delete(url, request);
	    return buildResponse(response);
	}

	private ResponseData buildResponse(HttpResponse response) {
		String contentType = response.headers().get("Content-Type");
		if(contentType == null)
		    contentType = "";
		PayloadHandlerFactory<?> factory = ContextFactory
				.get()
				.getContext()
				.entityHandlersRegistry()
				.getPayloadHandlerFactory(
				        DefaultHttpClientHelper.removeParameter(contentType)
	             );
		if (factory == null) {
			throw new IllegalStateException(
					"Content type handler factory not registered for content type '"
							+ contentType + "'");
		}
		PayloadHandler handler = factory.createHandler(response.payload());
		handler.setParameter(DefaultHttpClientHelper
				.extractParameter(contentType));
		PayloadWrapper payload = new DefaultPayloadWrapper();
		payload.setHandler(handler);
		PayloadResponse.Builder responseBuilder = new PayloadResponse.Builder(
				response.result());
		responseBuilder.header(response.headers());
		responseBuilder.body(payload);
		return responseBuilder.build();
	}

	private String getPayload(RequestData requestData) {
		String payload = "";
		if (requestData.entity() != null) {
			try {
				payload = IOUtils.toString(input.entity().getContent());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return payload;
	}
}
