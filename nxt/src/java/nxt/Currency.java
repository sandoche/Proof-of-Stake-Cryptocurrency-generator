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

import nxt.AccountLedger.LedgerEvent;
import nxt.db.DbClause;
import nxt.db.DbIterator;
import nxt.db.DbKey;
import nxt.db.VersionedEntityDbTable;
import nxt.util.Listener;
import nxt.util.Listeners;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("UnusedDeclaration")
public final class Currency {

    public enum Event {
        BEFORE_DISTRIBUTE_CROWDFUNDING, BEFORE_UNDO_CROWDFUNDING, BEFORE_DELETE
    }

    private static final DbKey.LongKeyFactory<Currency> currencyDbKeyFactory = new DbKey.LongKeyFactory<Currency>("id") {

        @Override
        public DbKey newKey(Currency currency) {
            return currency.dbKey == null ? newKey(currency.currencyId) : currency.dbKey;
        }

    };

    private static final VersionedEntityDbTable<Currency> currencyTable = new VersionedEntityDbTable<Currency>("currency", currencyDbKeyFactory, "code,name,description") {

        @Override
        protected Currency load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
            return new Currency(rs, dbKey);
        }

        @Override
        protected void save(Connection con, Currency currency) throws SQLException {
            currency.save(con);
        }

        @Override
        public String defaultSort() {
            return " ORDER BY creation_height DESC ";
        }

    };

    private static final class CurrencySupply {

        private final DbKey dbKey;
        private final long currencyId;
        private long currentSupply;
        private long currentReservePerUnitNQT;

        private CurrencySupply(Currency currency) {
            this.currencyId = currency.currencyId;
            this.dbKey = currencySupplyDbKeyFactory.newKey(this.currencyId);
        }

        private CurrencySupply(ResultSet rs, DbKey dbKey) throws SQLException {
            this.currencyId = rs.getLong("id");
            this.dbKey = dbKey;
            this.currentSupply = rs.getLong("current_supply");
            this.currentReservePerUnitNQT = rs.getLong("current_reserve_per_unit_nqt");
        }

        private void save(Connection con) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("MERGE INTO currency_supply (id, current_supply, "
                    + "current_reserve_per_unit_nqt, height, latest) "
                    + "KEY (id, height) VALUES (?, ?, ?, ?, TRUE)")) {
                int i = 0;
                pstmt.setLong(++i, this.currencyId);
                pstmt.setLong(++i, this.currentSupply);
                pstmt.setLong(++i, this.currentReservePerUnitNQT);
                pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
                pstmt.executeUpdate();
            }
        }
    }

    private static final DbKey.LongKeyFactory<CurrencySupply> currencySupplyDbKeyFactory = new DbKey.LongKeyFactory<CurrencySupply>("id") {

        @Override
        public DbKey newKey(CurrencySupply currencySupply) {
            return currencySupply.dbKey;
        }

    };

    private static final VersionedEntityDbTable<CurrencySupply> currencySupplyTable = new VersionedEntityDbTable<CurrencySupply>("currency_supply", currencySupplyDbKeyFactory) {

        @Override
        protected CurrencySupply load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
            return new CurrencySupply(rs, dbKey);
        }

        @Override
        protected void save(Connection con, CurrencySupply currencySupply) throws SQLException {
            currencySupply.save(con);
        }

    };

    private static final Listeners<Currency,Event> listeners = new Listeners<>();

    public static boolean addListener(Listener<Currency> listener, Event eventType) {
        return listeners.addListener(listener, eventType);
    }

    public static boolean removeListener(Listener<Currency> listener, Event eventType) {
        return listeners.removeListener(listener, eventType);
    }

    public static DbIterator<Currency> getAllCurrencies(int from, int to) {
        return currencyTable.getAll(from, to);
    }

    public static int getCount() {
        return currencyTable.getCount();
    }

    public static Currency getCurrency(long id) {
        return currencyTable.get(currencyDbKeyFactory.newKey(id));
    }

    public static Currency getCurrencyByName(String name) {
        return currencyTable.getBy(new DbClause.StringClause("name_lower", name.toLowerCase(Locale.ROOT)));
    }

    public static Currency getCurrencyByCode(String code) {
        return currencyTable.getBy(new DbClause.StringClause("code", code.toUpperCase(Locale.ROOT)));
    }

    public static DbIterator<Currency> getCurrencyIssuedBy(long accountId, int from, int to) {
        return currencyTable.getManyBy(new DbClause.LongClause("account_id", accountId), from, to);
    }

    public static DbIterator<Currency> searchCurrencies(String query, int from, int to) {
        return currencyTable.search(query, DbClause.EMPTY_CLAUSE, from, to, " ORDER BY ft.score DESC, currency.creation_height DESC ");
    }

    static void addCurrency(LedgerEvent event, long eventId, Transaction transaction, Account senderAccount,
                            Attachment.MonetarySystemCurrencyIssuance attachment) {
        Currency oldCurrency;
        if ((oldCurrency = Currency.getCurrencyByCode(attachment.getCode())) != null) {
            oldCurrency.delete(event, eventId, senderAccount);
        }
        if ((oldCurrency = Currency.getCurrencyByCode(attachment.getName())) != null) {
            oldCurrency.delete(event, eventId, senderAccount);
        }
        if ((oldCurrency = Currency.getCurrencyByName(attachment.getName())) != null) {
            oldCurrency.delete(event, eventId, senderAccount);
        }
        if ((oldCurrency = Currency.getCurrencyByName(attachment.getCode())) != null) {
            oldCurrency.delete(event, eventId, senderAccount);
        }
        Currency currency = new Currency(transaction, attachment);
        currencyTable.insert(currency);
        if (currency.is(CurrencyType.MINTABLE) || currency.is(CurrencyType.RESERVABLE)) {
            CurrencySupply currencySupply = currency.getSupplyData();
            currencySupply.currentSupply = attachment.getInitialSupply();
            currencySupplyTable.insert(currencySupply);
        }

    }

    static {
        Nxt.getBlockchainProcessor().addListener(new CrowdFundingListener(), BlockchainProcessor.Event.AFTER_BLOCK_APPLY);
    }

    static void init() {}

    private final long currencyId;

    private final DbKey dbKey;
    private final long accountId;
    private final String name;
    private final String code;
    private final String description;
    private final int type;
    private final long maxSupply;
    private final long reserveSupply;
    private final int creationHeight;
    private final int issuanceHeight;
    private final long minReservePerUnitNQT;
    private final int minDifficulty;
    private final int maxDifficulty;
    private final byte ruleset;
    private final byte algorithm;
    private final byte decimals;
    private final long initialSupply;
    private CurrencySupply currencySupply;

    private Currency(Transaction transaction, Attachment.MonetarySystemCurrencyIssuance attachment) {
        this.currencyId = transaction.getId();
        this.dbKey = currencyDbKeyFactory.newKey(this.currencyId);
        this.accountId = transaction.getSenderId();
        this.name = attachment.getName();
        this.code = attachment.getCode();
        this.description = attachment.getDescription();
        this.type = attachment.getType();
        this.initialSupply = attachment.getInitialSupply();
        this.reserveSupply = attachment.getReserveSupply();
        this.maxSupply = attachment.getMaxSupply();
        this.creationHeight = Nxt.getBlockchain().getHeight();
        this.issuanceHeight = attachment.getIssuanceHeight();
        this.minReservePerUnitNQT = attachment.getMinReservePerUnitNQT();
        this.minDifficulty = attachment.getMinDifficulty();
        this.maxDifficulty = attachment.getMaxDifficulty();
        this.ruleset = attachment.getRuleset();
        this.algorithm = attachment.getAlgorithm();
        this.decimals = attachment.getDecimals();
    }

    private Currency(ResultSet rs, DbKey dbKey) throws SQLException {
        this.currencyId = rs.getLong("id");
        this.dbKey = dbKey;
        this.accountId = rs.getLong("account_id");
        this.name = rs.getString("name");
        this.code = rs.getString("code");
        this.description = rs.getString("description");
        this.type = rs.getInt("type");
        this.initialSupply = rs.getLong("initial_supply");
        this.reserveSupply = rs.getLong("reserve_supply");
        this.maxSupply = rs.getLong("max_supply");
        this.creationHeight = rs.getInt("creation_height");
        this.issuanceHeight = rs.getInt("issuance_height");
        this.minReservePerUnitNQT = rs.getLong("min_reserve_per_unit_nqt");
        this.minDifficulty = rs.getByte("min_difficulty") & 0xFF;
        this.maxDifficulty = rs.getByte("max_difficulty") & 0xFF;
        this.ruleset = rs.getByte("ruleset");
        this.algorithm = rs.getByte("algorithm");
        this.decimals = rs.getByte("decimals");
    }

    private void save(Connection con) throws SQLException {
        try (PreparedStatement pstmt = con.prepareStatement("MERGE INTO currency (id, account_id, name, name_lower, code, "
                + "description, type, initial_supply, reserve_supply, max_supply, creation_height, issuance_height, min_reserve_per_unit_nqt, "
                + "min_difficulty, max_difficulty, ruleset, algorithm, decimals, height, latest) "
                + "KEY (id, height) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE)")) {
            int i = 0;
            pstmt.setLong(++i, this.currencyId);
            pstmt.setLong(++i, this.accountId);
            pstmt.setString(++i, this.name);
            pstmt.setString(++i, this.name.toLowerCase(Locale.ROOT));
            pstmt.setString(++i, this.code);
            pstmt.setString(++i, this.description);
            pstmt.setInt(++i, this.type);
            pstmt.setLong(++i, this.initialSupply);
            pstmt.setLong(++i, this.reserveSupply);
            pstmt.setLong(++i, this.maxSupply);
            pstmt.setInt(++i, this.creationHeight);
            pstmt.setInt(++i, this.issuanceHeight);
            pstmt.setLong(++i, this.minReservePerUnitNQT);
            pstmt.setByte(++i, (byte)this.minDifficulty);
            pstmt.setByte(++i, (byte)this.maxDifficulty);
            pstmt.setByte(++i, this.ruleset);
            pstmt.setByte(++i, this.algorithm);
            pstmt.setByte(++i, this.decimals);
            pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
            pstmt.executeUpdate();
        }
    }

    public long getId() {
        return currencyId;
    }

    public long getAccountId() {
        return accountId;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public int getType() {
        return type;
    }

    public long getInitialSupply() {
        return initialSupply;
    }

    public long getCurrentSupply() {
        if (!is(CurrencyType.RESERVABLE) && !is(CurrencyType.MINTABLE)) {
            return initialSupply;
        }
        if (getSupplyData() == null) {
            return 0;
        }
        return currencySupply.currentSupply;
    }

    public long getReserveSupply() {
        return reserveSupply;
    }

    public long getMaxSupply() {
        return maxSupply;
    }

    public int getCreationHeight() {
        return creationHeight;
    }

    public int getIssuanceHeight() {
        return issuanceHeight;
    }

    public long getMinReservePerUnitNQT() {
        return minReservePerUnitNQT;
    }

    public int getMinDifficulty() {
        return minDifficulty;
    }

    public int getMaxDifficulty() {
        return maxDifficulty;
    }

    public byte getRuleset() {
        return ruleset;
    }

    public byte getAlgorithm() {
        return algorithm;
    }

    public byte getDecimals() {
        return decimals;
    }

    public long getCurrentReservePerUnitNQT() {
        if (!is(CurrencyType.RESERVABLE) || getSupplyData() == null) {
            return 0;
        }
        return currencySupply.currentReservePerUnitNQT;
    }

    public boolean isActive() {
        return issuanceHeight <= BlockchainImpl.getInstance().getHeight();
    }

    private CurrencySupply getSupplyData() {
        if (!is(CurrencyType.RESERVABLE) && !is(CurrencyType.MINTABLE)) {
            return null;
        }
        if (currencySupply == null) {
            currencySupply = currencySupplyTable.get(currencyDbKeyFactory.newKey(this));
            if (currencySupply == null) {
                currencySupply = new CurrencySupply(this);
            }
        }
        return currencySupply;
    }

    static void increaseReserve(LedgerEvent event, long eventId, Account account, long currencyId, long amountPerUnitNQT) {
        Currency currency = Currency.getCurrency(currencyId);
        account.addToBalanceNQT(event, eventId, -Math.multiplyExact(currency.getReserveSupply(), amountPerUnitNQT));
        CurrencySupply currencySupply = currency.getSupplyData();
        currencySupply.currentReservePerUnitNQT += amountPerUnitNQT;
        currencySupplyTable.insert(currencySupply);
        CurrencyFounder.addOrUpdateFounder(currencyId, account.getId(), amountPerUnitNQT);
    }

    static void claimReserve(LedgerEvent event, long eventId, Account account, long currencyId, long units) {
        account.addToCurrencyUnits(event, eventId, currencyId, -units);
        Currency currency = Currency.getCurrency(currencyId);
        currency.increaseSupply(- units);
        account.addToBalanceAndUnconfirmedBalanceNQT(event, eventId,
                Math.multiplyExact(units, currency.getCurrentReservePerUnitNQT()));
    }

    static void transferCurrency(LedgerEvent event, long eventId, Account senderAccount, Account recipientAccount,
                                 long currencyId, long units) {
        senderAccount.addToCurrencyUnits(event, eventId, currencyId, -units);
        recipientAccount.addToCurrencyAndUnconfirmedCurrencyUnits(event, eventId, currencyId, units);
    }

    void increaseSupply(long units) {
        getSupplyData();
        currencySupply.currentSupply += units;
        if (currencySupply.currentSupply > maxSupply || currencySupply.currentSupply < 0) {
            currencySupply.currentSupply -= units;
            throw new IllegalArgumentException("Cannot add " + units + " to current supply of " + currencySupply.currentSupply);
        }
        currencySupplyTable.insert(currencySupply);
    }

    public DbIterator<Account.AccountCurrency> getAccounts(int from, int to) {
        return Account.getCurrencyAccounts(this.currencyId, from, to);
    }

    public DbIterator<Account.AccountCurrency> getAccounts(int height, int from, int to) {
        return Account.getCurrencyAccounts(this.currencyId, height, from, to);
    }

    public DbIterator<Exchange> getExchanges(int from, int to) {
        return Exchange.getCurrencyExchanges(this.currencyId, from, to);
    }

    public DbIterator<CurrencyTransfer> getTransfers(int from, int to) {
        return CurrencyTransfer.getCurrencyTransfers(this.currencyId, from, to);
    }

    public boolean is(CurrencyType type) {
        return (this.type & type.getCode()) != 0;
    }

    public boolean canBeDeletedBy(long senderAccountId) {
        if (!is(CurrencyType.NON_SHUFFLEABLE) && Shuffling.getHoldingShufflingCount(currencyId, false) > 0) {
            return false;
        }
        if (!isActive()) {
            return senderAccountId == accountId;
        }
        if (is(CurrencyType.MINTABLE) && getCurrentSupply() < maxSupply && senderAccountId != accountId) {
            return false;
        }
        try (DbIterator<Account.AccountCurrency> accountCurrencies = Account.getCurrencyAccounts(this.currencyId, 0, -1)) {
            return ! accountCurrencies.hasNext() || accountCurrencies.next().getAccountId() == senderAccountId && ! accountCurrencies.hasNext();
        }
    }

    void delete(LedgerEvent event, long eventId, Account senderAccount) {
        if (!canBeDeletedBy(senderAccount.getId())) {
            // shouldn't happen as ownership has already been checked in validate, but as a safety check
            throw new IllegalStateException("Currency " + Long.toUnsignedString(currencyId) + " not entirely owned by " + Long.toUnsignedString(senderAccount.getId()));
        }
        listeners.notify(this, Event.BEFORE_DELETE);
        if (is(CurrencyType.RESERVABLE)) {
            if (is(CurrencyType.CLAIMABLE) && isActive()) {
                senderAccount.addToUnconfirmedCurrencyUnits(event, eventId, currencyId,
                        -senderAccount.getCurrencyUnits(currencyId));
                Currency.claimReserve(event, eventId, senderAccount, currencyId, senderAccount.getCurrencyUnits(currencyId));
            }
            if (!isActive()) {
                try (DbIterator<CurrencyFounder> founders = CurrencyFounder.getCurrencyFounders(currencyId, 0, Integer.MAX_VALUE)) {
                    for (CurrencyFounder founder : founders) {
                        Account.getAccount(founder.getAccountId())
                                .addToBalanceAndUnconfirmedBalanceNQT(event, eventId, Math.multiplyExact(reserveSupply,
                                        founder.getAmountPerUnitNQT()));
                    }
                }
            }
            CurrencyFounder.remove(currencyId);
        }
        if (is(CurrencyType.EXCHANGEABLE)) {
            List<CurrencyBuyOffer> buyOffers = new ArrayList<>();
            try (DbIterator<CurrencyBuyOffer> offers = CurrencyBuyOffer.getOffers(this, 0, -1)) {
                while (offers.hasNext()) {
                    buyOffers.add(offers.next());
                }
            }
            buyOffers.forEach((offer) -> CurrencyExchangeOffer.removeOffer(event, offer));
        }
        if (is(CurrencyType.MINTABLE)) {
            CurrencyMint.deleteCurrency(this);
        }
        senderAccount.addToUnconfirmedCurrencyUnits(event, eventId, currencyId,
                -senderAccount.getUnconfirmedCurrencyUnits(currencyId));
        senderAccount.addToCurrencyUnits(event, eventId, currencyId, -senderAccount.getCurrencyUnits(currencyId));
        currencyTable.delete(this);
    }

    private static final class CrowdFundingListener implements Listener<Block> {

        @Override
        public void notify(Block block) {
            if (block.getHeight() <= Constants.MONETARY_SYSTEM_BLOCK) {
                return;
            }
            try (DbIterator<Currency> issuedCurrencies = currencyTable.getManyBy(new DbClause.IntClause("issuance_height", block.getHeight()), 0, -1)) {
                for (Currency currency : issuedCurrencies) {
                    if (currency.getCurrentReservePerUnitNQT() < currency.getMinReservePerUnitNQT()) {
                        listeners.notify(currency, Event.BEFORE_UNDO_CROWDFUNDING);
                        undoCrowdFunding(currency);
                    } else {
                        listeners.notify(currency, Event.BEFORE_DISTRIBUTE_CROWDFUNDING);
                        distributeCurrency(currency);
                    }
                }
            }
        }

        private void undoCrowdFunding(Currency currency) {
            try (DbIterator<CurrencyFounder> founders = CurrencyFounder.getCurrencyFounders(currency.getId(), 0, Integer.MAX_VALUE)) {
                for (CurrencyFounder founder : founders) {
                    Account.getAccount(founder.getAccountId())
                            .addToBalanceAndUnconfirmedBalanceNQT(LedgerEvent.CURRENCY_UNDO_CROWDFUNDING, currency.getId(),
                                    Math.multiplyExact(currency.getReserveSupply(),
                                            founder.getAmountPerUnitNQT()));
                }
            }
            Account.getAccount(currency.getAccountId())
                    .addToCurrencyAndUnconfirmedCurrencyUnits(LedgerEvent.CURRENCY_UNDO_CROWDFUNDING, currency.getId(),
                            currency.getId(), - currency.getInitialSupply());
            currencyTable.delete(currency);
            CurrencyFounder.remove(currency.getId());
        }

        private void distributeCurrency(Currency currency) {
            long totalAmountPerUnit = 0;
            final long remainingSupply = currency.getReserveSupply() - currency.getInitialSupply();
            List<CurrencyFounder> currencyFounders = new ArrayList<>();
            try (DbIterator<CurrencyFounder> founders = CurrencyFounder.getCurrencyFounders(currency.getId(), 0, Integer.MAX_VALUE)) {
                for (CurrencyFounder founder : founders) {
                    totalAmountPerUnit += founder.getAmountPerUnitNQT();
                    currencyFounders.add(founder);
                }
            }
            CurrencySupply currencySupply = currency.getSupplyData();
            for (CurrencyFounder founder : currencyFounders) {
                long units = Math.multiplyExact(remainingSupply, founder.getAmountPerUnitNQT()) / totalAmountPerUnit;
                currencySupply.currentSupply += units;
                Account.getAccount(founder.getAccountId())
                        .addToCurrencyAndUnconfirmedCurrencyUnits(LedgerEvent.CURRENCY_DISTRIBUTION, currency.getId(),
                                currency.getId(), units);
            }
            Account issuerAccount = Account.getAccount(currency.getAccountId());
            issuerAccount.addToCurrencyAndUnconfirmedCurrencyUnits(LedgerEvent.CURRENCY_DISTRIBUTION, currency.getId(),
                    currency.getId(), currency.getReserveSupply() - currency.getCurrentSupply());
            if (!currency.is(CurrencyType.CLAIMABLE)) {
                issuerAccount.addToBalanceAndUnconfirmedBalanceNQT(LedgerEvent.CURRENCY_DISTRIBUTION, currency.getId(),
                        Math.multiplyExact(totalAmountPerUnit, currency.getReserveSupply()));
            }
            currencySupply.currentSupply = currency.getReserveSupply();
            currencySupplyTable.insert(currencySupply);
        }
    }
}
