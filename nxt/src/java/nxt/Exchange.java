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

package nxt;

import nxt.db.DbClause;
import nxt.db.DbIterator;
import nxt.db.DbKey;
import nxt.db.DbUtils;
import nxt.db.EntityDbTable;
import nxt.util.Listener;
import nxt.util.Listeners;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class Exchange {

    public enum Event {
        EXCHANGE
    }

    private static final Listeners<Exchange,Event> listeners = new Listeners<>();

    private static final DbKey.LinkKeyFactory<Exchange> exchangeDbKeyFactory = new DbKey.LinkKeyFactory<Exchange>("transaction_id", "offer_id") {

        @Override
        public DbKey newKey(Exchange exchange) {
            return exchange.dbKey;
        }

    };

    private static final EntityDbTable<Exchange> exchangeTable = new EntityDbTable<Exchange>("exchange", exchangeDbKeyFactory) {

        @Override
        protected Exchange load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
            return new Exchange(rs, dbKey);
        }

        @Override
        protected void save(Connection con, Exchange exchange) throws SQLException {
            exchange.save(con);
        }

    };

    public static DbIterator<Exchange> getAllExchanges(int from, int to) {
        return exchangeTable.getAll(from, to);
    }

    public static int getCount() {
        return exchangeTable.getCount();
    }

    public static boolean addListener(Listener<Exchange> listener, Event eventType) {
        return listeners.addListener(listener, eventType);
    }

    public static boolean removeListener(Listener<Exchange> listener, Event eventType) {
        return listeners.removeListener(listener, eventType);
    }

    public static DbIterator<Exchange> getCurrencyExchanges(long currencyId, int from, int to) {
        return exchangeTable.getManyBy(new DbClause.LongClause("currency_id", currencyId), from, to);
    }

    public static List<Exchange> getLastExchanges(long[] currencyIds) {
        try (Connection con = Db.db.getConnection();
             PreparedStatement pstmt = con.prepareStatement("SELECT * FROM exchange WHERE currency_id = ? ORDER BY height DESC, db_id DESC LIMIT 1")) {
            List<Exchange> result = new ArrayList<>();
            for (long currencyId : currencyIds) {
                pstmt.setLong(1, currencyId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        result.add(new Exchange(rs, null));
                    }
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    public static DbIterator<Exchange> getAccountExchanges(long accountId, int from, int to) {
        Connection con = null;
        try {
            con = Db.db.getConnection();
            PreparedStatement pstmt = con.prepareStatement("SELECT * FROM exchange WHERE seller_id = ?"
                    + " UNION ALL SELECT * FROM exchange WHERE buyer_id = ? AND seller_id <> ? ORDER BY height DESC, db_id DESC"
                    + DbUtils.limitsClause(from, to));
            int i = 0;
            pstmt.setLong(++i, accountId);
            pstmt.setLong(++i, accountId);
            pstmt.setLong(++i, accountId);
            DbUtils.setLimits(++i, pstmt, from, to);
            return exchangeTable.getManyBy(con, pstmt, false);
        } catch (SQLException e) {
            DbUtils.close(con);
            throw new RuntimeException(e.toString(), e);
        }
    }

    public static DbIterator<Exchange> getAccountCurrencyExchanges(long accountId, long currencyId, int from, int to) {
        Connection con = null;
        try {
            con = Db.db.getConnection();
            PreparedStatement pstmt = con.prepareStatement("SELECT * FROM exchange WHERE seller_id = ? AND currency_id = ?"
                    + " UNION ALL SELECT * FROM exchange WHERE buyer_id = ? AND seller_id <> ? AND currency_id = ? ORDER BY height DESC, db_id DESC"
                    + DbUtils.limitsClause(from, to));
            int i = 0;
            pstmt.setLong(++i, accountId);
            pstmt.setLong(++i, currencyId);
            pstmt.setLong(++i, accountId);
            pstmt.setLong(++i, accountId);
            pstmt.setLong(++i, currencyId);
            DbUtils.setLimits(++i, pstmt, from, to);
            return exchangeTable.getManyBy(con, pstmt, false);
        } catch (SQLException e) {
            DbUtils.close(con);
            throw new RuntimeException(e.toString(), e);
        }
    }

    public static DbIterator<Exchange> getExchanges(long transactionId) {
        return exchangeTable.getManyBy(new DbClause.LongClause("transaction_id", transactionId), 0, -1);
    }

    public static DbIterator<Exchange> getOfferExchanges(long offerId, int from, int to) {
        return exchangeTable.getManyBy(new DbClause.LongClause("offer_id", offerId), from, to);
    }

    public static int getExchangeCount(long currencyId) {
        return exchangeTable.getCount(new DbClause.LongClause("currency_id", currencyId));
    }

    static Exchange addExchange(Transaction transaction, long currencyId, CurrencyExchangeOffer offer, long sellerId, long buyerId, long units) {
        Exchange exchange = new Exchange(transaction.getId(), currencyId, offer, sellerId, buyerId, units);
        exchangeTable.insert(exchange);
        listeners.notify(exchange, Event.EXCHANGE);
        return exchange;
    }

    static void init() {}


    private final long transactionId;
    private final int timestamp;
    private final long currencyId;
    private final long blockId;
    private final int height;
    private final long offerId;
    private final long sellerId;
    private final long buyerId;
    private final DbKey dbKey;
    private final long units;
    private final long rate;

    private Exchange(long transactionId, long currencyId, CurrencyExchangeOffer offer, long sellerId, long buyerId, long units) {
        Block block = Nxt.getBlockchain().getLastBlock();
        this.transactionId = transactionId;
        this.blockId = block.getId();
        this.height = block.getHeight();
        this.currencyId = currencyId;
        this.timestamp = block.getTimestamp();
        this.offerId = offer.getId();
        this.sellerId = sellerId;
        this.buyerId = buyerId;
        this.dbKey = exchangeDbKeyFactory.newKey(this.transactionId, this.offerId);
        this.units = units;
        this.rate = offer.getRateNQT();
    }

    private Exchange(ResultSet rs, DbKey dbKey) throws SQLException {
        this.transactionId = rs.getLong("transaction_id");
        this.currencyId = rs.getLong("currency_id");
        this.blockId = rs.getLong("block_id");
        this.offerId = rs.getLong("offer_id");
        this.sellerId = rs.getLong("seller_id");
        this.buyerId = rs.getLong("buyer_id");
        this.dbKey = dbKey;
        this.units = rs.getLong("units");
        this.rate = rs.getLong("rate");
        this.timestamp = rs.getInt("timestamp");
        this.height = rs.getInt("height");
    }

    private void save(Connection con) throws SQLException {
        try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO exchange (transaction_id, currency_id, block_id, "
                + "offer_id, seller_id, buyer_id, units, rate, timestamp, height) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            int i = 0;
            pstmt.setLong(++i, this.transactionId);
            pstmt.setLong(++i, this.currencyId);
            pstmt.setLong(++i, this.blockId);
            pstmt.setLong(++i, this.offerId);
            pstmt.setLong(++i, this.sellerId);
            pstmt.setLong(++i, this.buyerId);
            pstmt.setLong(++i, this.units);
            pstmt.setLong(++i, this.rate);
            pstmt.setInt(++i, this.timestamp);
            pstmt.setInt(++i, this.height);
            pstmt.executeUpdate();
        }
    }

    public long getTransactionId() {
        return transactionId;
    }

    public long getBlockId() {
        return blockId;
    }

    public long getOfferId() {
        return offerId;
    }

    public long getSellerId() {
        return sellerId;
    }

    public long getBuyerId() {
        return buyerId;
    }

    public long getUnits() {
        return units;
    }

    public long getRate() {
        return rate;
    }
    
    public long getCurrencyId() {
        return currencyId;
    }
    
    public int getTimestamp() {
        return timestamp;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return "Exchange currency: " + Long.toUnsignedString(currencyId) + " offer: " + Long.toUnsignedString(offerId)
                + " rate: " + rate + " units: " + units + " height: " + height + " transaction: " + Long.toUnsignedString(transactionId);
    }

}
