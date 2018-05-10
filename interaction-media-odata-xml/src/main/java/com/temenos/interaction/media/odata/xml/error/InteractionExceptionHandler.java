package com.temenos.interaction.media.odata.xml.error;

/*
 * #%L
 * interaction-media-odata-xml
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


import org.apache.commons.httpclient.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all Interaction Framework exception handlers.
 * 
 * @author dgroves
 */
public class InteractionExceptionHandler<T extends Exception> {
    
    private final Logger logger = LoggerFactory.getLogger(InteractionExceptionHandler.class);
    
    public InteractionExceptionHandler(){}
    
    protected String generateLogMessage(T exception, int code){
        String message = new StringBuilder("HTTP ")
            .append(code)
            .append(" ")
            .append(HttpStatus.getStatusText(code))
            .toString();
            logger.error(message, exception);
            return message;
    }
}
