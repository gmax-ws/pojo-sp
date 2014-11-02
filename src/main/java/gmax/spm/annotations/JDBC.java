/**
 * POJO Stored Procedure Entity Manager Copyright (c) 2011-2014 Gmax
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
package gmax.spm.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * JDBC annotation. Contains JDBC connection parameters.
 *
 * @author Marius Gligor
 * @version 3.1
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
public @interface JDBC {

    /**
     * JDBC Driver.
     *
     * @return	JDBC driver class.
     */
    String driver();

    /**
     * URL connection.
     *
     * @return URL connection.
     */
    String url();

    /**
     * User name.
     *
     * @return	username.
     */
    String username() default "";

    /**
     * Password.
     *
     * @return	password.
     */
    String password() default "";
}
