package com.temenos.useragent.generic.internal;

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
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.temenos.useragent.generic.Links;

public class NullEntityWrapper implements EntityWrapper {

	@Override
	public String id() {
		return "";
	}

	@Override
	public String get(String fqName) {
		return "";
	}

	@Override
	public int count(String fqName) {
		return 0;
	}

	@Override
	public Links links() {
		return Links.empty();
	}

	@Override
	public void setHandler(EntityHandler handler) {
		throw new IllegalStateException("Unexpected method call");
	}

	@Override
	public void setSessionContext(SessionContext sessionCallback) {
		throw new IllegalStateException("Unexpected method call");
	}

	@Override
	public void set(String fqPropertyName, String value) {
		throw new IllegalStateException("Unexpected method call");
	}

	@Override
	public void remove(String fqPropertyName) {
		throw new IllegalStateException("Unexpected method call");
	}
	
	@Override
	public Payload embedded() {
		throw new IllegalStateException("Unexpected method call");
	}

	@Override
	public InputStream getContent() {
		try {
			return IOUtils.toInputStream("", "UTF-8");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
