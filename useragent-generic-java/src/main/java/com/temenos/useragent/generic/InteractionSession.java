package com.temenos.useragent.generic;

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

import com.temenos.useragent.generic.http.HttpClient;
import com.temenos.useragent.generic.internal.EntityWrapper;

/**
 * Defines a session through which interactions to an IRIS service on behalf of
 * a user agent are performed.
 * 
 * @author ssethupathi
 *
 */
public interface InteractionSession {

	/**
	 * Returns the header value for the name from the current session.
	 * 
	 * 
	 * @param name
	 *            of the header property
	 * @return value of the header property
	 */
	String header(String name);

	/**
	 * Returns the {@link Links links} associated to this session.
	 * 
	 * @return {@link Links links}
	 */
	Links links();

	/**
	 * Returns the {@link Entities entities} associated to this session.
	 * 
	 * @return {@link Entities entities}
	 */
	Entities entities();

	/**
	 * Returns the {@link Result result} of the last interaction performed
	 * through this session.
	 * 
	 * @return {@link Result result}
	 */
	Result result();

	/**
	 * Sets a header name-values pair to this session.
	 * 
	 * @param name
	 *            of the header property
	 * @param values
	 *            to the header property
	 * @return this session
	 */
	InteractionSession header(String name, String... values);

	/**
	 * Registers a {@link PayloadHandler payload handler} to handle the payload
	 * for a media type.
	 * 
	 * @param mediaType
	 *            of the payload
	 * @param handler
	 *            to handle the payload of the media type
	 * @return this session
	 */
	InteractionSession registerHandler(String mediaType,
			Class<? extends PayloadHandler> handler);

	/**
	 * Sets the value to the property in the {@link Entity entity} associated to
	 * this session.
	 * 
	 * @param propertyName
	 * @param propertyValue
	 * @return this session
	 */
	<T> InteractionSession set(String propertyName, T propertyValue);

	/**
	 * Removes the value for the property in the {@link Entity entity}
	 * associated to this session.
	 * 
	 * @param propertyName
	 * @return this session
	 */
	InteractionSession remove(String propertyName);
	
	/**
	 * Returns a {@link Url url} instance associated to this session with any
	 * pre-configuration applied.
	 * 
	 * @return a {@link Url url} instance
	 */
	Url url();

	/**
	 * Returns a {@link Url url} instance associated to this session with the
	 * set url string.
	 * 
	 * @param completeUrl
	 * @return a {@link Url url} instance
	 */
	Url url(String completeUrl);

	/**
	 * Enables reuse of this session for future interactions with the
	 * {@link Entity entity} from the recently completed interaction.
	 * 
	 * @return this session
	 */
	InteractionSession reuse();

	/**
	 * Uses the given {@link Entity entity} for future interactions in this
	 * session.
	 * 
	 * @param {@link Entity entity} to be used in future interactions
	 * @return this session
	 */
	InteractionSession use(EntityWrapper entity);

	/**
	 * Clears this session to reset to it's creation state.
	 * 
	 * @return this session
	 */
	InteractionSession clear();

	/**
	 * Sets user name for the Http Basic Authentication.
	 * 
	 * @param username
	 * @return this session
	 */
	InteractionSession basicAuth(String username, String password);

	/**
	 * Sets a {@linl HttpClient http client} instance to be used by this
	 * session.
	 * 
	 * @param httpClient
	 *            http client
	 */
	void useHttpClient(HttpClient httpClient);
}
