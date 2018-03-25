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

package nxt.user;

import nxt.Account;
import nxt.Block;
import nxt.Nxt;
import nxt.Transaction;
import nxt.db.DbIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import static nxt.user.JSONResponses.LOCK_ACCOUNT;

public final class UnlockAccount extends UserServlet.UserRequestHandler {

    static final UnlockAccount instance = new UnlockAccount();

    private UnlockAccount() {}

    private static final Comparator<JSONObject> myTransactionsComparator = (o1, o2) -> {
        int t1 = ((Number)o1.get("timestamp")).intValue();
        int t2 = ((Number)o2.get("timestamp")).intValue();
        if (t1 < t2) {
            return 1;
        }
        if (t1 > t2) {
            return -1;
        }
        String id1 = (String)o1.get("id");
        String id2 = (String)o2.get("id");
        return id2.compareTo(id1);
    };

    @Override
    JSONStreamAware processRequest(HttpServletRequest req, User user) throws IOException {
        String secretPhrase = req.getParameter("secretPhrase");
        // lock all other instances of this account being unlocked
        Users.getAllUsers().forEach(u -> {
            if (secretPhrase.equals(u.getSecretPhrase())) {
                u.lockAccount();
                if (! u.isInactive()) {
                    u.enqueue(LOCK_ACCOUNT);
                }
            }
        });

        long accountId = user.unlockAccount(secretPhrase);

        JSONObject response = new JSONObject();
        response.put("response", "unlockAccount");
        response.put("account", Long.toUnsignedString(accountId));

        if (secretPhrase.length() < 30) {

            response.put("secretPhraseStrength", 1);

        } else {

            response.put("secretPhraseStrength", 5);

        }

        Account account = Account.getAccount(accountId);
        if (account == null) {

            response.put("balanceNQT", 0);

        } else {

            response.put("balanceNQT", account.getUnconfirmedBalanceNQT());

            JSONArray myTransactions = new JSONArray();
            byte[] accountPublicKey = Account.getPublicKey(accountId);
            try (DbIterator<? extends Transaction> transactions = Nxt.getTransactionProcessor().getAllUnconfirmedTransactions()) {
                while (transactions.hasNext()) {
                    Transaction transaction = transactions.next();
                    if (Arrays.equals(transaction.getSenderPublicKey(), accountPublicKey)) {

                        JSONObject myTransaction = new JSONObject();
                        myTransaction.put("index", Users.getIndex(transaction));
                        myTransaction.put("transactionTimestamp", transaction.getTimestamp());
                        myTransaction.put("deadline", transaction.getDeadline());
                        myTransaction.put("account", Long.toUnsignedString(transaction.getRecipientId()));
                        myTransaction.put("sentAmountNQT", transaction.getAmountNQT());
                        if (accountId == transaction.getRecipientId()) {
                            myTransaction.put("receivedAmountNQT", transaction.getAmountNQT());
                        }
                        myTransaction.put("feeNQT", transaction.getFeeNQT());
                        myTransaction.put("numberOfConfirmations", -1);
                        myTransaction.put("id", transaction.getStringId());

                        myTransactions.add(myTransaction);

                    } else if (accountId == transaction.getRecipientId()) {

                        JSONObject myTransaction = new JSONObject();
                        myTransaction.put("index", Users.getIndex(transaction));
                        myTransaction.put("transactionTimestamp", transaction.getTimestamp());
                        myTransaction.put("deadline", transaction.getDeadline());
                        myTransaction.put("account", Long.toUnsignedString(transaction.getSenderId()));
                        myTransaction.put("receivedAmountNQT", transaction.getAmountNQT());
                        myTransaction.put("feeNQT", transaction.getFeeNQT());
                        myTransaction.put("numberOfConfirmations", -1);
                        myTransaction.put("id", transaction.getStringId());

                        myTransactions.add(myTransaction);

                    }
                }
            }

            SortedSet<JSONObject> myTransactionsSet = new TreeSet<>(myTransactionsComparator);

            int blockchainHeight = Nxt.getBlockchain().getLastBlock().getHeight();
            try (DbIterator<? extends Block> blockIterator = Nxt.getBlockchain().getBlocks(accountId, 0)) {
                while (blockIterator.hasNext()) {
                    Block block = blockIterator.next();
                    if (block.getTotalFeeNQT() > 0) {
                        JSONObject myTransaction = new JSONObject();
                        myTransaction.put("index", "block" + Users.getIndex(block));
                        myTransaction.put("blockTimestamp", block.getTimestamp());
                        myTransaction.put("block", block.getStringId());
                        myTransaction.put("earnedAmountNQT", block.getTotalFeeNQT());
                        myTransaction.put("numberOfConfirmations", blockchainHeight - block.getHeight());
                        myTransaction.put("id", "-");
                        myTransaction.put("timestamp", block.getTimestamp());
                        myTransactionsSet.add(myTransaction);
                    }
                }
            }

            try (DbIterator<? extends Transaction> transactionIterator = Nxt.getBlockchain().getTransactions(accountId, (byte) -1, (byte) -1, 0, false)) {
                while (transactionIterator.hasNext()) {
                    Transaction transaction = transactionIterator.next();
                    if (transaction.getSenderId() == accountId) {
                        JSONObject myTransaction = new JSONObject();
                        myTransaction.put("index", Users.getIndex(transaction));
                        myTransaction.put("blockTimestamp", transaction.getBlockTimestamp());
                        myTransaction.put("transactionTimestamp", transaction.getTimestamp());
                        myTransaction.put("account", Long.toUnsignedString(transaction.getRecipientId()));
                        myTransaction.put("sentAmountNQT", transaction.getAmountNQT());
                        if (accountId == transaction.getRecipientId()) {
                            myTransaction.put("receivedAmountNQT", transaction.getAmountNQT());
                        }
                        myTransaction.put("feeNQT", transaction.getFeeNQT());
                        myTransaction.put("numberOfConfirmations", blockchainHeight - transaction.getHeight());
                        myTransaction.put("id", transaction.getStringId());
                        myTransaction.put("timestamp", transaction.getTimestamp());
                        myTransactionsSet.add(myTransaction);
                    } else if (transaction.getRecipientId() == accountId) {
                        JSONObject myTransaction = new JSONObject();
                        myTransaction.put("index", Users.getIndex(transaction));
                        myTransaction.put("blockTimestamp", transaction.getBlockTimestamp());
                        myTransaction.put("transactionTimestamp", transaction.getTimestamp());
                        myTransaction.put("account", Long.toUnsignedString(transaction.getSenderId()));
                        myTransaction.put("receivedAmountNQT", transaction.getAmountNQT());
                        myTransaction.put("feeNQT", transaction.getFeeNQT());
                        myTransaction.put("numberOfConfirmations", blockchainHeight - transaction.getHeight());
                        myTransaction.put("id", transaction.getStringId());
                        myTransaction.put("timestamp", transaction.getTimestamp());
                        myTransactionsSet.add(myTransaction);
                    }
                }
            }

            Iterator<JSONObject> iterator = myTransactionsSet.iterator();
            while (myTransactions.size() < 1000 && iterator.hasNext()) {
                myTransactions.add(iterator.next());
            }

            if (myTransactions.size() > 0) {
                JSONObject response2 = new JSONObject();
                response2.put("response", "processNewData");
                response2.put("addedMyTransactions", myTransactions);
                user.enqueue(response2);
            }
        }
        return response;
    }

    @Override
    boolean requirePost() {
        return true;
    }

}
