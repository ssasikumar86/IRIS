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


import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.http.HttpMessage;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.temenos.useragent.generic.Result;
import com.temenos.useragent.generic.context.ConnectionConfig;
import com.temenos.useragent.generic.context.ContextFactory;

/**
 * Helper for the Http Client operations.
 * 
 * @author ssethupathi
 *
 */
public class DefaultHttpClientHelper {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHttpClientHelper.class);

	/**
	 * Builds and returns the {@link HttpMessage http message} with the request
	 * headers.
	 * 
	 * @param request
	 * @param message
	 */
	public static void buildRequestHeaders(HttpRequest request,
			HttpMessage message) {
		HttpHeader header = request.headers();
		for (String name : header.names()) {
			message.addHeader(name, header.get(name));
		}
	}

	/**
	 * Builds and returns the {@link HttpHeader http header} from the response
	 * message.
	 * 
	 * @param response
	 *            message
	 * @param http
	 *            header
	 */

	public static HttpHeader buildResponseHeaders(
			CloseableHttpResponse httpResponse) {
		HttpHeader header = new HttpHeader();
		for (org.apache.http.Header httpHeader : httpResponse.getAllHeaders()) {
			header.set(httpHeader.getName(), httpHeader.getValue());
		}
		return header;
	}

	/**
	 * Builds and returns http interaction execution result.
	 * 
	 * @param httpResponse
	 * @return interaction execution result
	 */
	public static Result buildResult(CloseableHttpResponse httpResponse) {
		StatusLine statusLine = httpResponse.getStatusLine();
		return new HttpResult(statusLine.getStatusCode(),
				statusLine.getReasonPhrase());
	}

	/**
	 * Builds and returns the basic authentication provider.
	 * 
	 * @return
	 */
	public static CredentialsProvider getBasicCredentialProvider() {
		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		ConnectionConfig config = ContextFactory.get().getContext()
				.connectionCongfig();
		credentialsProvider.setCredentials(
				AuthScope.ANY,
				new UsernamePasswordCredentials(config
						.getValue(ConnectionConfig.USER_NAME), config
						.getValue(ConnectionConfig.PASS_WORD)));
		return credentialsProvider;
	}

	/**
	 * Removes the optional parameter part of the content type and returns the
	 * type and subtype part.
	 * <p>
	 * For example, returns <i>application/atom+xml</i> from
	 * <i>application/atom+xml;type=entry</i>
	 * </p>
	 * 
	 * @param content
	 *            type with optional parameter
	 * @return content type without parameter
	 */
	public static String removeParameter(String contentType) {
		int parameterSeparatorIndex = contentType.indexOf(";");
		if (parameterSeparatorIndex > 0) {
			return contentType.substring(0, parameterSeparatorIndex).trim();
		} else {
			return contentType;
		}
	}

	/**
	 * Extracts and returns the optional parameter part of the content type.
	 * <p>
	 * For example, returns <i>type=entry</i> from
	 * <i>application/atom+xml;type=entry</i>
	 * </p>
	 * 
	 * @param content
	 *            type with optional parameter
	 * @return optional parameter, if present
	 */

	public static String extractParameter(String contentType) {
		int parameterStartIndex = contentType.indexOf(";") + 1;
		if (parameterStartIndex > 0
				&& (parameterStartIndex) < contentType.length()) {
			return contentType.substring(parameterStartIndex).trim();
		} else {
			return "";
		}
	}
}
