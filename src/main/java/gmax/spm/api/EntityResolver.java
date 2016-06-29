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

import gmax.spm.annotations.StoredProcedure;
import gmax.spm.annotations.StoredProcedureParameter;
import gmax.spm.exception.ProcedureManagerException;
import gmax.spm.i18n.Messages;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Entity resolver.
 *
 * @author Marius Gligor
 * @version 4.0
 */
class EntityResolver {

    /**
     * Entities registry (cache)
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
     * @param type POJO Class.
     * @return StoredProcedure annotation.
     */
    private StoredProcedure getProcedureName(Class<?> type) {

        if (!type.isAnnotationPresent(StoredProcedure.class)) {
            throw new ProcedureManagerException(String.format(Messages.ERROR_NO_ANNOTATION, "@StoredProcedure"));
        }

        return type.getAnnotation(StoredProcedure.class);
    }

    /**
     * Get a <code>List</code> of @StoredProcedureParameter annotated fields.
     *
     * @param type pojo Class.
     * @return List of StoredProcedureParameter annotated fields
     */
    private List<Field> getProcedureParameters(Class<?> type) {

        return Arrays.stream(type.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(StoredProcedureParameter.class))
                .collect(Collectors.toList());
    }

    /**
     * Build a SQL-92 call statement for a stored procedure or function.
     *
     * @param procedure       StoredProcedure metadata.
     * @param parametersCount Number of parameters.
     * @return Generated call statement as string.
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
     * Create a new entity.
     *
     * @param type POJO instance class.
     * @return A new entity instance.
     */
    private Entity createEntity(Class<?> type) {

        Entity entity = new Entity();
        entity.fields = getProcedureParameters(type);
        entity.sql = callStatementString(getProcedureName(type), entity.fields.size());
        return entity;
    }

    /**
     * Extract entity properties. Entity properties are cached.
     *
     * @param pojo POJO entity.
     * @return Entity fields and SQL statement.
     */
    Entity resolve(Object pojo) {

        Class<?> type = pojo.getClass();
        Entity entity = registry.get(type);

        if (entity == null) {
            // create and register a new entity
            entity = createEntity(type);
            registry.put(type, entity);
        }

        return entity;
    }

    /**
     * Entity properties.
     */
    static class Entity {

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
