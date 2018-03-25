/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2018 Jelurida IP B.V.
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

package nxt;

import nxt.db.DbClause;
import nxt.db.DbIterator;
import nxt.db.DbKey;
import nxt.db.EntityDbTable;
import nxt.util.Listener;
import nxt.util.Listeners;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class AssetDelete {

    public enum Event {
        ASSET_DELETE
    }

    private static final Listeners<AssetDelete,Event> listeners = new Listeners<>();

    private static final DbKey.LongKeyFactory<AssetDelete> deleteDbKeyFactory = new DbKey.LongKeyFactory<AssetDelete>("id") {

        @Override
        public DbKey newKey(AssetDelete assetDelete) {
            return assetDelete.dbKey;
        }

    };

    private static final EntityDbTable<AssetDelete> assetDeleteTable = new EntityDbTable<AssetDelete>("asset_delete", deleteDbKeyFactory) {

        @Override
        protected AssetDelete load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
            return new AssetDelete(rs, dbKey);
        }

        @Override
        protected void save(Connection con, AssetDelete assetDelete) throws SQLException {
            assetDelete.save(con);
        }

    };

    public static boolean addListener(Listener<AssetDelete> listener, Event eventType) {
        return listeners.addListener(listener, eventType);
    }

    public static boolean removeListener(Listener<AssetDelete> listener, Event eventType) {
        return listeners.removeListener(listener, eventType);
    }

    public static DbIterator<AssetDelete> getAssetDeletes(long assetId, int from, int to) {
        return assetDeleteTable.getManyBy(new DbClause.LongClause("asset_id", assetId), from, to);
    }

    public static DbIterator<AssetDelete> getAccountAssetDeletes(long accountId, int from, int to) {
        return assetDeleteTable.getManyBy(new DbClause.LongClause("account_id", accountId), from, to, " ORDER BY height DESC, db_id DESC ");
    }

    public static DbIterator<AssetDelete> getAccountAssetDeletes(long accountId, long assetId, int from, int to) {
        return assetDeleteTable.getManyBy(new DbClause.LongClause("account_id", accountId).and(new DbClause.LongClause("asset_id", assetId)),
                from, to, " ORDER BY height DESC, db_id DESC ");
    }

    static AssetDelete addAssetDelete(Transaction transaction, long assetId, long quantityQNT) {
        AssetDelete assetDelete = new AssetDelete(transaction, assetId, quantityQNT);
        assetDeleteTable.insert(assetDelete);
        listeners.notify(assetDelete, Event.ASSET_DELETE);
        return assetDelete;
    }

    static void init() {}


    private final long id;
    private final DbKey dbKey;
    private final long assetId;
    private final int height;
    private final long accountId;
    private final long quantityQNT;
    private final int timestamp;

    private AssetDelete(Transaction transaction, long assetId, long quantityQNT) {
        this.id = transaction.getId();
        this.dbKey = deleteDbKeyFactory.newKey(this.id);
        this.assetId = assetId;
        this.accountId = transaction.getSenderId();
        this.quantityQNT = quantityQNT;
        this.timestamp = Nxt.getBlockchain().getLastBlockTimestamp();
        this.height = Nxt.getBlockchain().getHeight();
    }

    private AssetDelete(ResultSet rs, DbKey dbKey) throws SQLException {
        this.id = rs.getLong("id");
        this.dbKey = dbKey;
        this.assetId = rs.getLong("asset_id");
        this.accountId = rs.getLong("account_id");
        this.quantityQNT = rs.getLong("quantity");
        this.timestamp = rs.getInt("timestamp");
        this.height = rs.getInt("height");
    }

    private void save(Connection con) throws SQLException {
        try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO asset_delete (id, asset_id, "
                + "account_id, quantity, timestamp, height) "
                + "VALUES (?, ?, ?, ?, ?, ?)")) {
            int i = 0;
            pstmt.setLong(++i, this.id);
            pstmt.setLong(++i, this.assetId);
            pstmt.setLong(++i, this.accountId);
            pstmt.setLong(++i, this.quantityQNT);
            pstmt.setInt(++i, this.timestamp);
            pstmt.setInt(++i, this.height);
            pstmt.executeUpdate();
        }
    }

    public long getId() {
        return id;
    }

    public long getAssetId() { return assetId; }

    public long getAccountId() {
        return accountId;
    }

    public long getQuantityQNT() { return quantityQNT; }

    public int getTimestamp() {
        return timestamp;
    }

    public int getHeight() {
        return height;
    }

}
