package com.temenos.interaction.example.hateoas.banking;

import java.security.SecureRandom;

/*
 * #%L
 * interaction-example-hateoas-banking
 * %%
 * Copyright (C) 2012 - 2013 Temenos Holdings N.V.
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


import java.util.Date;

import javax.ws.rs.HttpMethod;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.temenos.interaction.core.command.InteractionCommand;
import com.temenos.interaction.core.command.InteractionContext;
import com.temenos.interaction.core.resource.EntityResource;

public class POSTFundTransferCommand implements InteractionCommand {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(POSTFundTransferCommand.class);

	/* Implement InteractionCommand interface */

	@Override
	public Result execute(InteractionContext ctx) {
		assert(ctx != null);
		assert(ctx.getId() == null || "".equals(ctx.getId()));

		Long key = Math.abs(new SecureRandom().nextLong() % Long.MAX_VALUE);
		Date now = new Date();
		String json = "";
		json += "{";
		json += "  \"_links\" : {";
		json += "    \"self\" : { \"href\" : \"http://localhost:8080/example/api/fundtransfers/new\" }";
		json += "  },";
		json += "  \"id\" : \"" + key + "\",";
		json += "  \"body\" : \"" + now + "\"";
		json += "}";
		EntityResource<FundTransfer> er = null;
		try {
			JSONObject jsonObject = new JSONObject(json);
			er = new EntityResource<FundTransfer>(new FundTransfer(key, jsonObject.toString()));
		}
		catch(JSONException je) {
			LOGGER.error("Error passing JSON Object to Entity", je);
		}
		ctx.setResource(er);
		return Result.SUCCESS;
	}

	public String getMethod() {
		return HttpMethod.POST;
	}

}
