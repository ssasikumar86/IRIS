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

import javax.naming.spi.NamingManager;

public class LocalContextFactory {
    /**
     * do not instantiate this class directly. Use the factory method.
     */
    private LocalContextFactory() {}
    
    public static LocalContext createLocalContext(String databaseDriver) throws Exception {

        try { 
            LocalContext ctx = new LocalContext();
            Class.forName(databaseDriver);  
            NamingManager.setInitialContextFactoryBuilder(ctx);         
            return ctx;
        }
        catch(Exception e) {
            throw new Exception("Error Initializing Context: " + e.getMessage(),e);
        }
    }   
}
