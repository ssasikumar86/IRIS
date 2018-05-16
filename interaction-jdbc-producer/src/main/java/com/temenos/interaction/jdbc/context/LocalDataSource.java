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

import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;

class LocalDataSource implements DataSource , Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String connectionString;
    private String username;
    private String password;
    
    LocalDataSource(String connectionString, String username, String password) {
        this.connectionString = connectionString;
        this.username = username;
        this.password = password;
    }
    
    public Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection(connectionString, username, password);
    }

    public Connection getConnection(String username, String password)
            throws SQLException {return null;}
    public PrintWriter getLogWriter() throws SQLException {return null;}
    public int getLoginTimeout() throws SQLException {return 0;}
    public void setLogWriter(PrintWriter out) throws SQLException { }
    public void setLoginTimeout(int seconds) throws SQLException {}

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }
}
