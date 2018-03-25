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

import nxt.db.DbIterator;
import nxt.util.Convert;
import nxt.util.Logger;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DebugTrace {

    static final String QUOTE = Nxt.getStringProperty("nxt.debugTraceQuote", "\"");
    static final String SEPARATOR = Nxt.getStringProperty("nxt.debugTraceSeparator", "\t");
    static final boolean LOG_UNCONFIRMED = Nxt.getBooleanProperty("nxt.debugLogUnconfirmed");

    static void init() {
        List<String> accountIdStrings = Nxt.getStringListProperty("nxt.debugTraceAccounts");
        String logName = Nxt.getStringProperty("nxt.debugTraceLog");
        if (accountIdStrings.isEmpty() || logName == null) {
            return;
        }
        Set<Long> accountIds = new HashSet<>();
        for (String accountId : accountIdStrings) {
            if ("*".equals(accountId)) {
                accountIds.clear();
                break;
            }
            accountIds.add(Convert.parseAccountId(accountId));
        }
        final DebugTrace debugTrace = addDebugTrace(accountIds, logName);
        Nxt.getBlockchainProcessor().addListener(block -> debugTrace.resetLog(), BlockchainProcessor.Event.RESCAN_BEGIN);
        Logger.logDebugMessage("Debug tracing of " + (accountIdStrings.contains("*") ? "ALL"
                : String.valueOf(accountIds.size())) + " accounts enabled");
    }

    public static DebugTrace addDebugTrace(Set<Long> accountIds, String logName) {
        final DebugTrace debugTrace = new DebugTrace(accountIds, logName);
        Trade.addListener(debugTrace::trace, Trade.Event.TRADE);
        Exchange.addListener(debugTrace::trace, Exchange.Event.EXCHANGE);
        Currency.addListener(debugTrace::crowdfunding, Currency.Event.BEFORE_DISTRIBUTE_CROWDFUNDING);
        Currency.addListener(debugTrace::undoCrowdfunding, Currency.Event.BEFORE_UNDO_CROWDFUNDING);
        Currency.addListener(debugTrace::delete, Currency.Event.BEFORE_DELETE);
        CurrencyMint.addListener(debugTrace::currencyMint, CurrencyMint.Event.CURRENCY_MINT);
        Account.addListener(account -> debugTrace.trace(account, false), Account.Event.BALANCE);
        if (LOG_UNCONFIRMED) {
            Account.addListener(account -> debugTrace.trace(account, true), Account.Event.UNCONFIRMED_BALANCE);
        }
        Account.addAssetListener(accountAsset -> debugTrace.trace(accountAsset, false), Account.Event.ASSET_BALANCE);
        if (LOG_UNCONFIRMED) {
            Account.addAssetListener(accountAsset -> debugTrace.trace(accountAsset, true), Account.Event.UNCONFIRMED_ASSET_BALANCE);
        }
        Account.addCurrencyListener(accountCurrency -> debugTrace.trace(accountCurrency, false), Account.Event.CURRENCY_BALANCE);
        if (LOG_UNCONFIRMED) {
            Account.addCurrencyListener(accountCurrency -> debugTrace.trace(accountCurrency, true), Account.Event.UNCONFIRMED_CURRENCY_BALANCE);
        }
        Account.addLeaseListener(accountLease -> debugTrace.trace(accountLease, true), Account.Event.LEASE_STARTED);
        Account.addLeaseListener(accountLease -> debugTrace.trace(accountLease, false), Account.Event.LEASE_ENDED);
        Nxt.getBlockchainProcessor().addListener(debugTrace::traceBeforeAccept, BlockchainProcessor.Event.BEFORE_BLOCK_ACCEPT);
        Nxt.getBlockchainProcessor().addListener(debugTrace::trace, BlockchainProcessor.Event.BEFORE_BLOCK_APPLY);
        Nxt.getTransactionProcessor().addListener(transactions -> debugTrace.traceRelease(transactions.get(0)), TransactionProcessor.Event.RELEASE_PHASED_TRANSACTION);
        Shuffling.addListener(debugTrace::traceShufflingDistribute, Shuffling.Event.SHUFFLING_DONE);
        Shuffling.addListener(debugTrace::traceShufflingCancel, Shuffling.Event.SHUFFLING_CANCELLED);
        return debugTrace;
    }

    //NOTE: first and last columns should not have a blank entry in any row, otherwise VerifyTrace fails to parse the line
    private static final String[] columns = {"height", "event", "account", "asset", "currency", "balance", "unconfirmed balance",
            "asset balance", "unconfirmed asset balance", "currency balance", "unconfirmed currency balance",
            "transaction amount", "transaction fee", "generation fee", "effective balance", "dividend",
            "order", "order price", "order quantity", "order cost",
            "offer", "buy rate", "sell rate", "buy units", "sell units", "buy cost", "sell cost",
            "trade price", "trade quantity", "trade cost",
            "exchange rate", "exchange quantity", "exchange cost", "currency cost",
            "crowdfunding", "claim", "mint",
            "asset quantity", "currency units", "transaction", "lessee", "lessor guaranteed balance",
            "purchase", "purchase price", "purchase quantity", "purchase cost", "discount", "refund",
            "shuffling",
            "sender", "recipient", "block", "timestamp"};

    private static final Map<String,String> headers = new HashMap<>();
    static {
        for (String entry : columns) {
            headers.put(entry, entry);
        }
    }

    private final Set<Long> accountIds;
    private final String logName;
    private PrintWriter log;

    private DebugTrace(Set<Long> accountIds, String logName) {
        this.accountIds = accountIds;
        this.logName = logName;
        resetLog();
    }

    void resetLog() {
        if (log != null) {
            log.close();
        }
        try {
            log = new PrintWriter((new BufferedWriter(new OutputStreamWriter(new FileOutputStream(logName)))), true);
        } catch (IOException e) {
            Logger.logDebugMessage("Debug tracing to " + logName + " not possible", e);
            throw new RuntimeException(e);
        }
        this.log(headers);
    }

    private boolean include(long accountId) {
        return accountId != 0 && (accountIds.isEmpty() || accountIds.contains(accountId));
    }

    // Note: Trade events occur before the change in account balances
    private void trace(Trade trade) {
        long askAccountId = Order.Ask.getAskOrder(trade.getAskOrderId()).getAccountId();
        long bidAccountId = Order.Bid.getBidOrder(trade.getBidOrderId()).getAccountId();
        if (include(askAccountId)) {
            log(getValues(askAccountId, trade, true));
        }
        if (include(bidAccountId)) {
            log(getValues(bidAccountId, trade, false));
        }
    }

    private void trace(Exchange exchange) {
        long sellerAccountId = exchange.getSellerId();
        long buyerAccountId = exchange.getBuyerId();
        if (include(sellerAccountId)) {
            log(getValues(sellerAccountId, exchange, true));
        }
        if (include(buyerAccountId)) {
            log(getValues(buyerAccountId, exchange, false));
        }
    }

    private void trace(Account account, boolean unconfirmed) {
        if (include(account.getId())) {
            log(getValues(account.getId(), unconfirmed));
        }
    }

    private void trace(Account.AccountAsset accountAsset, boolean unconfirmed) {
        if (! include(accountAsset.getAccountId())) {
            return;
        }
        log(getValues(accountAsset.getAccountId(), accountAsset, unconfirmed));
    }

    private void trace(Account.AccountCurrency accountCurrency, boolean unconfirmed) {
        if (! include(accountCurrency.getAccountId())) {
            return;
        }
        log(getValues(accountCurrency.getAccountId(), accountCurrency, unconfirmed));
    }

    private void trace(Account.AccountLease accountLease, boolean start) {
        if (! include(accountLease.getCurrentLesseeId()) && ! include(accountLease.getLessorId())) {
            return;
        }
        log(getValues(accountLease.getLessorId(), accountLease, start));
    }

    private void traceBeforeAccept(Block block) {
        long generatorId = block.getGeneratorId();
        if (include(generatorId)) {
            log(getValues(generatorId, block));
        }
        for (long accountId : accountIds) {
            Account account = Account.getAccount(accountId);
            if (account != null) {
                try (DbIterator<Account> lessors = account.getLessors()) {
                    while (lessors.hasNext()) {
                        log(lessorGuaranteedBalance(lessors.next(), accountId));
                    }
                }
            }
        }
    }

    private void trace(Block block) {
        for (Transaction transaction : block.getTransactions()) {
            long senderId = transaction.getSenderId();
            if (((TransactionImpl)transaction).attachmentIsPhased()) {
                if (include(senderId)) {
                    log(getValues(senderId, transaction, false, true, false));
                }
                continue;
            }
            if (include(senderId)) {
                log(getValues(senderId, transaction, false, true, true));
                log(getValues(senderId, transaction, transaction.getAttachment(), false));
            }
            long recipientId = transaction.getRecipientId();
            if (transaction.getAmountNQT() > 0 && recipientId == 0) {
                recipientId = Genesis.CREATOR_ID;
            }
            if (include(recipientId)) {
                log(getValues(recipientId, transaction, true, true, true));
                log(getValues(recipientId, transaction, transaction.getAttachment(), true));
            }
        }
    }

    private void traceRelease(Transaction transaction) {
        long senderId = transaction.getSenderId();
        if (include(senderId)) {
            log(getValues(senderId, transaction, false, false, true));
            log(getValues(senderId, transaction, transaction.getAttachment(), false));
        }
        long recipientId = transaction.getRecipientId();
        if (include(recipientId)) {
            log(getValues(recipientId, transaction, true, false, true));
            log(getValues(recipientId, transaction, transaction.getAttachment(), true));
        }
    }

    private void traceShufflingDistribute(Shuffling shuffling) {
        ShufflingParticipant.getParticipants(shuffling.getId()).forEach(shufflingParticipant -> {
            if (include(shufflingParticipant.getAccountId())) {
                log(getValues(shufflingParticipant.getAccountId(), shuffling, false));
            }
        });
        for (byte[] recipientPublicKey : shuffling.getRecipientPublicKeys()) {
            long recipientId = Account.getId(recipientPublicKey);
            if (include(recipientId)) {
                log(getValues(recipientId, shuffling, true));
            }
        }
    }

    private void traceShufflingCancel(Shuffling shuffling) {
        long blamedAccountId = shuffling.getAssigneeAccountId();
        if (blamedAccountId != 0 && include(blamedAccountId)) {
            Map<String,String> map = getValues(blamedAccountId, false);
            map.put("transaction fee", String.valueOf(-Constants.SHUFFLING_DEPOSIT_NQT));
            map.put("event", "shuffling blame");
            log(map);
            long fee = Constants.SHUFFLING_DEPOSIT_NQT / 4;
            int height = Nxt.getBlockchain().getHeight();
            for (int i = 0; i < 3; i++) {
                long generatorId = BlockDb.findBlockAtHeight(height - i - 1).getGeneratorId();
                if (include(generatorId)) {
                    Map<String, String> generatorMap = getValues(generatorId, false);
                    generatorMap.put("generation fee", String.valueOf(fee));
                    generatorMap.put("event", "shuffling blame");
                    log(generatorMap);
                }
            }
            fee = Constants.SHUFFLING_DEPOSIT_NQT - 3 * fee;
            long generatorId = Nxt.getBlockchain().getLastBlock().getGeneratorId();
            if (include(generatorId)) {
                Map<String,String> generatorMap = getValues(generatorId, false);
                generatorMap.put("generation fee", String.valueOf(fee));
                generatorMap.put("event", "shuffling blame");
                log(generatorMap);
            }
        }
    }

    private Map<String,String> lessorGuaranteedBalance(Account account, long lesseeId) {
        Map<String,String> map = new HashMap<>();
        map.put("account", Long.toUnsignedString(account.getId()));
        map.put("lessor guaranteed balance", String.valueOf(account.getGuaranteedBalanceNQT()));
        map.put("lessee", Long.toUnsignedString(lesseeId));
        map.put("timestamp", String.valueOf(Nxt.getBlockchain().getLastBlock().getTimestamp()));
        map.put("height", String.valueOf(Nxt.getBlockchain().getHeight()));
        map.put("event", "lessor guaranteed balance");
        return map;
    }

    private void crowdfunding(Currency currency) {
        long totalAmountPerUnit = 0;
        long foundersTotal = 0;
        final long remainingSupply = currency.getReserveSupply() - currency.getInitialSupply();
        List<CurrencyFounder> currencyFounders = new ArrayList<>();
        try (DbIterator<CurrencyFounder> founders = CurrencyFounder.getCurrencyFounders(currency.getId(), 0, Integer.MAX_VALUE)) {
            for (CurrencyFounder founder : founders) {
                totalAmountPerUnit += founder.getAmountPerUnitNQT();
                currencyFounders.add(founder);
            }
        }
        for (CurrencyFounder founder : currencyFounders) {
            long units = Math.multiplyExact(remainingSupply, founder.getAmountPerUnitNQT()) / totalAmountPerUnit;
            Map<String,String> founderMap = getValues(founder.getAccountId(), false);
            founderMap.put("currency", Long.toUnsignedString(currency.getId()));
            founderMap.put("currency units", String.valueOf(units));
            founderMap.put("event", "distribution");
            log(founderMap);
            foundersTotal += units;
        }
        Map<String,String> map = getValues(currency.getAccountId(), false);
        map.put("currency", Long.toUnsignedString(currency.getId()));
        map.put("crowdfunding", String.valueOf(currency.getReserveSupply()));
        map.put("currency units", String.valueOf(remainingSupply - foundersTotal));
        if (!currency.is(CurrencyType.CLAIMABLE)) {
            map.put("currency cost", String.valueOf(Math.multiplyExact(currency.getReserveSupply(), currency.getCurrentReservePerUnitNQT())));
        }
        map.put("event", "crowdfunding");
        log(map);
    }

    private void undoCrowdfunding(Currency currency) {
        try (DbIterator<CurrencyFounder> founders = CurrencyFounder.getCurrencyFounders(currency.getId(), 0, Integer.MAX_VALUE)) {
            for (CurrencyFounder founder : founders) {
                Map<String,String> founderMap = getValues(founder.getAccountId(), false);
                founderMap.put("currency", Long.toUnsignedString(currency.getId()));
                founderMap.put("currency cost", String.valueOf(Math.multiplyExact(currency.getReserveSupply(), founder.getAmountPerUnitNQT())));
                founderMap.put("event", "undo distribution");
                log(founderMap);
            }
        }
        Map<String,String> map = getValues(currency.getAccountId(), false);
        map.put("currency", Long.toUnsignedString(currency.getId()));
        map.put("currency units", String.valueOf(-currency.getInitialSupply()));
        map.put("event", "undo crowdfunding");
        log(map);
    }

    private void delete(Currency currency) {
        long accountId;
        long units;
        if (!currency.isActive()) {
            accountId = currency.getAccountId();
            units = currency.getCurrentSupply();
        } else {
            try (DbIterator<Account.AccountCurrency> accountCurrencies = Account.getCurrencyAccounts(currency.getId(), 0, -1)) {
                if (accountCurrencies.hasNext()) {
                    Account.AccountCurrency accountCurrency = accountCurrencies.next();
                    accountId = accountCurrency.getAccountId();
                    units = accountCurrency.getUnits();
                } else {
                    return;
                }
            }
        }
        Map<String,String> map = getValues(accountId, false);
        map.put("currency", Long.toUnsignedString(currency.getId()));
        if (currency.is(CurrencyType.RESERVABLE)) {
            if (currency.is(CurrencyType.CLAIMABLE) && currency.isActive()) {
                map.put("currency cost", String.valueOf(Math.multiplyExact(units, currency.getCurrentReservePerUnitNQT())));
            }
            if (!currency.isActive()) {
                try (DbIterator<CurrencyFounder> founders = CurrencyFounder.getCurrencyFounders(currency.getId(), 0, Integer.MAX_VALUE)) {
                    for (CurrencyFounder founder : founders) {
                        Map<String,String> founderMap = getValues(founder.getAccountId(), false);
                        founderMap.put("currency", Long.toUnsignedString(currency.getId()));
                        founderMap.put("currency cost", String.valueOf(Math.multiplyExact(currency.getReserveSupply(), founder.getAmountPerUnitNQT())));
                        founderMap.put("event", "undo distribution");
                        log(founderMap);
                    }
                }
            }
        }
        map.put("currency units", String.valueOf(-units));
        map.put("event", "currency delete");
        log(map);
    }

    private void currencyMint(CurrencyMint.Mint mint) {
        if (!include(mint.accountId)) {
            return;
        }
        Map<String, String> map = getValues(mint.accountId, false);
        map.put("currency", Long.toUnsignedString(mint.currencyId));
        map.put("currency units", String.valueOf(mint.units));
        map.put("event", "currency mint");
        log(map);
    }

    private Map<String,String> getValues(long accountId, boolean unconfirmed) {
        Map<String,String> map = new HashMap<>();
        map.put("account", Long.toUnsignedString(accountId));
        Account account = Account.getAccount(accountId);
        map.put("balance", String.valueOf(account != null ? account.getBalanceNQT() : 0));
        map.put("unconfirmed balance", String.valueOf(account != null ? account.getUnconfirmedBalanceNQT() : 0));
        map.put("timestamp", String.valueOf(Nxt.getBlockchain().getLastBlock().getTimestamp()));
        map.put("height", String.valueOf(Nxt.getBlockchain().getHeight()));
        map.put("event", unconfirmed ? "unconfirmed balance" : "balance");
        return map;
    }

    private Map<String,String> getValues(long accountId, Trade trade, boolean isAsk) {
        Map<String,String> map = getValues(accountId, false);
        map.put("asset", Long.toUnsignedString(trade.getAssetId()));
        map.put("trade quantity", String.valueOf(isAsk ? - trade.getQuantityQNT() : trade.getQuantityQNT()));
        map.put("trade price", String.valueOf(trade.getPriceNQT()));
        long tradeCost = Math.multiplyExact(trade.getQuantityQNT(), trade.getPriceNQT());
        map.put("trade cost", String.valueOf((isAsk ? tradeCost : - tradeCost)));
        map.put("event", "trade");
        return map;
    }

    private Map<String,String> getValues(long accountId, Exchange exchange, boolean isSell) {
        Map<String,String> map = getValues(accountId, false);
        map.put("currency", Long.toUnsignedString(exchange.getCurrencyId()));
        map.put("exchange quantity", String.valueOf(isSell ? -exchange.getUnits() : exchange.getUnits()));
        map.put("exchange rate", String.valueOf(exchange.getRate()));
        long exchangeCost = Math.multiplyExact(exchange.getUnits(), exchange.getRate());
        map.put("exchange cost", String.valueOf((isSell ? exchangeCost : - exchangeCost)));
        map.put("event", "exchange");
        return map;
    }

    private Map<String,String> getValues(long accountId, Shuffling shuffling, boolean isRecipient) {
        Map<String,String> map = getValues(accountId, false);
        map.put("shuffling", Long.toUnsignedString(shuffling.getId()));
        String amount = String.valueOf(isRecipient ? shuffling.getAmount() : -shuffling.getAmount());
        String deposit = String.valueOf(isRecipient ? Constants.SHUFFLING_DEPOSIT_NQT : -Constants.SHUFFLING_DEPOSIT_NQT);
        switch (shuffling.getHoldingType()) {
            case NXT:
                map.put("transaction amount", amount);
                break;
            case ASSET:
                map.put("asset quantity", amount);
                map.put("asset", Long.toUnsignedString(shuffling.getHoldingId()));
                map.put("transaction amount", deposit);
                break;
            case CURRENCY:
                map.put("currency units", amount);
                map.put("currency", Long.toUnsignedString(shuffling.getHoldingId()));
                map.put("transaction amount", deposit);
                break;
            default:
                throw new RuntimeException("Unsupported holding type " + shuffling.getHoldingType());
        }
        map.put("event", "shuffling distribute");
        return map;
    }

    private Map<String,String> getValues(long accountId, Transaction transaction, boolean isRecipient, boolean logFee, boolean logAmount) {
        long amount = transaction.getAmountNQT();
        long fee = transaction.getFeeNQT();
        if (isRecipient) {
            fee = 0; // fee doesn't affect recipient account
        } else {
            // for sender the amounts are subtracted
            amount = - amount;
            fee = - fee;
        }
        if (fee == 0 && amount == 0) {
            return Collections.emptyMap();
        }
        Map<String,String> map = getValues(accountId, false);
        if (logAmount) {
            map.put("transaction amount", String.valueOf(amount));
        }
        if (logFee) {
            map.put("transaction fee", String.valueOf(fee));
        }
        map.put("transaction", transaction.getStringId());
        if (isRecipient) {
            map.put("sender", Long.toUnsignedString(transaction.getSenderId()));
        } else {
            map.put("recipient", Long.toUnsignedString(transaction.getRecipientId()));
        }
        map.put("event", "transaction");
        return map;
    }

    private Map<String,String> getValues(long accountId, Block block) {
        long fee = block.getTotalFeeNQT();
        if (fee == 0) {
            return Collections.emptyMap();
        }
        long totalBackFees = 0;
        if (block.getHeight() > Constants.SHUFFLING_BLOCK) {
            long[] backFees = new long[3];
            for (Transaction transaction : block.getTransactions()) {
                long[] fees = ((TransactionImpl)transaction).getBackFees();
                for (int i = 0; i < fees.length; i++) {
                    backFees[i] += fees[i];
                }
            }
            for (int i = 0; i < backFees.length; i++) {
                if (backFees[i] == 0) {
                    break;
                }
                totalBackFees += backFees[i];
                long previousGeneratorId = BlockDb.findBlockAtHeight(block.getHeight() - i - 1).getGeneratorId();
                if (include(previousGeneratorId)) {
                    Map<String,String> map = getValues(previousGeneratorId, false);
                    map.put("effective balance", String.valueOf(Account.getAccount(previousGeneratorId).getEffectiveBalanceNXT()));
                    map.put("generation fee", String.valueOf(backFees[i]));
                    map.put("block", block.getStringId());
                    map.put("event", "block");
                    map.put("timestamp", String.valueOf(block.getTimestamp()));
                    map.put("height", String.valueOf(block.getHeight()));
                    log(map);
                }
            }
        }
        Map<String,String> map = getValues(accountId, false);
        map.put("effective balance", String.valueOf(Account.getAccount(accountId).getEffectiveBalanceNXT()));
        map.put("generation fee", String.valueOf(fee - totalBackFees));
        map.put("block", block.getStringId());
        map.put("event", "block");
        map.put("timestamp", String.valueOf(block.getTimestamp()));
        map.put("height", String.valueOf(block.getHeight()));
        return map;
    }

    private Map<String,String> getValues(long accountId, Account.AccountAsset accountAsset, boolean unconfirmed) {
        Map<String,String> map = new HashMap<>();
        map.put("account", Long.toUnsignedString(accountId));
        map.put("asset", Long.toUnsignedString(accountAsset.getAssetId()));
        if (unconfirmed) {
            map.put("unconfirmed asset balance", String.valueOf(accountAsset.getUnconfirmedQuantityQNT()));
        } else {
            map.put("asset balance", String.valueOf(accountAsset.getQuantityQNT()));
        }
        map.put("timestamp", String.valueOf(Nxt.getBlockchain().getLastBlock().getTimestamp()));
        map.put("height", String.valueOf(Nxt.getBlockchain().getHeight()));
        map.put("event", "asset balance");
        return map;
    }

    private Map<String,String> getValues(long accountId, Account.AccountCurrency accountCurrency, boolean unconfirmed) {
        Map<String,String> map = new HashMap<>();
        map.put("account", Long.toUnsignedString(accountId));
        map.put("currency", Long.toUnsignedString(accountCurrency.getCurrencyId()));
        if (unconfirmed) {
            map.put("unconfirmed currency balance", String.valueOf(accountCurrency.getUnconfirmedUnits()));
        } else {
            map.put("currency balance", String.valueOf(accountCurrency.getUnits()));
        }
        map.put("timestamp", String.valueOf(Nxt.getBlockchain().getLastBlock().getTimestamp()));
        map.put("height", String.valueOf(Nxt.getBlockchain().getHeight()));
        map.put("event", "currency balance");
        return map;
    }

    private Map<String,String> getValues(long accountId, Account.AccountLease accountLease, boolean start) {
        Map<String,String> map = new HashMap<>();
        map.put("account", Long.toUnsignedString(accountId));
        map.put("event", start ? "lease begin" : "lease end");
        map.put("timestamp", String.valueOf(Nxt.getBlockchain().getLastBlock().getTimestamp()));
        map.put("height", String.valueOf(Nxt.getBlockchain().getHeight()));
        map.put("lessee", Long.toUnsignedString(accountLease.getCurrentLesseeId()));
        return map;
    }

    private Map<String,String> getValues(long accountId, Transaction transaction, Attachment attachment, boolean isRecipient) {
        Map<String,String> map = getValues(accountId, false);
        if (attachment instanceof Attachment.ColoredCoinsOrderPlacement) {
            if (isRecipient) {
                return Collections.emptyMap();
            }
            Attachment.ColoredCoinsOrderPlacement orderPlacement = (Attachment.ColoredCoinsOrderPlacement)attachment;
            boolean isAsk = orderPlacement instanceof Attachment.ColoredCoinsAskOrderPlacement;
            map.put("asset", Long.toUnsignedString(orderPlacement.getAssetId()));
            map.put("order", transaction.getStringId());
            map.put("order price", String.valueOf(orderPlacement.getPriceNQT()));
            long quantity = orderPlacement.getQuantityQNT();
            if (isAsk) {
                quantity = - quantity;
            }
            map.put("order quantity", String.valueOf(quantity));
            BigInteger orderCost = BigInteger.valueOf(orderPlacement.getPriceNQT()).multiply(BigInteger.valueOf(orderPlacement.getQuantityQNT()));
            if (! isAsk) {
                orderCost = orderCost.negate();
            }
            map.put("order cost", orderCost.toString());
            String event = (isAsk ? "ask" : "bid") + " order";
            map.put("event", event);
        } else if (attachment instanceof Attachment.ColoredCoinsAssetIssuance) {
            if (isRecipient) {
                return Collections.emptyMap();
            }
            Attachment.ColoredCoinsAssetIssuance assetIssuance = (Attachment.ColoredCoinsAssetIssuance)attachment;
            map.put("asset", transaction.getStringId());
            map.put("asset quantity", String.valueOf(assetIssuance.getQuantityQNT()));
            map.put("event", "asset issuance");
        } else if (attachment instanceof Attachment.ColoredCoinsAssetTransfer) {
            Attachment.ColoredCoinsAssetTransfer assetTransfer = (Attachment.ColoredCoinsAssetTransfer)attachment;
            map.put("asset", Long.toUnsignedString(assetTransfer.getAssetId()));
            long quantity = assetTransfer.getQuantityQNT();
            if (! isRecipient) {
                quantity = - quantity;
            }
            map.put("asset quantity", String.valueOf(quantity));
            map.put("event", "asset transfer");
        } else if (attachment instanceof Attachment.ColoredCoinsAssetDelete) {
            if (isRecipient) {
                return Collections.emptyMap();
            }
            Attachment.ColoredCoinsAssetDelete assetDelete = (Attachment.ColoredCoinsAssetDelete)attachment;
            map.put("asset", Long.toUnsignedString(assetDelete.getAssetId()));
            long quantity = assetDelete.getQuantityQNT();
            map.put("asset quantity", String.valueOf(-quantity));
            map.put("event", "asset delete");
        } else if (attachment instanceof Attachment.ColoredCoinsOrderCancellation) {
            Attachment.ColoredCoinsOrderCancellation orderCancellation = (Attachment.ColoredCoinsOrderCancellation)attachment;
            map.put("order", Long.toUnsignedString(orderCancellation.getOrderId()));
            map.put("event", "order cancel");
        } else if (attachment instanceof Attachment.DigitalGoodsPurchase) {
            Attachment.DigitalGoodsPurchase purchase = (Attachment.DigitalGoodsPurchase)transaction.getAttachment();
            if (isRecipient) {
                map = getValues(DigitalGoodsStore.Goods.getGoods(purchase.getGoodsId()).getSellerId(), false);
            }
            map.put("event", "purchase");
            map.put("purchase", transaction.getStringId());
        } else if (attachment instanceof Attachment.DigitalGoodsDelivery) {
            Attachment.DigitalGoodsDelivery delivery = (Attachment.DigitalGoodsDelivery)transaction.getAttachment();
            DigitalGoodsStore.Purchase purchase = DigitalGoodsStore.Purchase.getPurchase(delivery.getPurchaseId());
            if (isRecipient) {
                map = getValues(purchase.getBuyerId(), false);
            }
            map.put("event", "delivery");
            map.put("purchase", Long.toUnsignedString(delivery.getPurchaseId()));
            long discount = delivery.getDiscountNQT();
            map.put("purchase price", String.valueOf(purchase.getPriceNQT()));
            map.put("purchase quantity", String.valueOf(purchase.getQuantity()));
            long cost = Math.multiplyExact(purchase.getPriceNQT(), (long) purchase.getQuantity());
            if (isRecipient) {
                cost = - cost;
            }
            map.put("purchase cost", String.valueOf(cost));
            if (! isRecipient) {
                discount = - discount;
            }
            map.put("discount", String.valueOf(discount));
        } else if (attachment instanceof Attachment.DigitalGoodsRefund) {
            Attachment.DigitalGoodsRefund refund = (Attachment.DigitalGoodsRefund)transaction.getAttachment();
            if (isRecipient) {
                map = getValues(DigitalGoodsStore.Purchase.getPurchase(refund.getPurchaseId()).getBuyerId(), false);
            }
            map.put("event", "refund");
            map.put("purchase", Long.toUnsignedString(refund.getPurchaseId()));
            long refundNQT = refund.getRefundNQT();
            if (! isRecipient) {
                refundNQT = - refundNQT;
            }
            map.put("refund", String.valueOf(refundNQT));
        } else if (attachment == Attachment.ARBITRARY_MESSAGE) {
            map = new HashMap<>();
            map.put("account", Long.toUnsignedString(accountId));
            map.put("timestamp", String.valueOf(Nxt.getBlockchain().getLastBlock().getTimestamp()));
            map.put("height", String.valueOf(Nxt.getBlockchain().getHeight()));
            map.put("event", attachment == Attachment.ARBITRARY_MESSAGE ? "message" : "encrypted message");
            if (isRecipient) {
                map.put("sender", Long.toUnsignedString(transaction.getSenderId()));
            } else {
                map.put("recipient", Long.toUnsignedString(transaction.getRecipientId()));
            }
        } else if (attachment instanceof Attachment.MonetarySystemPublishExchangeOffer) {
            Attachment.MonetarySystemPublishExchangeOffer publishOffer = (Attachment.MonetarySystemPublishExchangeOffer)attachment;
            map.put("currency", Long.toUnsignedString(publishOffer.getCurrencyId()));
            map.put("offer", transaction.getStringId());
            map.put("buy rate", String.valueOf(publishOffer.getBuyRateNQT()));
            map.put("sell rate", String.valueOf(publishOffer.getSellRateNQT()));
            long buyUnits = publishOffer.getInitialBuySupply();
            map.put("buy units", String.valueOf(buyUnits));
            long sellUnits = publishOffer.getInitialSellSupply();
            map.put("sell units", String.valueOf(sellUnits));
            BigInteger buyCost = BigInteger.valueOf(publishOffer.getBuyRateNQT()).multiply(BigInteger.valueOf(buyUnits));
            map.put("buy cost", buyCost.toString());
            BigInteger sellCost = BigInteger.valueOf(publishOffer.getSellRateNQT()).multiply(BigInteger.valueOf(sellUnits));
            map.put("sell cost", sellCost.toString());
            map.put("event", "offer");
        } else if (attachment instanceof Attachment.MonetarySystemCurrencyIssuance) {
            Attachment.MonetarySystemCurrencyIssuance currencyIssuance = (Attachment.MonetarySystemCurrencyIssuance) attachment;
            map.put("currency", transaction.getStringId());
            map.put("currency units", String.valueOf(currencyIssuance.getInitialSupply()));
            map.put("event", "currency issuance");
        } else if (attachment instanceof Attachment.MonetarySystemCurrencyTransfer) {
            Attachment.MonetarySystemCurrencyTransfer currencyTransfer = (Attachment.MonetarySystemCurrencyTransfer) attachment;
            map.put("currency", Long.toUnsignedString(currencyTransfer.getCurrencyId()));
            long units = currencyTransfer.getUnits();
            if (!isRecipient) {
                units = -units;
            }
            map.put("currency units", String.valueOf(units));
            map.put("event", "currency transfer");
        } else if (attachment instanceof Attachment.MonetarySystemReserveClaim) {
            Attachment.MonetarySystemReserveClaim claim = (Attachment.MonetarySystemReserveClaim) attachment;
            map.put("currency", Long.toUnsignedString(claim.getCurrencyId()));
            Currency currency = Currency.getCurrency(claim.getCurrencyId());
            map.put("currency units", String.valueOf(-claim.getUnits()));
            map.put("currency cost", String.valueOf(Math.multiplyExact(claim.getUnits(), currency.getCurrentReservePerUnitNQT())));
            map.put("event", "currency claim");
        } else if (attachment instanceof Attachment.MonetarySystemReserveIncrease) {
            Attachment.MonetarySystemReserveIncrease reserveIncrease = (Attachment.MonetarySystemReserveIncrease) attachment;
            map.put("currency", Long.toUnsignedString(reserveIncrease.getCurrencyId()));
            Currency currency = Currency.getCurrency(reserveIncrease.getCurrencyId());
            map.put("currency cost", String.valueOf(-Math.multiplyExact(reserveIncrease.getAmountPerUnitNQT(), currency.getReserveSupply())));
            map.put("event", "currency reserve");
        } else if (attachment instanceof Attachment.ColoredCoinsDividendPayment) {
            Attachment.ColoredCoinsDividendPayment dividendPayment = (Attachment.ColoredCoinsDividendPayment)attachment;
            long totalDividend = 0;
            String assetId = Long.toUnsignedString(dividendPayment.getAssetId());
            try (DbIterator<Account.AccountAsset> iterator = Account.getAssetAccounts(dividendPayment.getAssetId(), dividendPayment.getHeight(), 0, -1)) {
                while (iterator.hasNext()) {
                    Account.AccountAsset accountAsset = iterator.next();
                    if (accountAsset.getAccountId() != accountId && accountAsset.getQuantityQNT() != 0) {
                        long dividend = Math.multiplyExact(accountAsset.getQuantityQNT(), dividendPayment.getAmountNQTPerQNT());
                        Map recipient = getValues(accountAsset.getAccountId(), false);
                        recipient.put("dividend", String.valueOf(dividend));
                        recipient.put("asset", assetId);
                        recipient.put("event", "dividend");
                        totalDividend += dividend;
                        log(recipient);
                    }
                }
            }
            map.put("dividend", String.valueOf(-totalDividend));
            map.put("asset", assetId);
            map.put("event", "dividend");
        } else {
            return Collections.emptyMap();
        }
        return map;
    }

    private void log(Map<String,String> map) {
        if (map.isEmpty()) {
            return;
        }
        StringBuilder buf = new StringBuilder();
        for (String column : columns) {
            if (!LOG_UNCONFIRMED && column.startsWith("unconfirmed")) {
                continue;
            }
            String value = map.get(column);
            if (value != null) {
                buf.append(QUOTE).append(value).append(QUOTE);
            }
            buf.append(SEPARATOR);
        }
        log.println(buf.toString());
    }

}
