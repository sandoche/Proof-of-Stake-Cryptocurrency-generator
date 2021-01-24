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

import nxt.util.Filter;
import nxt.util.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionScheduler {

    private static final Map<Transaction, TransactionScheduler> transactionSchedulers = new ConcurrentHashMap<>();

    public static void schedule(Filter<Transaction> filter, Transaction transaction) {
        if (transactionSchedulers.size() >= 100) {
            throw new RuntimeException("Cannot schedule more than 100 transactions! Please restart your node if you want to clear existing scheduled transactions.");
        }
        transactionSchedulers.put(transaction, new TransactionScheduler(filter, transaction));
    }

    public static List<Transaction> getScheduledTransactions(long accountId) {
        ArrayList<Transaction> list = new ArrayList<>();
        for (Transaction transaction : transactionSchedulers.keySet()) {
            if (accountId == 0 || transaction.getSenderId() == accountId) {
                list.add(transaction);
            }
        }
        return list;
    }

    public static Transaction deleteScheduledTransaction(long transactionId) {
        Iterator<Transaction> iterator = transactionSchedulers.keySet().iterator();
        while (iterator.hasNext()) {
            Transaction transaction = iterator.next();
            if (transaction.getId() == transactionId) {
                iterator.remove();
                return transaction;
            }
        }
        return null;
    }

    static {
        TransactionProcessorImpl.getInstance().addListener(transactions -> {
            Iterator<Map.Entry<Transaction, TransactionScheduler>> iterator = transactionSchedulers.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Transaction, TransactionScheduler> entry = iterator.next();
                Transaction scheduledTransaction = entry.getKey();
                TransactionScheduler transactionScheduler = entry.getValue();
                for (Transaction transaction : transactions) {
                    if (transactionScheduler.processEvent(transaction)) {
                        iterator.remove();
                        Logger.logInfoMessage("Removed " + scheduledTransaction.getStringId() + " from transaction scheduler");
                        break;
                    }
                }
            }
        }, TransactionProcessor.Event.ADDED_UNCONFIRMED_TRANSACTIONS);
    }

    private final Transaction transaction;
    private final Filter<Transaction> filter;

    private TransactionScheduler(Filter<Transaction> filter, Transaction transaction) {
        this.transaction = transaction;
        this.filter = filter;
    }

    private boolean processEvent(Transaction unconfirmedTransaction) {
        if (transaction.getExpiration() < Nxt.getEpochTime()) {
            Logger.logInfoMessage("Expired transaction in transaction scheduler " + transaction.getSenderId());
            return true;
        }
        if (!filter.ok(unconfirmedTransaction)) {
            return false;
        }
        try {
            TransactionProcessorImpl.getInstance().broadcast(transaction);
            return true;
        } catch (NxtException.ValidationException e) {
            Logger.logInfoMessage("Failed to broadcast: " + e.getMessage());
            return true;
        }
    }

}
