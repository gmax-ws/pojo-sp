/*
 * POJO Stored Procedure Entity Manager
 * Copyright (c) 2011-2016 Gmax
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
import gmax.spm.i18n.Messages;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static gmax.spm.i18n.Messages.ERROR_NO_ANNOTATION;

/**
 * Factory class to create ProcedureManager instances.
 *
 * @author Marius Gligor
 * @version 4.0
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
     * @return ProcedureManager instance.
     */
    public static ProcedureManager createInstance() {
        return new ProcedureManagerImpl();
    }

    /**
     * Create a new instance of ProcedureManager.
     *
     * @param source DataSource object.
     * @return ProcedureManager instance.
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
     * @param connection JDBC connection.
     * @return ProcedureManager instance.
     */
    public static ProcedureManager createInstance(Connection connection) {
        return new ProcedureManagerImpl(connection);
    }

    /**
     * Create a new instance of ProcedureManager from a @JDBC annotated class.
     *
     * @param jdbcClass @JDBC annotated class.
     * @return ProcedureManager instance.
     */
    public static ProcedureManager createInstance(Class<?> jdbcClass) {

        if (!jdbcClass.isAnnotationPresent(JDBC.class)) {
            throw new ProcedureManagerException(String.format(ERROR_NO_ANNOTATION, "@JDBC"));
        }

        try {
            JDBC jdbc = jdbcClass.getAnnotation(JDBC.class);
            Class.forName(jdbc.driver());

            Connection connection = DriverManager.getConnection(jdbc.url(),
                    jdbc.username(), jdbc.password());

            return new ProcedureManagerImpl(connection);
        } catch (ClassNotFoundException | SQLException e) {
            throw new ProcedureManagerException(e);
        }
    }
}
