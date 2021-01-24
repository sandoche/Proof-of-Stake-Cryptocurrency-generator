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


import nxt.Constants;
import nxt.Nxt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class VersionedEntityDbTable<T> extends EntityDbTable<T> {

    protected VersionedEntityDbTable(String table, DbKey.Factory<T> dbKeyFactory) {
        super(table, dbKeyFactory, true, null);
    }

    protected VersionedEntityDbTable(String table, DbKey.Factory<T> dbKeyFactory, String fullTextSearchColumns) {
        super(table, dbKeyFactory, true, fullTextSearchColumns);
    }

    public final boolean delete(T t) {
        return delete(t, false);
    }

    public final boolean delete(T t, boolean keepInCache) {
        if (t == null) {
            return false;
        }
        if (!db.isInTransaction()) {
            throw new IllegalStateException("Not in transaction");
        }
        DbKey dbKey = dbKeyFactory.newKey(t);
        try (Connection con = db.getConnection();
             PreparedStatement pstmtCount = con.prepareStatement("SELECT 1 FROM " + table
                     + dbKeyFactory.getPKClause() + " AND height < ? LIMIT 1")) {
            int i = dbKey.setPK(pstmtCount);
            pstmtCount.setInt(i, Nxt.getBlockchain().getHeight());
            try (ResultSet rs = pstmtCount.executeQuery()) {
                if (rs.next()) {
                    try (PreparedStatement pstmt = con.prepareStatement("UPDATE " + table
                            + " SET latest = FALSE " + dbKeyFactory.getPKClause() + " AND latest = TRUE LIMIT 1")) {
                        dbKey.setPK(pstmt);
                        pstmt.executeUpdate();
                        save(con, t);
                        pstmt.executeUpdate(); // delete after the save
                    }
                    return true;
                } else {
                    try (PreparedStatement pstmtDelete = con.prepareStatement("DELETE FROM " + table + dbKeyFactory.getPKClause())) {
                        dbKey.setPK(pstmtDelete);
                        return pstmtDelete.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        } finally {
            if (!keepInCache) {
                db.getCache(table).remove(dbKey);
            }
        }
    }

    static void popOff(final TransactionalDb db, final String table, final int height, final DbKey.Factory dbKeyFactory) {
        if (!db.isInTransaction()) {
            throw new IllegalStateException("Not in transaction");
        }
        try (Connection con = db.getConnection();
             PreparedStatement pstmtSelectToDelete = con.prepareStatement("SELECT DISTINCT " + dbKeyFactory.getPKColumns()
                     + " FROM " + table + " WHERE height > ?");
             PreparedStatement pstmtDelete = con.prepareStatement("DELETE FROM " + table
                     + " WHERE height > ? LIMIT " + Constants.BATCH_COMMIT_SIZE);
             PreparedStatement pstmtSetLatest = con.prepareStatement("UPDATE " + table
                     + " SET latest = TRUE " + dbKeyFactory.getPKClause() + " AND height ="
                     + " (SELECT MAX(height) FROM " + table + dbKeyFactory.getPKClause() + ")")) {
            pstmtSelectToDelete.setInt(1, height);
            List<DbKey> dbKeys = new ArrayList<>();
            try (ResultSet rs = pstmtSelectToDelete.executeQuery()) {
                while (rs.next()) {
                    dbKeys.add(dbKeyFactory.newKey(rs));
                }
            }
            pstmtDelete.setInt(1, height);
            int count;
            do {
                count = pstmtDelete.executeUpdate();
                db.commitTransaction();
            } while (count >= Constants.BATCH_COMMIT_SIZE);
            count = 0;
            for (DbKey dbKey : dbKeys) {
                int i = 1;
                i = dbKey.setPK(pstmtSetLatest, i);
                i = dbKey.setPK(pstmtSetLatest, i);
                pstmtSetLatest.executeUpdate();
                if (++count % Constants.BATCH_COMMIT_SIZE == 0) {
                    db.commitTransaction();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    static void trim(final TransactionalDb db, final String table, final int height, final DbKey.Factory dbKeyFactory) {
        if (!db.isInTransaction()) {
            throw new IllegalStateException("Not in transaction");
        }
        try (Connection con = db.getConnection();
             PreparedStatement pstmtSelect = con.prepareStatement("SELECT " + dbKeyFactory.getPKColumns() + ", MAX(height) AS max_height"
                     + " FROM " + table + " WHERE height < ? GROUP BY " + dbKeyFactory.getPKColumns() + " HAVING COUNT(DISTINCT height) > 1");
             PreparedStatement pstmtDelete = con.prepareStatement("DELETE FROM " + table + dbKeyFactory.getPKClause()
                     + " AND height < ? AND height >= 0 LIMIT " + Constants.BATCH_COMMIT_SIZE);
             PreparedStatement pstmtDeleteDeleted = con.prepareStatement("DELETE FROM " + table + " WHERE height < ? AND height >= 0 AND latest = FALSE "
                     + " AND (" + dbKeyFactory.getPKColumns() + ") NOT IN (SELECT " + dbKeyFactory.getPKColumns() + " FROM "
                     + table + " WHERE height >= ?) LIMIT " + Constants.BATCH_COMMIT_SIZE)) {
            pstmtSelect.setInt(1, height);
            try (ResultSet rs = pstmtSelect.executeQuery()) {
                int count = 0;
                int deleted;
                while (rs.next()) {
                    DbKey dbKey = dbKeyFactory.newKey(rs);
                    int i = 1;
                    i = dbKey.setPK(pstmtDelete, i);
                    pstmtDelete.setInt(i, rs.getInt("max_height"));
                    do {
                        deleted = pstmtDelete.executeUpdate();
                        if ((count += deleted) >= Constants.BATCH_COMMIT_SIZE) {
                            db.commitTransaction();
                            count = 0;
                        }
                    } while (deleted >= Constants.BATCH_COMMIT_SIZE);
                }
                pstmtDeleteDeleted.setInt(1, height);
                pstmtDeleteDeleted.setInt(2, height);
                do {
                    deleted = pstmtDeleteDeleted.executeUpdate();
                    db.commitTransaction();
                } while (deleted >= Constants.BATCH_COMMIT_SIZE);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

}
