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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.temenos.useragent.generic.Link;
import com.temenos.useragent.generic.Links;

public class DefaultEntityWrapper implements EntityWrapper {

	private List<Link> namedLinks;
	private EntityHandler entityHandler;
	private SessionContext sessionCallback;

	public DefaultEntityWrapper() {
	}

	@Override
	public String id() {
		return entityHandler.getId();
	}

	@Override
	public String get(String fqName) {
		return entityHandler.getValue(fqName);
	}

	@Override
	public int count(String fqName) {
		return entityHandler.getCount(fqName);
	}

	@Override
	public Links links() {
		if (sessionCallback == null) {
			return Links.empty();
		} else {
			return Links.create(entityLinks(), sessionCallback);
		}
	}
	
	@Override
	public Payload embedded() {
		return entityHandler.embedded();
	}

	private List<Link> entityLinks() {
		checkAndBuildLinks();
		return namedLinks;
	}

	private void checkAndBuildLinks() {
		if (namedLinks != null) {
			return;
		}
		List<Link> links = entityHandler.getLinks();
		namedLinks = new ArrayList<Link>();
		for (Link link : links) {
			namedLinks.add(link);
		}
	}

	@Override
	public InputStream getContent() {
		return entityHandler.getContent();
	}

	@Override
	public void set(String fqPropertyName, String value) {
		entityHandler.setValue(fqPropertyName, value);
	}

	@Override
	public void remove(String fqPropertyName) {
		entityHandler.remove(fqPropertyName);
	}

	@Override
	public void setHandler(EntityHandler transformer) {
		this.entityHandler = transformer;
	}

	@Override
	public void setSessionContext(SessionContext sessionCallback) {
		this.sessionCallback = sessionCallback;
	}
}
