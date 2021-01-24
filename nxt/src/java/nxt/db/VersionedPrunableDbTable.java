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

public abstract class VersionedPrunableDbTable<T> extends PrunableDbTable<T> {

    protected VersionedPrunableDbTable(String table, DbKey.Factory<T> dbKeyFactory) {
        super(table, dbKeyFactory, true, null);
    }

    protected VersionedPrunableDbTable(String table, DbKey.Factory<T> dbKeyFactory, String fullTextSearchColumns) {
        super(table, dbKeyFactory, true, fullTextSearchColumns);
    }

}
