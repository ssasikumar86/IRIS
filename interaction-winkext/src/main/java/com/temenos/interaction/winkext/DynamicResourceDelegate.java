package com.temenos.interaction.winkext;

/*
 * #%L
 * interaction-winkext
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


import java.util.Collection;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.common.DynamicResource;
import org.apache.wink.common.model.multipart.InMultiPart;

import com.temenos.interaction.core.UriInfoImpl;
import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.resource.EntityResource;
import com.temenos.interaction.core.rim.HTTPResourceInteractionModel;
import com.temenos.interaction.core.rim.ResourceInteractionModel;

public class DynamicResourceDelegate implements HTTPResourceInteractionModel, DynamicResource {

	private final HTTPResourceInteractionModel parent;
	private final HTTPResourceInteractionModel resource;
	
	public DynamicResourceDelegate(HTTPResourceInteractionModel parent, HTTPResourceInteractionModel resource) {
		this.parent = parent;
		this.resource = resource;
	}

	@Override
    public String getBeanName() {
		if (resource instanceof DynamicResource) {
			return ((DynamicResource)resource).getBeanName();
		}
        return resource.getCurrentState().getId();
    }

	@Override
    public void setBeanName(String beanName) {
        throw new AssertionError("Not supported");
    }

    public void setWorkspaceTitle(String workspaceTitle) {
        throw new AssertionError("Not supported");
    }

	@Override
    public String getWorkspaceTitle() {
		return "DefaultWorkspace";
    }

    public void setCollectionTitle(String collectionTitle) {
        throw new AssertionError("Not supported");
    }

	@Override
    public String getCollectionTitle() {
		return resource.getResourcePath();
    }

	@Override
    public String getPath() {
        return resource.getResourcePath();
    }

	@Override
    public void setParent(Object parent) {
        throw new AssertionError("Not supported");
    }

	@Override
    public HTTPResourceInteractionModel getParent() {
        return parent;
    }

	@Override
	public Response get(HttpHeaders headers, String id, UriInfo uriInfo) {
		return resource.get(headers, id, new UriInfoImpl(uriInfo));
	}

	@Override
	public Response post(HttpHeaders headers, UriInfo uriInfo, InMultiPart inMP) {
		return resource.post(headers, new UriInfoImpl(uriInfo), inMP);
	}	
	
	@Override
    public Response post( @Context HttpHeaders headers, @PathParam("id") String id, @Context UriInfo uriInfo, 
    		MultivaluedMap<String, String> formParams) {
		return resource.post(headers, id, new UriInfoImpl(uriInfo), formParams);
	}

	@Override
	public Response post(HttpHeaders headers, String id, UriInfo uriInfo, EntityResource<?> eresource) {
		return resource.post(headers, id, new UriInfoImpl(uriInfo), eresource);
	}
	
	@Override
	public Response put(HttpHeaders headers, UriInfo uriInfo, InMultiPart inMP) {
		return resource.put(headers, new UriInfoImpl(uriInfo), inMP);
	}	

	@Override
	public Response put(HttpHeaders headers, String id, UriInfo uriInfo, EntityResource<?> eresource) {
		return resource.put(headers, id, new UriInfoImpl(uriInfo), eresource);
	}

	@Override
	public Response delete(HttpHeaders headers, String id, UriInfo uriInfo) {
		return resource.delete(headers, id, new UriInfoImpl(uriInfo));
	}

	@Override
	public Response options(@Context HttpHeaders headers, @PathParam("id") String id, @Context UriInfo uriInfo) {
		return resource.options(headers, id, new UriInfoImpl(uriInfo));
	}

	@Override
	public ResourceState getCurrentState() {
		return resource.getCurrentState();
	}

	@Override
	public String getResourcePath() {
		return resource.getResourcePath();
	}

	@Override
	public String getFQResourcePath() {
		return resource.getFQResourcePath();
	}

	@Override
	public Collection<ResourceInteractionModel> getChildren() {
		return resource.getChildren();
	}

    @Override
    public Response delete(HttpHeaders headers, String id, UriInfo uriInfo, EntityResource<?> eresource) {
        return resource.delete(headers, id, new UriInfoImpl(uriInfo), eresource);
    }    
}
