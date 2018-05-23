package com.temenos.interaction.jdbc.context;

/*
 * #%L
 * interaction-jdbc-producer
 * %%
 * Copyright (C) 2012 - 2018 Temenos Holdings N.V.
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

/**
 * TODO: Document me!
 *
 * @author vsangeetha1
 *
 */
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
public class LocalContext extends InitialContext implements InitialContextFactoryBuilder, InitialContextFactory {

    Map<Object,Object> dataSources;
    
    LocalContext() throws NamingException {
        super();
        dataSources = new HashMap<Object,Object>();
    }
    
    public void addDataSource(String name, String connectionString, String username, String password) {
        this.
        dataSources.put(name, new LocalDataSource(connectionString,username,password));
    }

    public InitialContextFactory createInitialContextFactory(
            Hashtable<?, ?> hsh) throws NamingException {
        dataSources.putAll(hsh);
        return this;
    }

    public Context getInitialContext(Hashtable<?, ?> arg0)
            throws NamingException {
        return this;
    }

    @Override
    public Object lookup(String name) throws NamingException {
        Object ret = dataSources.get(name);
        return (ret != null) ? ret : super.lookup(name);
    }   
}
