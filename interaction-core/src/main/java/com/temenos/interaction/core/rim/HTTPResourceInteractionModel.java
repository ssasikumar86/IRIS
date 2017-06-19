package com.temenos.interaction.core.rim;

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


import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.common.model.multipart.InMultiPart;

import com.temenos.interaction.core.ExtendedMediaTypes;
import com.temenos.interaction.core.resource.EntityResource;

public interface HTTPResourceInteractionModel extends ResourceInteractionModel {

	/**
	 * GET a resource representation.
	 */
	@GET
	@Produces({
			MediaType.APPLICATION_ATOM_XML,
			MediaType.APPLICATION_XML,
			ExtendedMediaTypes.APPLICATION_ATOMSVC_XML,
			MediaType.APPLICATION_JSON,
			MediaType.APPLICATION_XHTML_XML,
			MediaType.TEXT_HTML,
			MediaType.WILDCARD})
	public Response get(@Context HttpHeaders headers, @PathParam("id") String id, @Context UriInfo uriInfo);
	
	/**
	 * POST a resource representation from a from.
	 */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({
        MediaType.APPLICATION_ATOM_XML,
        MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON,
        MediaType.WILDCARD})
    public Response post( @Context HttpHeaders headers, @PathParam("id") String id, @Context UriInfo uriInfo, 
    		MultivaluedMap<String, String> formParams);

    /**
     * POST a multipart request i.e. multiple resource representations 
     */
    @POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({
        MediaType.APPLICATION_ATOM_XML,
        MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON,
        MediaType.WILDCARD})
    public Response post(@Context HttpHeaders headers, @Context UriInfo uriInfo, InMultiPart inMP);
    
	/**
	 * POST a resource representation.
	 */
	@POST
	@Consumes({
		MediaType.APPLICATION_ATOM_XML,
    	MediaType.APPLICATION_XML, 
    	MediaType.APPLICATION_JSON, 
    	MediaType.WILDCARD})
	@Produces({
		MediaType.APPLICATION_ATOM_XML,
    	MediaType.APPLICATION_XML, 
    	MediaType.APPLICATION_JSON, 
    	MediaType.WILDCARD})
	public Response post(@Context HttpHeaders headers, @PathParam("id") String id, @Context UriInfo uriInfo,
			EntityResource<?> resource);

    /**
     * PUT a multipart request i.e. multiple resource representations 
     */	
    @PUT
	@Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({
        MediaType.APPLICATION_ATOM_XML,
        MediaType.APPLICATION_XML,
        MediaType.APPLICATION_JSON,
        MediaType.WILDCARD})
    public Response put(@Context HttpHeaders headers, @Context UriInfo uriInfo, InMultiPart inMP);
	
	/**
	 * PUT a resource.
	 */
	@PUT
	@Consumes({
		MediaType.APPLICATION_ATOM_XML,
    	MediaType.APPLICATION_XML, 
    	MediaType.APPLICATION_JSON, 
    	MediaType.WILDCARD})
	@Produces({
		MediaType.APPLICATION_ATOM_XML,
    	MediaType.APPLICATION_XML, 
    	MediaType.APPLICATION_JSON, 
    	MediaType.WILDCARD})
	public Response put(@Context HttpHeaders headers, @PathParam("id") String id, @Context UriInfo uriInfo,
			EntityResource<?> resource);

	/**
	 * DELETE a resource.
	 */
	@DELETE
	@Consumes({
        MediaType.APPLICATION_ATOM_XML,
        MediaType.APPLICATION_XML, 
        MediaType.APPLICATION_JSON, 
        MediaType.WILDCARD})
    @Produces({
        MediaType.APPLICATION_ATOM_XML,
        MediaType.APPLICATION_XML, 
        MediaType.APPLICATION_JSON, 
        MediaType.WILDCARD})
	public Response delete(@Context HttpHeaders headers, @PathParam("id") String id, @Context UriInfo uriInfo);
}