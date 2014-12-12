/*
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

import gmax.spm.annotations.StoredProcedureParameter;
import gmax.spm.annotations.enums.TransactionOperation;
import gmax.spm.api.EntityResolver.Entity;
import gmax.spm.exception.ProcedureManagerException;
import gmax.spm.i18n.Messages;

import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * ProcedureManager and TransactionManager API.
 *
 * @author Marius Gligor
 * @version 3.1
 */
class ProcedureManagerImpl implements ProcedureManager, TransactionManager {

    /**
     * Entity resolver instance.
     */
    private final EntityResolver resolver = new EntityResolver();

    /**
     * The JDBC Connection object
     */
    private Connection connection;

    /**
     * Default constructor.
     */
    ProcedureManagerImpl() {
    }

    /**
     * Construct a ProcedureManager instance using a JDBC Connection.
     *
     * @param   connection 
     *          JDBC Connection object.
     */
    ProcedureManagerImpl(Connection connection) {
        this.connection = connection;
    }

    /**
     * Library version.
     *
     * @return see above.
     */
    @Override
    public String version() {
        return Messages.VERSION;
    }

    /**
     * Get connection.
     *
     * @return  JDBC Connection object.
     */
    @Override
    public Connection getConnection() {
        return connection;
    }

    /**
     * Close connection.
     */
    @Override
    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new ProcedureManagerException(e);
        } finally {
            connection = null;
        }
    }

    /**
     * Get TransactionManager API.
     *
     * @return TransactionManager interface.
     */
    @Override
    public TransactionManager getTransactionManager() {
        return this;
    }

    /**
     * Register the input/output parameters before the call.
     *
     * @param   statement 
     *          CallableStatement object.
     * @param   pojo 
     *          Stored procedure entity.
     * @param   fields 
     *          List of fields.
     *
     * @throws  SQLException
     * @throws  IllegalAccessException
     */
    private void bindInputParameters(CallableStatement statement, Object pojo,
            List<Field> fields) throws SQLException, IllegalAccessException {

        for (Field field : fields) {
            StoredProcedureParameter param = field
                    .getAnnotation(StoredProcedureParameter.class);

            switch (param.direction()) {
                case IN:
                    field.setAccessible(true);
                    statement.setObject(param.index(), field.get(pojo));
                    break;
                case OUT:
                    statement.registerOutParameter(param.index(), param.type());
                    break;
                case INOUT:
                    field.setAccessible(true);
                    statement.setObject(param.index(), field.get(pojo));
                    statement.registerOutParameter(param.index(), param.type());
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Register the output parameters after call.
     *
     * @param   statement
     *          CallableStatement object.
     * @param   pojo
     *          Stored procedure entity.
     * @param   fields
     *          List of fields.
     *
     * @throws  IllegalAccessException
     * @throws  SQLException
     */
    private void bindOutputParameters(CallableStatement statement, Object pojo,
            List<Field> fields) throws IllegalAccessException, SQLException {

        for (Field field : fields) {
            StoredProcedureParameter param = field
                    .getAnnotation(StoredProcedureParameter.class);
            switch (param.direction()) {
                case OUT:
                case INOUT:
                    field.setAccessible(true);
                    field.set(pojo, statement.getObject(param.index()));
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Call the function or stored procedure.
     *
     * @param   connection
     *          JDBC connection.
     * @param   pojo 
     *          Entity object.
     *
     * @return  <code>true</code> if the first result is a <code>ResultSet</code> object; 
     *          <code>false</code> if the first result is an update count or no result
     */
    @Override
    public boolean call(Connection connection, Object pojo) {
        this.connection = connection;
        return call(pojo);
    }

    /**
     * Call a function or stored procedure.
     *
     * @param   pojo 
     *          POJO entity.
     *
     * @return  <code>true</code> if the first result is a <code>ResultSet</code> object; 
     *          <code>false</code> if the first result is an update count or no result
     */
    @Override
    public boolean call(Object pojo) {

        // Null POJO entities are not allowed.
        if (pojo == null) {
            throw new ProcedureManagerException(Messages.ERROR_NO_ENTITY);
        }

        // check database connection.
        if (connection == null) {
            throw new ProcedureManagerException(Messages.ERROR_NO_CONNECTION);
        }

        return execute(pojo);
    }

    /**
     * Execute JBBC statement.
     *
     * @param   pojo 
     *          Stored procedure object.
     *
     * @return  <code>true</code> success. <code>false</code> error.
     */
    private boolean execute(Object pojo) {

        boolean result = false;

        // resolve entity
        Entity entity = resolver.resolve(pojo);

        // call procedure
        try (CallableStatement statement = connection.prepareCall(entity.sql)) {
            bindInputParameters(statement, pojo, entity.fields);
            result = statement.execute();
            bindOutputParameters(statement, pojo, entity.fields);
        } catch (SQLException | IllegalAccessException e) {
            throw new ProcedureManagerException(e);
        }

        return result;
    }

    /**
     * Start a new JDBC transaction.
     */
    @Override
    public void begin() {
        processTransaction(TransactionOperation.START);
    }

    /**
     * Save changes.
     */
    @Override
    public void commit() {
        processTransaction(TransactionOperation.COMMIT);
    }

    /**
     * Undo the changes.
     */
    @Override
    public void rollback() {
        processTransaction(TransactionOperation.ROLLBACK);
    }

    /**
     * Stop a JDBC transaction.
     */
    @Override
    public void end() {
        processTransaction(TransactionOperation.STOP);
    }

    /**
     * Process JDBC transaction.
     *
     * @param   operation 
     *          Transaction operation.
     */
    private void processTransaction(TransactionOperation operation) {
        if (connection == null) {
            throw new ProcedureManagerException(Messages.ERROR_NO_CONNECTION);
        }
        try {
            switch (operation) {
                case START:
                    connection.setAutoCommit(false);
                    break;
                case COMMIT:
                    connection.commit();
                    break;
                case ROLLBACK:
                    connection.rollback();
                    break;
                default:
                    connection.setAutoCommit(true);
                    break;
            }
        } catch (SQLException e) {
            throw new ProcedureManagerException(e);
        }
    }
}
