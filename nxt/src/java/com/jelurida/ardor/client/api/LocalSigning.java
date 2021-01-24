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
import nxt.crypto.Crypto;
import nxt.http.callers.BroadcastTransactionCall;
import nxt.http.callers.SendMoneyCall;
import nxt.http.callers.SignTransactionCall;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Sample Java program which creates a transaction on a remote node, signs it locally, then broadcasts
 * the signed transaction bytes to the remote node.
 */
public class LocalSigning {

    private static final String SECRET_PHRASE = "hope peace happen touch easy pretend worthless talk them indeed wheel state"; // Only needed for signTransactionCall

    public static void main(String[] args) throws MalformedURLException {
        LocalSigning localSigning = new LocalSigning();
        localSigning.submitSignAndBroadcast();
    }

    private void submitSignAndBroadcast() throws MalformedURLException {
        URL localUrl = new URL("http://localhost:6876/nxt"); // Start your local testnet node and make sure it is fully synced with the blockchain
        URL remoteUrl = new URL("https://testnxt.jelurida.com/nxt"); // Jelurida remote testnet node
        byte[] publicKey = Crypto.getPublicKey(SECRET_PHRASE); // Use to generate unsigned transaction without revealing the secret phrase

        // This is just a sample, you can submit any transaction type using its specific caller
        JO unsignedTransactionResponse = submitRemotely(remoteUrl, publicKey);

        // Somehow transfer the unsigned transaction data to an offline workstation.
        // Then sign the transaction on the offline workstation
        JO signTransactionResponse = signLocally(localUrl, unsignedTransactionResponse);

        // Transfer the information signed transaction data to an online workstation
        // Then broadcast it to network
        broadcast(remoteUrl, signTransactionResponse);
    }

    private void broadcast(URL remoteUrl, JO signTransactionResponse) {
        JO signedTransactionJSON = signTransactionResponse.getJo("transactionJSON");
        JO broadcastResponse = BroadcastTransactionCall.create().
                transactionJSON(signedTransactionJSON.toJSONString()).
                remote(remoteUrl).
                call();
        System.out.printf("broadcastResponse: %s\n", broadcastResponse.toJSONString());
    }

    private JO signLocally(URL localUrl, JO unsignedTransactionResponse) {
        JO unsignedTransactionJSON = unsignedTransactionResponse.getJo("transactionJSON");
        JO signTransactionResponse = SignTransactionCall.create().
                unsignedTransactionJSON(unsignedTransactionJSON.toJSONString()).
                secretPhrase(SECRET_PHRASE).
                remote(localUrl).
                call();
        System.out.printf("signTransactionResponse: %s\n", signTransactionResponse.toJSONString());
        return signTransactionResponse;
    }

    private JO submitRemotely(URL remoteUrl, byte[] publicKey) {
        JO unsignedTransactionResponse = SendMoneyCall.create().
                recipient("NXT-KX2S-UULA-7YZ7-F3R8L").
                amountNQT(12345678).
                publicKey(publicKey).
                deadline(15).
                feeNQT(100000000). // See other examples for fee calculation
                broadcast(false).
                remote(remoteUrl).
                trustRemoteCertificate(true).
                call();
        System.out.printf("unsignedTransactionResponse: %s\n", unsignedTransactionResponse.toJSONString());
        return unsignedTransactionResponse;
    }
}
