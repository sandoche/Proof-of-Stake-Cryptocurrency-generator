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
import nxt.db.VersionedEntityDbTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Each CurrencyFounder instance represents a single founder contribution for a non issued currency
 * Once the currency is issued all founder contributions are removed
 * In case the currency is not issued because of insufficient funding, all funds are returned to the founders
 */
public class CurrencyFounder {

    private static final DbKey.LinkKeyFactory<CurrencyFounder> currencyFounderDbKeyFactory = new DbKey.LinkKeyFactory<CurrencyFounder>("currency_id", "account_id") {

        @Override
        public DbKey newKey(CurrencyFounder currencyFounder) {
            return currencyFounder.dbKey;
        }

    };

    private static final VersionedEntityDbTable<CurrencyFounder> currencyFounderTable = new VersionedEntityDbTable<CurrencyFounder>("currency_founder", currencyFounderDbKeyFactory) {

        @Override
        protected CurrencyFounder load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
            return new CurrencyFounder(rs, dbKey);
        }

        @Override
        protected void save(Connection con, CurrencyFounder currencyFounder) throws SQLException {
            currencyFounder.save(con);
        }

        @Override
        public String defaultSort() {
            return " ORDER BY height DESC ";
        }

    };

    static void init() {}

    private final DbKey dbKey;
    private final long currencyId;
    private final long accountId;
    private long amountPerUnitNQT;

    private CurrencyFounder(long currencyId, long accountId, long amountPerUnitNQT) {
        this.currencyId = currencyId;
        this.dbKey = currencyFounderDbKeyFactory.newKey(currencyId, accountId);
        this.accountId = accountId;
        this.amountPerUnitNQT = amountPerUnitNQT;
    }

    private CurrencyFounder(ResultSet rs, DbKey dbKey) throws SQLException {
        this.currencyId = rs.getLong("currency_id");
        this.accountId = rs.getLong("account_id");
        this.dbKey = dbKey;
        this.amountPerUnitNQT = rs.getLong("amount");
    }

    private void save(Connection con) throws SQLException {
        try (PreparedStatement pstmt = con.prepareStatement("MERGE INTO currency_founder (currency_id, account_id, amount, height, latest) "
                + "KEY (currency_id, account_id, height) VALUES (?, ?, ?, ?, TRUE)")) {
            int i = 0;
            pstmt.setLong(++i, this.getCurrencyId());
            pstmt.setLong(++i, this.getAccountId());
            pstmt.setLong(++i, this.getAmountPerUnitNQT());
            pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
            pstmt.executeUpdate();
        }
    }

    public long getCurrencyId() {
        return currencyId;
    }

    public long getAccountId() {
        return accountId;
    }

    public long getAmountPerUnitNQT() {
        return amountPerUnitNQT;
    }

    static void addOrUpdateFounder(long currencyId, long accountId, long amount) {
        CurrencyFounder founder = getFounder(currencyId, accountId);
        if (founder == null) {
            founder = new CurrencyFounder(currencyId, accountId, amount);
        } else {
            founder.amountPerUnitNQT += amount;
        }
        currencyFounderTable.insert(founder);
    }

    public static CurrencyFounder getFounder(long currencyId, long accountId) {
        return currencyFounderTable.get(currencyFounderDbKeyFactory.newKey(currencyId, accountId));
    }

    public static DbIterator<CurrencyFounder> getCurrencyFounders(long currencyId, int from, int to) {
        return currencyFounderTable.getManyBy(new DbClause.LongClause("currency_id", currencyId), from, to);
    }

    public static DbIterator<CurrencyFounder> getFounderCurrencies(long accountId, int from, int to) {
        return currencyFounderTable.getManyBy(new DbClause.LongClause("account_id", accountId), from, to);
    }

    static void remove(long currencyId) {
        List<CurrencyFounder> founders = new ArrayList<>();
        try (DbIterator<CurrencyFounder> currencyFounders = CurrencyFounder.getCurrencyFounders(currencyId, 0, Integer.MAX_VALUE)) {
            for (CurrencyFounder founder : currencyFounders) {
                founders.add(founder);
            }
        }
        founders.forEach(currencyFounderTable::delete);
    }
}
