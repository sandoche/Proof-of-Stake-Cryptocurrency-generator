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
import nxt.db.VersionedEntityDbTable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class CurrencySellOffer extends CurrencyExchangeOffer {

    private static final DbKey.LongKeyFactory<CurrencySellOffer> sellOfferDbKeyFactory = new DbKey.LongKeyFactory<CurrencySellOffer>("id") {

        @Override
        public DbKey newKey(CurrencySellOffer sell) {
            return sell.dbKey;
        }

    };

    private static final VersionedEntityDbTable<CurrencySellOffer> sellOfferTable = new VersionedEntityDbTable<CurrencySellOffer>("sell_offer", sellOfferDbKeyFactory) {

        @Override
        protected CurrencySellOffer load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
            return new CurrencySellOffer(rs, dbKey);
        }

        @Override
        protected void save(Connection con, CurrencySellOffer sell) throws SQLException {
            sell.save(con, table);
        }

    };

    public static int getCount() {
        return sellOfferTable.getCount();
    }

    public static CurrencySellOffer getOffer(long id) {
        return sellOfferTable.get(sellOfferDbKeyFactory.newKey(id));
    }

    public static DbIterator<CurrencySellOffer> getAll(int from, int to) {
        return sellOfferTable.getAll(from, to);
    }

    public static DbIterator<CurrencySellOffer> getOffers(Currency currency, int from, int to) {
        return getCurrencyOffers(currency.getId(), false, from, to);
    }

    public static DbIterator<CurrencySellOffer> getCurrencyOffers(long currencyId, boolean availableOnly, int from, int to) {
        DbClause dbClause = new DbClause.LongClause("currency_id", currencyId);
        if (availableOnly) {
            dbClause = dbClause.and(availableOnlyDbClause);
        }
        return sellOfferTable.getManyBy(dbClause, from, to, " ORDER BY rate ASC, creation_height ASC, transaction_height ASC, transaction_index ASC ");
    }

    public static DbIterator<CurrencySellOffer> getAccountOffers(long accountId, boolean availableOnly, int from, int to) {
        DbClause dbClause = new DbClause.LongClause("account_id", accountId);
        if (availableOnly) {
            dbClause = dbClause.and(availableOnlyDbClause);
        }
        return sellOfferTable.getManyBy(dbClause, from, to, " ORDER BY rate ASC, creation_height ASC, transaction_height ASC, transaction_index ASC ");
    }

    public static CurrencySellOffer getOffer(Currency currency, Account account) {
        return getOffer(currency.getId(), account.getId());
    }

    public static CurrencySellOffer getOffer(final long currencyId, final long accountId) {
        return sellOfferTable.getBy(new DbClause.LongClause("currency_id", currencyId).and(new DbClause.LongClause("account_id", accountId)));
    }

    public static DbIterator<CurrencySellOffer> getOffers(DbClause dbClause, int from, int to) {
        return sellOfferTable.getManyBy(dbClause, from, to);
    }

    public static DbIterator<CurrencySellOffer> getOffers(DbClause dbClause, int from, int to, String sort) {
        return sellOfferTable.getManyBy(dbClause, from, to, sort);
    }

    static void addOffer(Transaction transaction, Attachment.MonetarySystemPublishExchangeOffer attachment) {
        sellOfferTable.insert(new CurrencySellOffer(transaction, attachment));
    }

    static void remove(CurrencySellOffer sellOffer) {
        sellOfferTable.delete(sellOffer);
    }

    static void init() {}

    private final DbKey dbKey;

    private CurrencySellOffer(Transaction transaction, Attachment.MonetarySystemPublishExchangeOffer attachment) {
        super(transaction.getId(), attachment.getCurrencyId(), transaction.getSenderId(), attachment.getSellRateNQT(),
                attachment.getTotalSellLimit(), attachment.getInitialSellSupply(), attachment.getExpirationHeight(), transaction.getHeight(),
                transaction.getIndex());
        this.dbKey = sellOfferDbKeyFactory.newKey(id);
    }

    private CurrencySellOffer(ResultSet rs, DbKey dbKey) throws SQLException {
        super(rs);
        this.dbKey = dbKey;
    }

    @Override
    public CurrencyBuyOffer getCounterOffer() {
        return CurrencyBuyOffer.getOffer(id);
    }

    long increaseSupply(long delta) {
        long excess = super.increaseSupply(delta);
        sellOfferTable.insert(this);
        return excess;
    }

    void decreaseLimitAndSupply(long delta) {
        super.decreaseLimitAndSupply(delta);
        sellOfferTable.insert(this);
    }
}
