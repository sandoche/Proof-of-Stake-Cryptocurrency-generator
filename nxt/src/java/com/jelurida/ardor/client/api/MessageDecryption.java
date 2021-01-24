/*
 * Copyright © 2016-2020 Jelurida IP B.V.
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

package com.jelurida.ardor.client.api;

import nxt.addons.JO;
import nxt.http.callers.DecryptFromCall;
import nxt.http.callers.GetBlockCall;
import nxt.http.callers.GetBlockchainTransactionsCall;
import nxt.http.responses.BlockResponse;
import nxt.http.responses.TransactionResponse;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sample Java program which iterates through blockchain transactions and decrypts their attached message
 */
public class MessageDecryption {

    private static final String RECIPIENT_SECRET_PHRASE = "no30FiiC95auuD0tbA1QJuhACtPdT6llpYInYREIT9GKlZhvBB";
    private static final String SENDER_ACCOUNT = "NXT-XK4R-7VJU-6EQG-7R335";

    public static void main(String[] args) throws MalformedURLException {
        URL remoteUrl = new URL("https://testnxt.jelurida.com/nxt");
        URL localUrl = new URL("http://localhost:6876/nxt");
        MessageDecryption messageDecryption = new MessageDecryption();
        List<TransactionResponse> transactions = messageDecryption.getTransactions(	2490135, SENDER_ACCOUNT, remoteUrl);
        for (TransactionResponse transaction : transactions) {
            JO attachmentJson = transaction.getAttachmentJson();
            if (!attachmentJson.isExist("encryptedMessage")) {
                continue;
            }
            JO encryptedData = attachmentJson.getJo("encryptedMessage");
            JO response = DecryptFromCall.create().account(SENDER_ACCOUNT).secretPhrase(RECIPIENT_SECRET_PHRASE).
                    data(encryptedData.getString("data")).nonce(encryptedData.getString("nonce")).remote(localUrl).call();
            System.out.println(response);
        }
    }

    private List<TransactionResponse> getTransactions(int height, String account, URL url) {
        // Get the block timestamp from which to load transactions and load the contract account transactions
        BlockResponse block = GetBlockCall.create().height(height).remote(url).getBlock();
        GetBlockchainTransactionsCall getBlockchainTransactionsResponse = GetBlockchainTransactionsCall.create().
                timestamp(block.getTimestamp()).
                account(account).
                executedOnly(true).
                type(0).
                subtype(0).remote(url);
        List<TransactionResponse> transactionList = getBlockchainTransactionsResponse.getTransactions();
        return transactionList.stream().filter(t -> t.getSenderRs().equals(account)).collect(Collectors.toList());
    }
}
