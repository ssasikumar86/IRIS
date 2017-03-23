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


import static org.junit.Assert.*;

import org.apache.http.HttpMessage;
import org.apache.http.client.methods.HttpGet;
import org.junit.Ignore;
import org.junit.Test;

import com.temenos.useragent.generic.http.DefaultHttpClientHelper;
import com.temenos.useragent.generic.http.HttpHeader;
import com.temenos.useragent.generic.http.HttpRequest;
import com.temenos.useragent.generic.http.HttpRequestImpl;

public class DefaultHttpClientHelperTest {

	@Test
	public void testBuildRequestHeaders() {
		HttpHeader requestHeader = new HttpHeader();
		requestHeader.set("Content-Type", "application/atom+xml");
		HttpRequest request = new HttpRequestImpl(requestHeader);
		HttpMessage message = new HttpGet();
		DefaultHttpClientHelper.buildRequestHeaders(request, message);
		assertEquals("application/atom+xml",
				message.getFirstHeader("Content-Type").getValue());
	}

	@Test
	public void testRemoveParameter() {
		String contentType = "foo/bar";
		assertEquals("foo/bar",
				DefaultHttpClientHelper.removeParameter(contentType));
		contentType = "foo/bar;";
		assertEquals("foo/bar",
				DefaultHttpClientHelper.removeParameter(contentType));
		contentType = "foo/bar;a=b";
		assertEquals("foo/bar",
				DefaultHttpClientHelper.removeParameter(contentType));
		contentType = "";
		assertEquals("", DefaultHttpClientHelper.removeParameter(contentType));
	}

	@Test
	public void testExtractParameter() {
		String contentType = "foo/bar";
		assertEquals("", DefaultHttpClientHelper.extractParameter(contentType));
		contentType = "foo/bar;";
		assertEquals("", DefaultHttpClientHelper.extractParameter(contentType));
		contentType = "foo/bar;a=b";
		assertEquals("a=b",
				DefaultHttpClientHelper.extractParameter(contentType));
		contentType = "";
		assertEquals("", DefaultHttpClientHelper.extractParameter(contentType));
	}
}
