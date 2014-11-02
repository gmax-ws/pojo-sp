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

import gmax.spm.annotations.StoredProcedure;
import gmax.spm.annotations.StoredProcedureParameter;
import gmax.spm.exception.ProcedureManagerException;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Entity resolver.
 *
 * @author Marius Gligor
 * @version 3.1
 */
class EntityResolver {

    /** Annotation missing */
    private static final String ERROR_NO_ANNOTATION = "%s annotation is missing.";
    
    /**
     * Entities cache
     */
    private final Map<Class<?>, Entity> registry;

    /**
     * Default constructor.
     */
    EntityResolver() {
        this.registry = new ConcurrentHashMap<>();
    }

    /**
     * Get procedure name from @StoredProcedure annotation.
     *
     * @param   type 
     *          pojo Class.
     *
     * @return  StoredProcedure annotation.
     */
    private StoredProcedure getProcedureName(Class<? extends Object> type) {

        if (!type.isAnnotationPresent(StoredProcedure.class)) {
            String message = String.format(ERROR_NO_ANNOTATION, "@StoredProcedure");
            throw new ProcedureManagerException(message);
        }

        return type.getAnnotation(StoredProcedure.class);
    }

    /**
     * Get a <code>List</code> of @StoredProcedureParameter annotated fields.
     *
     * @param   type 
     *          pojo Class.
     *
     * @return  List of StoredProcedureParameter annotated fields
     */
    private List<Field> getProcedureParameters(Class<? extends Object> type) {

        List<Field> fields = new LinkedList<>();

        for (Field field : type.getDeclaredFields()) {
            if (field.isAnnotationPresent(StoredProcedureParameter.class)) {
                fields.add(field);
            }
        }

        return fields;
    }

    /**
     * Build a SQL-92 call statement for a stored procedure or function.
     *
     * @param   procedure 
     *          StoredProcedure metadata.
     * @param   parametersCount 
     *          Number of parameters.
     *
     * @return  Generated call statement as string.
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
     * Extract entity properties. Entity properties are cached.
     *
     * @param   pojo
     *          POJO entity.
     *
     * @return  Entity fields and SQL statement.
     */
    Entity resolve(Object pojo) {

        Class<? extends Object> type = pojo.getClass();
        Entity entity = registry.get(type);

        if (entity == null) {
            entity = new Entity();
            registry.put(type, entity);

            entity.fields = getProcedureParameters(type);
            entity.sql = callStatementString(getProcedureName(type),
                    entity.fields.size());
        }

        return entity;
    }

    /**
     * Entity properties.
     */
    class Entity {

        /**
         * SQL statement
         */
        String sql;

        /**
         * Entity fields
         */
        List<Field> fields;
    }
}
