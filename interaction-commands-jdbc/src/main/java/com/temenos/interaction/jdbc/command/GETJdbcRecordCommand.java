package com.temenos.interaction.jdbc.command;

/*
 * Jdbc command. 
 * 
 * Given an enquiry constructs a Jdbc request and recovers the requested information from a JDBCProducer.
 */

/*
 * #%L
 * interaction-commands-jdbc
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

import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.temenos.interaction.authorization.command.AuthorizationAttributes;
import com.temenos.interaction.core.command.InteractionContext;
import com.temenos.interaction.core.command.InteractionException;
import com.temenos.interaction.core.entity.Entity;
import com.temenos.interaction.core.resource.EntityResource;
import com.temenos.interaction.jdbc.producer.JdbcProducer;

public class GETJdbcRecordCommand implements JdbcCommand {
	private final static Logger logger = LoggerFactory.getLogger(GETJdbcRecordCommand.class);

	// Somewhere to store the producer.
	JdbcProducer producer;

	public GETJdbcRecordCommand(JdbcProducer producer) {
		this.producer = producer;
	}

	/*
	 * Execute the command.
	 * 
	 * If there is any form of internal error during authorization this will
	 * throw and nothing should be returned to the use.
	 */
	@Override
	public Result execute(InteractionContext ctx) throws InteractionException {

		// Unpack interaction context parameters,
		String entityType = ctx.getCurrentState().getEntityName();

		// For raw command table name is the same as the entity type.
		String tableName = entityType;

		String key = ctx.getId();

		// Get data from JDBC
		EntityResource<Entity> result = null;
		try {
			result = producer.queryEntity(tableName, key, ctx, entityType);
		} catch (InteractionException e) {
			logger.error("Jdbc query failed. " + e);

			if (Status.NOT_FOUND == e.getHttpStatus()) {
				// Not found is just a failure.
				return Result.FAILURE;
			}

			// Other Interaction exceptions should thrown up to the framework.
			throw e;
		} catch (Exception e) {
			logger.error("Jdbc query failed with internal error. " + e);

			// Wrap other exception in an InteractionException and throw to
			// framework.
			throw (new InteractionException(Status.INTERNAL_SERVER_ERROR, e));
		}

		// Write result into context
		ctx.setResource(result);

		// Indicate that database level filtering was successful.
		ctx.setAttribute(AuthorizationAttributes.FILTER_DONE_ATTRIBUTE, Boolean.TRUE);
		ctx.setAttribute(AuthorizationAttributes.SELECT_DONE_ATTRIBUTE, Boolean.TRUE);

		return Result.SUCCESS;

	}

}
