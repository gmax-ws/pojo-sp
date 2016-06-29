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
package gmax.spm.i18n;

/**
 * Messages (en) 
 * 
 * @author Marius Gligor
 * @version 4.0
 */
public interface Messages {

    /** Library version */
    String VERSION = "4.0";

    /** Annotation missing */
    String ERROR_NO_ANNOTATION = "%s annotation is missing.";
    
    /** No JDBC connection error message */
    String ERROR_NO_CONNECTION = "JDBC connection is missing.";
    
    /** Null stored procedure entity error message */
    String ERROR_NO_ENTITY = "Null stored procedure entity is not allowed.";
}
