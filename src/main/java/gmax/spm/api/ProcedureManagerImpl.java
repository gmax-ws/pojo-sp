/**
 * POJO Stored Procedure Entity Manager
 * Copyright (c) 2011-2014 Gmax
 *
 * Author: Marius Gligor <marius.gligor@gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111, USA.
 */
package gmax.spm.api;

import gmax.spm.annotations.StoredProcedure;
import gmax.spm.annotations.StoredProcedureParameter;
import gmax.spm.annotations.enums.TransactionOperation;
import gmax.spm.exception.ProcedureManagerException;

import java.lang.reflect.Field;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * ProcedureManager and TransactionManager API.
 * 
 * @author Marius Gligor
 * @version 3.0
 */
class ProcedureManagerImpl implements ProcedureManager, TransactionManager {

	/** Fields cache */
	private final Map<Class<?>, List<Field>> cache = new HashMap<>();

	/** The JDBC Connection object */
	private Connection connection;

	/**
	 * Default constructor.
	 */
	public ProcedureManagerImpl() {
	}

	/**
	 * Construct a ProcedureManager instance using a JDBC Connection.
	 * 
	 * @param 	connection
	 *       	JDBC Connection object.
	 */
	public ProcedureManagerImpl(Connection connection) {
		this.connection = connection;
	}

	/**
	 * Get connection.
	 * 
	 * @return JDBC Connection object.
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
		
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				throw new ProcedureManagerException(e);
			}
		}
	}

	/**
	 * Get TransactionManager API.
	 * 
	 * @return 	TransactionManager interface.
	 */
	@Override
	public TransactionManager getTransactionManager() {
		return this;
	}

	/**
	 * Get procedure name from @StoredProcedure annotation.
	 * 
	 * @param 	type
	 * 			pojo Class.
	 * 
	 * @return 	StoredProcedure annotation.
	 */
	private StoredProcedure getProcedureName(Class<? extends Object> type) {

		if (!type.isAnnotationPresent(StoredProcedure.class)) {
			throw new ProcedureManagerException(
					"@StoredProcedure annotation is missing.");
		}
		
		return type.getAnnotation(StoredProcedure.class);
	}

	/**
	 * Get a <code>List</code> of @StoredProcedureParameter annotated fields.
	 * 
	 * @param 	type
	 *          pojo Class.
	 * 
	 * @return List of StoredProcedureParameter annotated fields
	 */
	private List<Field> getProcedureParameters(Class<? extends Object> type) {
		
		List<Field> fields = cache.get(type);
		
		if (fields == null) {		
			fields = new LinkedList<>();
			cache.put(type, fields);
			
			for (Field field : type.getDeclaredFields()) {
				if (field.isAnnotationPresent(StoredProcedureParameter.class)) {
					fields.add(field);
				}
			}
		}
		
		return fields;
	}

	/**
	 * Build a SQL-92 call statement for a stored procedure or function.
	 * 
	 * @param 	procedure
	 *        	StoredProcedure metadata.
	 * @param 	parametersCount
	 * 			Number of parameters.
	 * 
	 * @return 	Generated call statement as string.
	 */
	private String callStatementString(StoredProcedure procedure,
			int parametersCount) {
		
		StringBuilder buffer = new StringBuilder("{");
		
		if (!procedure.procedure()) {
			buffer.append("? = ");
			parametersCount--;
		}
		
		buffer.append("call ").append(procedure.name()).append("(");
		for (int i = 0; i < parametersCount; i++) {
			buffer.append(i == 0 ? "?" : " ,?");
		}
		
		return buffer.append(")}").toString();
	}

	/**
	 * Register the input/output parameters before the call.
	 * 
	 * @param 	statement
	 *          CallableStatement object.
	 * @param 	pojo
	 *          Stored procedure entity.
	 * @param 	fields
	 *          List of fields.
	 * 
	 * @throws 	SQLException
	 * @throws 	IllegalAccessException
	 */
	private void setParameters(CallableStatement statement, Object pojo,
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
	 * @param 	statement
	 *          CallableStatement object.
	 * @param 	pojo
	 *          Stored procedure entity.
	 * @param 	fields
	 *          List of fields.
	 * 
	 * @throws 	IllegalAccessException
	 * @throws 	SQLException
	 */
	private void getParameters(CallableStatement statement, Object pojo,
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
	 * @param 	pojo
	 *          Entity object.
	 */
	@Override
	public void call(Connection connection, Object pojo) {
		this.connection = connection;
		call(pojo);
	}

	/**
	 * Call the function or stored procedure.
	 * 
	 * @param 	pojo
	 *          POJO entity.
	 */
	@Override	
	public void call(Object pojo) {
		if (pojo != null) {
			// get name and parameters
			Class<? extends Object> type = pojo.getClass();
			StoredProcedure procedure = getProcedureName(type);
			List<Field> fields = getProcedureParameters(type);
		
			// call procedure
			CallableStatement statement = null;
			try {
				String sql = callStatementString(procedure, fields.size());
				statement = connection.prepareCall(sql);
				setParameters(statement, pojo, fields);
				statement.execute();
				getParameters(statement, pojo, fields);
			} catch (SQLException | IllegalAccessException e) {
				throw new ProcedureManagerException(e);
			} finally {
				cleanUp(statement);
			}
		}
	}

	/**
	 * Close statement.
	 * 
	 * @param 	statement
	 *          CallableStatement object.
	 */
	private void cleanUp(CallableStatement statement) {

		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				throw new ProcedureManagerException(e);
			}
		}
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
	 * Process transaction.
	 * 
	 * @param 	operation
	 *          Transaction operation.
	 */
	private void processTransaction(TransactionOperation operation) {
		if (connection == null) {
			throw new ProcedureManagerException("JDBC connection is missing.");
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
