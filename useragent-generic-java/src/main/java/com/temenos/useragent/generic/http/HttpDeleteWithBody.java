package com.temenos.useragent.generic.http;

/*
 * #%L
 * useragent-generic-java
 * %%
 * Copyright (C) 2012 - 2017 Temenos Holdings N.V.
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

import java.net.URI;

import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;

/**
 * HttpDelete will not support Delete with an Entity, Hence this class will override this functionality.
 *
 * @author mohamednazir
 *
 */

@NotThreadSafe // HttpRequestBase is @NotThreadSafe
public class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
    
    public static final String METHOD_NAME = "DELETE";
    public String getMethod() { 
        return METHOD_NAME;
    }

    public HttpDeleteWithBody(final String uri) {
        super();
        setURI(URI.create(uri));
    }
    
    public HttpDeleteWithBody(final URI uri) {
        super();
        setURI(uri);
    }
    
    public HttpDeleteWithBody() { 
        super();
    }

}
