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
import nxt.db.EntityDbTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class ExchangeRequest {

    private static final DbKey.LongKeyFactory<ExchangeRequest> exchangeRequestDbKeyFactory = new DbKey.LongKeyFactory<ExchangeRequest>("id") {

        @Override
        public DbKey newKey(ExchangeRequest exchangeRequest) {
            return exchangeRequest.dbKey;
        }

    };

    private static final EntityDbTable<ExchangeRequest> exchangeRequestTable = new EntityDbTable<ExchangeRequest>("exchange_request", exchangeRequestDbKeyFactory) {

        @Override
        protected ExchangeRequest load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
            return new ExchangeRequest(rs, dbKey);
        }

        @Override
        protected void save(Connection con, ExchangeRequest exchangeRequest) throws SQLException {
            exchangeRequest.save(con);
        }

    };

    public static DbIterator<ExchangeRequest> getAllExchangeRequests(int from, int to) {
        return exchangeRequestTable.getAll(from, to);
    }

    public static int getCount() {
        return exchangeRequestTable.getCount();
    }

    public static ExchangeRequest getExchangeRequest(long transactionId) {
        return exchangeRequestTable.get(exchangeRequestDbKeyFactory.newKey(transactionId));
    }

    public static DbIterator<ExchangeRequest> getCurrencyExchangeRequests(long currencyId, int from, int to) {
        return exchangeRequestTable.getManyBy(new DbClause.LongClause("currency_id", currencyId), from, to);
    }

    public static DbIterator<ExchangeRequest> getAccountExchangeRequests(long accountId, int from, int to) {
        return exchangeRequestTable.getManyBy(new DbClause.LongClause("account_id", accountId), from, to);
    }

    public static DbIterator<ExchangeRequest> getAccountCurrencyExchangeRequests(long accountId, long currencyId, int from, int to) {
        return exchangeRequestTable.getManyBy(new DbClause.LongClause("account_id", accountId).and(new DbClause.LongClause("currency_id", currencyId)), from, to);
    }

    static void addExchangeRequest(Transaction transaction, Attachment.MonetarySystemExchangeBuy attachment) {
        ExchangeRequest exchangeRequest = new ExchangeRequest(transaction, attachment);
        exchangeRequestTable.insert(exchangeRequest);
    }

    static void addExchangeRequest(Transaction transaction, Attachment.MonetarySystemExchangeSell attachment) {
        ExchangeRequest exchangeRequest = new ExchangeRequest(transaction, attachment);
        exchangeRequestTable.insert(exchangeRequest);
    }

    static void init() {}


    private final long id;
    private final long accountId;
    private final long currencyId;
    private final int height;
    private final int timestamp;
    private final DbKey dbKey;
    private final long units;
    private final long rate;
    private final boolean isBuy;

    private ExchangeRequest(Transaction transaction, Attachment.MonetarySystemExchangeBuy attachment) {
        this(transaction, attachment, true);
    }

    private ExchangeRequest(Transaction transaction, Attachment.MonetarySystemExchangeSell attachment) {
        this(transaction, attachment, false);
    }

    private ExchangeRequest(Transaction transaction, Attachment.MonetarySystemExchange attachment, boolean isBuy) {
        this.id = transaction.getId();
        this.dbKey = exchangeRequestDbKeyFactory.newKey(this.id);
        this.accountId = transaction.getSenderId();
        this.currencyId = attachment.getCurrencyId();
        this.units = attachment.getUnits();
        this.rate = attachment.getRateNQT();
        this.isBuy = isBuy;
        Block block = Nxt.getBlockchain().getLastBlock();
        this.height = block.getHeight();
        this.timestamp = block.getTimestamp();
    }

    private ExchangeRequest(ResultSet rs, DbKey dbKey) throws SQLException {
        this.id = rs.getLong("id");
        this.dbKey = dbKey;
        this.accountId = rs.getLong("account_id");
        this.currencyId = rs.getLong("currency_id");
        this.units = rs.getLong("units");
        this.rate = rs.getLong("rate");
        this.isBuy = rs.getBoolean("is_buy");
        this.timestamp = rs.getInt("timestamp");
        this.height = rs.getInt("height");
    }

    private void save(Connection con) throws SQLException {
        try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO exchange_request (id, account_id, currency_id, "
                + "units, rate, is_buy, timestamp, height) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            int i = 0;
            pstmt.setLong(++i, this.id);
            pstmt.setLong(++i, this.accountId);
            pstmt.setLong(++i, this.currencyId);
            pstmt.setLong(++i, this.units);
            pstmt.setLong(++i, this.rate);
            pstmt.setBoolean(++i, this.isBuy);
            pstmt.setInt(++i, this.timestamp);
            pstmt.setInt(++i, this.height);
            pstmt.executeUpdate();
        }
    }

    public long getId() {
        return id;
    }

    public long getAccountId() {
        return accountId;
    }

    public long getCurrencyId() {
        return currencyId;
    }

    public long getUnits() {
        return units;
    }

    public long getRate() {
        return rate;
    }

    public boolean isBuy() {
        return isBuy;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public int getHeight() {
        return height;
    }

}
