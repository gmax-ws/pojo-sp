/*
 * POJO Stored Procedure Entity Manager
 * Copyright (c) 2011-2021 Scalable Solutions SRL
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
package scalable.solutions.spm.api;

import java.sql.Connection;

/**
 * ProcedureManager API.
 *
 * @author Marius Gligor
 * @version 6.0
 */
public interface ProcedureManager {

    /**
     * Library version.
     * 
     * @return  see above. 
     */
    String version();
    
    /**
     * Close the procedure manager.
     */
    void close();

    /**
     * Return JDBC Connection object.
     *
     * @return  JDBC Connection.
     */
    Connection getConnection();

    /**
     * Return Transaction Manager object of this JDBC Connection.
     *
     * @return  TransactionManager object.
     */
    TransactionManager getTransactionManager();

    /**
     * Call a stored procedure or a function
     *
     * @param   pojo 
     *          Entity instance.
     * 
     * @return  <code>true</code> if the first result is a <code>ResultSet</code>
     *          object; <code>false</code> if the first result is an update
     *          count or there is no result
     */
    boolean call(Object pojo);

    /**
     * Call a stored procedure or a function using the given connection.
     *
     * @param   connection 
     *          Database connection object.
     * @param   pojo
     *          Entity instance.
     * 
     * @return  <code>true</code> if the first result is a <code>ResultSet</code>
     *          object; <code>false</code> if the first result is an update
     *          count or there is no result
     */
    boolean call(Connection connection, Object pojo);
}
