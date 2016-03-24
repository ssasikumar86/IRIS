package com.temenos.interaction.core.command;

/*
 * #%L
 * interaction-core
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


import javax.ws.rs.core.Response.StatusType;

import org.odata4j.core.OError;
import org.odata4j.exceptions.ODataProducerException;

import com.temenos.interaction.core.resource.EntityResource;

/**
 * Interaction Producer exception.
 * This exception enables IRIS Producer to raise interaction producer error messages.  
 */
public class InteractionProducerException extends ODataProducerException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String entitySetName;
	private EntityResource<?> entityResource;
	private StatusType httpStatus;

	/**
	 * @param error
	 */
	public InteractionProducerException(StatusType httpStatus, OError error, String entitySetName, EntityResource<?> entityResource) {
		super(error);
		this.httpStatus = httpStatus;
		this.entitySetName = entitySetName;
		this.entityResource = entityResource;
	}
	
	public InteractionProducerException(StatusType httpStatus, String message, String entitySetName, EntityResource<?> entityResource) {
		super(message, null);
		this.httpStatus = httpStatus;
		this.entitySetName = entitySetName;
		this.entityResource = entityResource;
	}

	@Override
	public StatusType getHttpStatus() {
		return httpStatus;
	}
	
	/**
	 * @return the entitySetName
	 */
	public String getEntitySetName() {
		return entitySetName;
	}

	
	/**
	 * @return the entityProperties
	 */
	public EntityResource<?> getEntityResource() {
		return entityResource; 
	}
}
