package com.temenos.interaction.core.hypermedia;

/*
 * #%L
 * interaction-core
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


import javax.ws.rs.core.MultivaluedMap;
import java.util.Collection;


/**
 * A LinkGenerator is used to generate a {@link Collection} of {@link Link}
 * for a {@link Transition} using data from a resource entity.
 *
 */
public interface LinkGenerator {

    /**
     * Create a {@link Collection} of {@link Link} using supplied path parameters,
     * query parameters and entity.
     *
     * @param pathParameters path parameters sent in request
     *
     * @param queryParameters query parameters sent in request
     *
     * @param entity resource entity coming back in response
     *
     * @return {@link Collection} of {@link Link}
     */
    public Collection<Link> createLink(MultivaluedMap<String, String> pathParameters,
            MultivaluedMap<String, String> queryParameters, Object entity);

}
