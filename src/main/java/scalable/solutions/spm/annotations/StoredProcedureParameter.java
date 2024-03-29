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
package scalable.solutions.spm.annotations;

import scalable.solutions.spm.annotations.enums.Direction;

import java.lang.annotation.*;
import java.sql.Types;

/**
 * Stored Procedure parameters annotation.
 *
 * @author Marius Gligor
 * @version 6.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD})
public @interface StoredProcedureParameter {

    /**
     * The stored procedure parameter index.
     *
     * @return  parameter index.
     */
    int index();

    /**
     * The stored procedure parameter SQL type.
     *
     * @return	SQL type mapping for this stored procedure parameter.
     */
    int type() default Types.VARCHAR;

    /**
     * The stored procedure parameter direction attribute IN, OUT, INOUT.
     *
     * @return  Direction attribute of the stored procedure parameter.
     */
    Direction direction() default Direction.IN;
}
