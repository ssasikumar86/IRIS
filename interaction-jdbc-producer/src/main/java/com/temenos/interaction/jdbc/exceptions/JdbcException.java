package com.temenos.interaction.jdbc.exceptions;

/*
 * #%L
 * interaction-jdbc-producer
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

import com.temenos.interaction.core.command.InteractionException;

public class JdbcException extends InteractionException {

	private static final long serialVersionUID = 1L;

	/**
	 * Construct a new exception
	 * 
	 * @param httpStatus
	 *            HTTP status code
	 */
	public JdbcException(StatusType httpStatus) {
		super(httpStatus);
	}

	/**
	 * Construct a new exception
	 * 
	 * @param httpStatus
	 *            HTTP status code
	 * @param message
	 *            error message
	 */
	public JdbcException(StatusType httpStatus, String message) {
		super(httpStatus, message);
	}

	/**
	 * Construct a new exception
	 * 
	 * @param httpStatus
	 *            HTTP status code
	 * @param message
	 *            error message
	 * @param cause
	 *            the cause. (A null value is permitted, and indicates that the
	 *            cause is nonexistent or unknown.)
	 */
	public JdbcException(StatusType httpStatus, String message, Throwable cause) {
		super(httpStatus, message, cause);
	}

	/**
	 * Construct a new exception
	 * 
	 * @param httpStatus
	 *            HTTP status code
	 * @param cause
	 *            the cause. (A null value is permitted, and indicates that the
	 *            cause is nonexistent or unknown.)
	 */
	public JdbcException(StatusType httpStatus, Throwable cause) {
		super(httpStatus, cause);
	}
}
