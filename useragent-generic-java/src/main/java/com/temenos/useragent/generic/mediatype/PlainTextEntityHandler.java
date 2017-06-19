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


import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;

import com.temenos.useragent.generic.Link;
import com.temenos.useragent.generic.internal.EntityHandler;
import com.temenos.useragent.generic.internal.Payload;

/**
 * An entity handler implementation for <i>text/plain</i> media type.
 * 
 * @author ssethupathi
 *
 */
public class PlainTextEntityHandler implements EntityHandler {

	private String plainText;

	public PlainTextEntityHandler(String plainText) {
		this.plainText = plainText;
	}

	@Override
	public String getId() {
		return "";
	}

	@Override
	public List<Link> getLinks() {
		return Collections.emptyList();
	}

	@Override
	public String getValue(String fqPropertyName) {
		return "";
	}

	@Override
	public void setValue(String fqPropertyName, String value) {
		// do nothing
	}
	
	@Override
	public void remove(String fqPropertyName) {
		// do nothing
	}
	
	@Override
	public Payload embedded() {
		return null;
	}
	
	@Override
	public int getCount(String fqPropertyName) {
		return 0;
	}

	@Override
	public void setContent(InputStream stream) {
		// do nothing
	}

	@Override
	public InputStream getContent() {
		return IOUtils.toInputStream(plainText);
	}
}
