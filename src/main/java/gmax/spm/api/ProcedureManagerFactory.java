/**
 * POJO Stored Procedure Entity Manager 
 * Copyright (c) 2011-2014 Gmax
 *
 * Author: Marius Gligor <marius.gligor@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111, USA.
 */
package gmax.spm.api;

import gmax.spm.annotations.JDBC;
import gmax.spm.exception.ProcedureManagerException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Factory class to create ProcedureManager instances.
 *
 * @author Marius Gligor
 * @version 3.0
 */
public final class ProcedureManagerFactory {

    /**
     * Hidden constructor. 
     */
    private ProcedureManagerFactory() {
    }

    /**
     * Create a new instance of ProcedureManager.
     *
     * @return 	ProcedureManager instance.
     */
    public static ProcedureManager createInstance() {
        return new ProcedureManagerImpl();
    }

    /**
     * Create a new instance of ProcedureManager.
     *
     * @param 	source 
     * 			DataSource object.
     * 
     * @return 	ProcedureManager instance.
     */
    public static ProcedureManager createInstance(DataSource source) {
        try {
            return new ProcedureManagerImpl(source.getConnection());
        } catch (SQLException e) {
            throw new ProcedureManagerException(e);
        }
    }

    /**
     * Create a new instance of ProcedureManager from a JDBC connection.
     *
     * @param 	connection
     * 			JDBC connection.
     * 
     * @return 	ProcedureManager instance.
     */
    public static ProcedureManager createInstance(Connection connection) {
        return new ProcedureManagerImpl(connection);
    }

    /**
     * Create a new instance of ProcedureManager from a @JDBC annotated class.
     *
     * @param 	jdbcClass 
     * 			@JDBC annotated class.
     * 
     * @return 	ProcedureManager instance.
     */
    public static ProcedureManager createInstance(Class<?> jdbcClass) {
        
        if (!jdbcClass.isAnnotationPresent(JDBC.class)) {
            throw new ProcedureManagerException("@JDBC annotation is missing.");
        }
        
        JDBC jdbc = jdbcClass.getAnnotation(JDBC.class);
        
        try {
            Class.forName(jdbc.driver());
            
            Connection connection = DriverManager.getConnection(jdbc.url(),
                    jdbc.username(), jdbc.password());
            
            return new ProcedureManagerImpl(connection);
        } catch (ClassNotFoundException | SQLException e) {
            throw new ProcedureManagerException(e);
        }
    }
}
