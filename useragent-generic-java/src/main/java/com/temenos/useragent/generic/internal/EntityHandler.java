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
import java.util.List;

import com.temenos.useragent.generic.Link;

/**
 * Defines a handler for {@link Entity entity} so entities in different media
 * types are registered and handled in a generic way.
 * 
 * @author ssethupathi
 *
 */
public interface EntityHandler {

	/**
	 * Returns the id for from the underlying entity type.
	 * 
	 * @return id
	 */
	String getId();

	/**
	 * Returns the links from the underlying entity type.
	 * 
	 * @return links
	 */
	List<Link> getLinks();

	/**
	 * Returns the text value for the fully qualified property name which is
	 * part of the underlying entity content.
	 * <p>
	 * For entity types which do not have structured contents such as XML, json
	 * or with no content for the property will return empty string.
	 * </p>
	 * 
	 * @param fqPropertyName
	 * @return property value
	 */
	String getValue(String fqPropertyName);

	/**
	 * Sets the text value for the fully qualified property name in the
	 * underlying entity.
	 * <p>
	 * This method on entity types which do not have structured contents such as
	 * XML, json or with no possible content for the property will have no
	 * effect.
	 * </p>
	 * 
	 * @param fqPropertyName
	 * @param value
	 */
	void setValue(String fqPropertyName, String value);

    /**
     * Sets the primitive data-type value for the fully qualified property name in the underlying entity.
     * <p>
     * Default implementation calls {@link #setValue(String, String)}
     * </p>
     * 
     * @param fqPropertyName
     * @param value
     */
    default <T> void setPrimitiveValue(String fqPropertyName, T value) {
        setValue(fqPropertyName, value != null ? String.valueOf(value) : null);
    }

	/**
	 * Returns the count for the fully qualified property name which is part of
	 * the underlying entity content.
	 * <p>
	 * For entity types which do not have structured contents such as XML, json
	 * or with no content for the property will return 0.
	 * </p>
	 * 
	 * @param fqPropertyName
	 * @return count
	 */

	int getCount(String fqPropertyName);

	/**
	 * Removes the property for the given fully qualified property name from the
	 * underlying entity content.
	 * 
	 * @param fqPropertyName
	 */
	void remove(String fqPropertyName);

	/**
	 * Sets the content for the underlying entity type.
	 * 
	 * @param stream
	 */
	void setContent(InputStream stream);

	/**
	 * Returns the content of the underlying entity.
	 * 
	 * @return content
	 */
	InputStream getContent();
	
	/**
	 * Returns the embedded payload of the underlying entity.
	 * 
	 * @return embedded payload or null if the underlying entity has no embedded
	 *         payload
	 */
	Payload embedded();

}
