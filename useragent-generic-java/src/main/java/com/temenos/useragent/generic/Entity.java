package com.temenos.useragent.generic;

import com.temenos.useragent.generic.internal.Payload;

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


/**
 * Defines an entity which represents an item in the payload.
 * 
 * @author ssethupathi
 *
 */
public interface Entity {

	/**
	 * Returns the id of the entity.
	 * 
	 * @return id
	 */
	String id();

	/**
	 * Returns the value from this {@link Entity entity} for the fully qualified
	 * property name.
	 * 
	 * @param fqName
	 *            property name
	 * @return property value
	 */
	String get(String fqName);

	/**
	 * Returns the number of existence of a property for the specified fully
	 * qualified name in this {@link Entity entity}.
	 * 
	 * @param fqName
	 *            property name
	 * @return count
	 */
	int count(String fqName);

	/**
	 * Returns the {@link Link links} which are part of this {@link Entity
	 * entity}.
	 * 
	 * @return links
	 */
	Links links();
	
	/**
	 * Returns the embedded payload.
	 * 
	 * @return embedded payload or null if this link has no embedded payload
	 */
	Payload embedded();

}