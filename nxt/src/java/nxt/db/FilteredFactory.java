/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2020 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of the Nxt software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package nxt.db;

import java.sql.PreparedStatement;
import java.sql.Statement;

/**
 * Create Statement and PrepareStatement for use with FilteredConnection
 */
public interface FilteredFactory {

    /**
     * Create a FilteredStatement for the supplied Statement
     *
     * @param   stmt                Statement
     * @return                      Wrapped statement
     */
    Statement createStatement(Statement stmt);

    /**
     * Create a FilteredPreparedStatement for the supplied PreparedStatement
     *
     * @param   stmt                Prepared statement
     * @param   sql                 SQL statement
     * @return                      Wrapped prepared statement
     */
    PreparedStatement createPreparedStatement(PreparedStatement stmt, String sql);
}
