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

/**
 * TransactionManager API
 *
 * @author Marius Gligor
 * @version 6.0
 */
public interface TransactionManager {

    /* Start transaction */
    void begin();

    /* Stop transaction */
    void end();

    /* Save changes */
    void commit();

    /* Undo changes */
    void rollback();
}
