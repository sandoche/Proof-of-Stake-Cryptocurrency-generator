/*
 * Copyright © 2020 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of this software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package nxt.db;

import nxt.Constants;
import nxt.Nxt;
import nxt.util.Logger;
import org.h2.value.Value;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Locale;

public class TrimmableDbTable<T> extends DerivedDbTable {
    private static final boolean USE_FAST_TRIMMING = true;
    final boolean multiversion;
    protected final DbKey.Factory<T> dbKeyFactory;
    private final boolean isFastTrimEnabled;
    private String[] keyColumns;
    private final int trimFrequency;
    private int trimCounter = 0;

    TrimmableDbTable(String table, DbKey.Factory<T> dbKeyFactory, boolean multiversion) {
        super(table);
        this.dbKeyFactory = dbKeyFactory;
        this.multiversion = multiversion;
        this.trimFrequency = Nxt.getIntProperty("nxt.trimFrequencyMultiplier." + table, 1);
        trimCounter = trimFrequency - 1;
        this.isFastTrimEnabled = USE_FAST_TRIMMING && checkFastTrimIndex(table);
    }

    /**
     * Check if exists an index ordering the rows by key, ascending; and for each key - by height,
     * descending. There must be only one index by the first column of the key or else we don't have
     * a guarantee that the correct index will be used
     */
    private boolean checkFastTrimIndex(String table) {
        if (!multiversion
                || !(dbKeyFactory instanceof DbKey.LongKeyFactory)
                    && !(dbKeyFactory instanceof DbKey.LinkKeyFactory)) {
            return false;
        }
        table = table.toUpperCase(Locale.ROOT);

        keyColumns = Arrays.stream(dbKeyFactory.getPKColumns().split(",")).
                map(String::trim).map(String::toUpperCase).toArray(String[]::new);

        try (Connection con = db.getConnection();
             PreparedStatement pstmtIndex = con.prepareStatement(
                     "SELECT ID, INDEX_NAME, ASC_OR_DESC FROM INFORMATION_SCHEMA.INDEXES " +
                     " WHERE TABLE_NAME = '" + table + "' AND COLUMN_NAME = '" + keyColumns[0] + "' " +
                     " AND ORDINAL_POSITION = 1");
             PreparedStatement pstmtColumns = con.prepareStatement(
                     "SELECT COLUMN_NAME, ASC_OR_DESC FROM INFORMATION_SCHEMA.INDEXES " +
                             " WHERE ID = ? ORDER BY ORDINAL_POSITION")) {
            try (ResultSet rs = pstmtIndex.executeQuery()) {
                if (rs.next()) {
                    String indexName = rs.getString("INDEX_NAME");
                    if (!"A".equals(rs.getString("ASC_OR_DESC"))) {
                        Logger.logWarningMessage("Column " + keyColumns[0] + " in index " + indexName +
                                " is descending. Fast trimming is disabled for table " + table);
                        return false;
                    }
                    int indexId = rs.getInt("ID");
                    if (rs.next()) {
                        Logger.logWarningMessage("More than one index on column " + keyColumns[0] +
                                " is found for table " + table + ": " + indexName + " and " +
                                rs.getString("INDEX_NAME") + ". Fast trimming is disabled");
                        return false;
                    }
                    pstmtColumns.setInt(1, indexId);
                    try (ResultSet rsColumns = pstmtColumns.executeQuery()) {
                        int column = 0;
                        while (rsColumns.next()) {
                            String columnName = rsColumns.getString("COLUMN_NAME");
                            String ascOrDesc = rsColumns.getString("ASC_OR_DESC");
                            if (column < keyColumns.length) {
                                if (!keyColumns[column].equalsIgnoreCase(columnName)) {
                                    Logger.logWarningMessage("Column in position " + (column + 1) +
                                            " in " + indexName + " is '" + columnName + "' instead of '"
                                            +  keyColumns[0] + "'." +
                                            " Fast trimming is disabled for table " + table);
                                    return false;
                                }
                                if (!"A".equals(ascOrDesc)) {
                                    Logger.logWarningMessage("Index " + indexName + " is not ASC for column '" +
                                            columnName + "'. Fast trimming is disabled for table " + table);
                                    return false;
                                }
                            } else if (column == keyColumns.length) {
                                if (!"height".equalsIgnoreCase(columnName)) {
                                    Logger.logWarningMessage("Column in position " + (column + 1) + " in " + indexName +
                                            " is " + columnName + " instead of 'height'." +
                                            " Fast trimming is disabled for table " + table);
                                    return false;
                                }
                                if (!"D".equals(ascOrDesc)) {
                                    Logger.logWarningMessage("Index " + indexName + " is not DESC for 'height'." +
                                            " Fast trimming is disabled for table " + table);
                                    return false;
                                }
                            }
                            column++;
                        }
                        if (column < keyColumns.length + 1) {
                            Logger.logWarningMessage("Not enough columns in index " + indexName + "." +
                                    " Fast trimming is disabled for table " + table);
                            return false;
                        }
                    }
                    return true;
                } else {
                    Logger.logWarningMessage("No index on column '" + keyColumns[0] + "' is found for table " +
                            table + ". Fast trimming is disabled");
                    return false;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    @Override
    public void popOffTo(int height) {
        if (multiversion) {
            VersionedEntityDbTable.popOff(db, table, height, dbKeyFactory);
        } else {
            super.popOffTo(height);
        }
    }

    @Override
    public void trim(int height) {
        if (multiversion) {
            trimCounter++;
            if (trimCounter >= trimFrequency) {
                trimCounter = 0;
                if (isFastTrimEnabled) {
                    fastTrim(height);
                } else {
                    VersionedEntityDbTable.trim(db, table, height, dbKeyFactory);
                }
            }
        } else {
            super.trim(height);
        }
    }

    private static class TrimContext {
        private int trimHeight;
        private Value[] prevRowKey;
        private boolean isDeleted;
        private boolean isHighestRowBeforeTrimSkipped;
        private long lastBatchMarker;
    }

    private static ThreadLocal<TrimContext> trimContext = ThreadLocal.withInitial(TrimContext::new);

    /**
     * Uses a stored procedure. Requires an index on (< key column(s) > ASC, height DESC)
     */
    private void fastTrim(final int height) {
        TrimContext context = trimContext.get();
        context.trimHeight = height;
        context.lastBatchMarker = Long.MIN_VALUE;

        try (Connection con = db.getConnection();
             PreparedStatement pstmt = con.prepareStatement("DELETE FROM " + table + " WHERE " +
                     //forces the sort order by the fast trim index (see checkFastTrimIndex) and skips all keys until
                     // lastBatchMarker because they were already checked during previous batch
                     keyColumns[0] + " >= ? " +
                     " AND CAN_BE_TRIMMED(height, latest, " + dbKeyFactory.getPKColumns() + ") LIMIT " + Constants.BATCH_COMMIT_SIZE)) {
            int deleted;
            do {
                context.isHighestRowBeforeTrimSkipped = false;
                pstmt.setLong(1, context.lastBatchMarker);
                deleted = pstmt.executeUpdate();
                db.commitTransaction();
            } while (deleted >= Constants.BATCH_COMMIT_SIZE);
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    public static boolean canBeTrimmed(int height, boolean latest, Value... key) {
        TrimContext context = trimContext.get();
        context.lastBatchMarker = key[0].getLong();
        boolean result = false;
        if (!Arrays.equals(context.prevRowKey, key)) {
            context.prevRowKey = key;
            context.isDeleted = height < context.trimHeight && !latest;
            context.isHighestRowBeforeTrimSkipped = false;
        }
        if (height < context.trimHeight && height >= 0) {
            if (context.isDeleted) {
                result = true;
            } else {
                if (context.isHighestRowBeforeTrimSkipped) {
                    result = true;
                } else {
                    context.isHighestRowBeforeTrimSkipped = true;
                }
            }
        }
        //Logger.logDebugMessage("stored procedure for " + id + " " + height + " " + latest + " " + result);
        return result;
    }

}
