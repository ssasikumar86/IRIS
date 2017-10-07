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


import com.temenos.useragent.generic.mediatype.AtomPayloadHandler;
import com.temenos.useragent.generic.mediatype.HalJsonPayloadHandler;
import com.temenos.useragent.generic.mediatype.JsonPayloadHandler;
import com.temenos.useragent.generic.mediatype.PlainTextPayloadHandler;

/**
 * Factory for accessing the {@link Context context}.
 * 
 * @author ssethupathi
 *
 */
public class ContextFactory {

	private static ThreadLocal<ContextFactory> currentContextFactory = new ThreadLocal<ContextFactory>() {
		@Override
		protected ContextFactory initialValue() {
			return new ContextFactory();
		}
	};

	private ContextImpl context;

	private ContextFactory() {
		BaseConnectionConfig baseConnConfig = new BaseConnectionConfig();
		SystemConnectionConfig sysConnConfig = new SystemConnectionConfig(
				baseConnConfig);
		context = new ContextImpl(sysConnConfig);
	}

	/**
	 * Returns the {@link ContextFactory context factory} associated to the
	 * current thread.
	 * 
	 * @return context factory
	 */
	public static ContextFactory get() {
		return currentContextFactory.get();
	}

	/**
	 * Sets the connection property for the session.
	 * 
	 * @param name
	 * @param value
	 */
	public void setConnectionProperty(String name, String value) {
		context.setSessionProperty(name, value);
	}

	/**
	 * Returns the execution {@link Context context}.
	 * 
	 * @return context
	 */
	public Context getContext() {
		return context;
	}

	public static class ContextImpl implements Context {
		private SessionConnectionConfig connectionConfig;
		private ContentTypeHandlers registry = new ContentTypeHandlers();
		private ContextImpl(ConnectionConfig connConfig) {
			this.connectionConfig = new SessionConnectionConfig(connConfig);
		}

		@Override
		public ConnectionConfig connectionCongfig() {
			return connectionConfig;
		}

		@Override
		public ContentTypeHandlers entityHandlersRegistry() {

			registry.registerForPayloadifMissing("application/atom+xml",
					AtomPayloadHandler.class);
			registry.registerForPayloadifMissing("text/plain", PlainTextPayloadHandler.class);
			registry.registerForPayloadifMissing("text/html", PlainTextPayloadHandler.class);
			registry.registerForPayloadifMissing("", PlainTextPayloadHandler.class);
			registry.registerForPayloadifMissing("application/hal+json", HalJsonPayloadHandler.class);
			registry.registerForPayloadifMissing("application/json", JsonPayloadHandler.class);
			return registry;
		}

		private void setSessionProperty(String name, String value) {
			connectionConfig.setValue(name, value);
		}
	}
}
