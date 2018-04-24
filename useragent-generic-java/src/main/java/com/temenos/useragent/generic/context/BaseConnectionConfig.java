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


import java.util.Properties;

import org.apache.commons.codec.binary.Base64;

/**
 * This class contains base connection configuration parameters.
 * 
 * @author ssethupathi
 *
 */
public class BaseConnectionConfig implements ConnectionConfig {

	private Properties connectionProperties = new Properties();

	public BaseConnectionConfig() {
		this.connectionProperties = getBaseConnectionProperties();
	}

	@Override
	public String getValue(String name) {
		return connectionProperties.getProperty(name, "");
	}

	private Properties getBaseConnectionProperties() {
		Properties baseConnprops = new Properties();
		baseConnprops
				.setProperty(ConnectionConfig.ENDPOINT_URI,
						"http://localhost:9089/t24interactiontests-iris/t24interactiontests.svc");
		baseConnprops.setProperty(ConnectionConfig.SERVICE_ROOT, "GB0010001");
		baseConnprops.setProperty(ConnectionConfig.USER_NAME, "INPUTT");
		baseConnprops.setProperty(ConnectionConfig.PASS_WORD, Base64.decodeBase64("MTIzNDU2").toString());
		return baseConnprops;
	}
}
