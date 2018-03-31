/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2017 Jelurida IP B.V.
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

package nxt.http;

import nxt.BlockchainTest;
import nxt.Constants;
import nxt.util.Logger;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class SendMoneyTest extends BlockchainTest {

    @Test
    public void sendMoney() {
        JSONObject response = new APICall.Builder("sendMoney").
                param("secretPhrase", ALICE.getSecretPhrase()).
                param("recipient", BOB.getStrId()).
                param("amountNQT", 100 * Constants.ONE_NXT).
                param("feeNQT", Constants.ONE_NXT).
                build().invoke();
        Logger.logDebugMessage("sendMoney: " + response);
        // Forger
        Assert.assertEquals(0, FORGY.getBalanceDiff());
        Assert.assertEquals(0, FORGY.getUnconfirmedBalanceDiff());
        // Sender
        Assert.assertEquals(0, ALICE.getBalanceDiff());
        Assert.assertEquals(-100 * Constants.ONE_NXT - Constants.ONE_NXT, ALICE.getUnconfirmedBalanceDiff());
        // Recipient
        Assert.assertEquals(0, BOB.getBalanceDiff());
        Assert.assertEquals(0, BOB.getUnconfirmedBalanceDiff());
        generateBlock();
        // Forger
        Assert.assertEquals(Constants.ONE_NXT, FORGY.getBalanceDiff());
        Assert.assertEquals(Constants.ONE_NXT, FORGY.getUnconfirmedBalanceDiff());
        // Sender
        Assert.assertEquals(-100 * Constants.ONE_NXT - Constants.ONE_NXT, ALICE.getBalanceDiff());
        Assert.assertEquals(-100 * Constants.ONE_NXT - Constants.ONE_NXT, ALICE.getUnconfirmedBalanceDiff());
        // Recipient
        Assert.assertEquals(100 * Constants.ONE_NXT, BOB.getBalanceDiff());
        Assert.assertEquals(100 * Constants.ONE_NXT, BOB.getUnconfirmedBalanceDiff());
    }

    @Test
    public void sendTooMuchMoney() {
        JSONObject response = new APICall.Builder("sendMoney").
                param("secretPhrase", ALICE.getSecretPhrase()).
                param("recipient", BOB.getStrId()).
                param("amountNQT", ALICE.getInitialBalance()).
                param("feeNQT", Constants.ONE_NXT).
                build().invoke();
        Logger.logDebugMessage("sendMoney: " + response);
        Assert.assertEquals((long)6, response.get("errorCode"));
    }

    @Test
    public void sendAndReturn() {
        JSONObject response = new APICall.Builder("sendMoney").
                param("secretPhrase", ALICE.getSecretPhrase()).
                param("recipient", BOB.getStrId()).
                param("amountNQT", 100 * Constants.ONE_NXT).
                param("feeNQT", Constants.ONE_NXT).
                build().invoke();
        Logger.logDebugMessage("sendMoney1: " + response);
        response = new APICall.Builder("sendMoney").
                param("secretPhrase", BOB.getSecretPhrase()).
                param("recipient", ALICE.getStrId()).
                param("amountNQT", 100 * Constants.ONE_NXT).
                param("feeNQT", Constants.ONE_NXT).
                build().invoke();
        Logger.logDebugMessage("sendMoney2: " + response);
        // Forger
        Assert.assertEquals(0, FORGY.getBalanceDiff());
        Assert.assertEquals(0, FORGY.getUnconfirmedBalanceDiff());
        // Sender
        Assert.assertEquals(0, ALICE.getBalanceDiff());
        Assert.assertEquals(-100 * Constants.ONE_NXT - Constants.ONE_NXT, ALICE.getUnconfirmedBalanceDiff());
        // Recipient
        Assert.assertEquals(0, BOB.getBalanceDiff());
        Assert.assertEquals(-100 * Constants.ONE_NXT - Constants.ONE_NXT, BOB.getUnconfirmedBalanceDiff());
        generateBlock();
        // Forger
        Assert.assertEquals(2*Constants.ONE_NXT, FORGY.getBalanceDiff());
        Assert.assertEquals(2*Constants.ONE_NXT, FORGY.getUnconfirmedBalanceDiff());
        // Sender
        Assert.assertEquals(-Constants.ONE_NXT, ALICE.getBalanceDiff());
        Assert.assertEquals(-Constants.ONE_NXT, ALICE.getUnconfirmedBalanceDiff());
        // Recipient
        Assert.assertEquals(-Constants.ONE_NXT, BOB.getBalanceDiff());
        Assert.assertEquals(-Constants.ONE_NXT, BOB.getUnconfirmedBalanceDiff());
    }

    @Test
    public void signAndBroadcastBytes() {
        JSONObject response = new APICall.Builder("sendMoney").
                param("publicKey", ALICE.getPublicKeyStr()).
                param("recipient", BOB.getStrId()).
                param("amountNQT", 100 * Constants.ONE_NXT).
                param("feeNQT", Constants.ONE_NXT).
                build().invoke();
        Logger.logDebugMessage("sendMoney: " + response);
        generateBlock();
        // No change transaction not broadcast
        Assert.assertEquals(0, ALICE.getBalanceDiff());
        Assert.assertEquals(0, ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, BOB.getBalanceDiff());
        Assert.assertEquals(0, BOB.getUnconfirmedBalanceDiff());

        response = new APICall.Builder("signTransaction").
                param("secretPhrase", ALICE.getSecretPhrase()).
                param("unsignedTransactionBytes", (String)response.get("unsignedTransactionBytes")).
                build().invoke();
        Logger.logDebugMessage("signTransaction: " + response);

        response = new APICall.Builder("broadcastTransaction").
                param("transactionBytes", (String)response.get("transactionBytes")).
                build().invoke();
        Logger.logDebugMessage("broadcastTransaction: " + response);
        generateBlock();

        // Sender
        Assert.assertEquals(-100 * Constants.ONE_NXT - Constants.ONE_NXT, ALICE.getBalanceDiff());
        Assert.assertEquals(-100 * Constants.ONE_NXT - Constants.ONE_NXT, ALICE.getUnconfirmedBalanceDiff());
        // Recipient
        Assert.assertEquals(100 * Constants.ONE_NXT, BOB.getBalanceDiff());
        Assert.assertEquals(100 * Constants.ONE_NXT, BOB.getUnconfirmedBalanceDiff());
    }

    @Test
    public void signAndBroadcastJSON() {
        JSONObject response = new APICall.Builder("sendMoney").
                param("publicKey", ALICE.getPublicKeyStr()).
                param("recipient", BOB.getStrId()).
                param("amountNQT", 100 * Constants.ONE_NXT).
                param("feeNQT", Constants.ONE_NXT).
                build().invoke();
        Logger.logDebugMessage("sendMoney: " + response);
        generateBlock();
        // No change transaction not broadcast
        Assert.assertEquals(0, ALICE.getBalanceDiff());
        Assert.assertEquals(0, ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, BOB.getBalanceDiff());
        Assert.assertEquals(0, BOB.getUnconfirmedBalanceDiff());

        response = new APICall.Builder("signTransaction").
                param("secretPhrase", ALICE.getSecretPhrase()).
                param("unsignedTransactionJSON", response.get("transactionJSON").toString()).
                build().invoke();
        Logger.logDebugMessage("signTransaction: " + response);

        response = new APICall.Builder("broadcastTransaction").
                param("transactionBytes", (String)response.get("transactionBytes")).
                build().invoke();
        Logger.logDebugMessage("broadcastTransaction: " + response);
        generateBlock();

        // Sender
        Assert.assertEquals(-100 * Constants.ONE_NXT - Constants.ONE_NXT, ALICE.getBalanceDiff());
        Assert.assertEquals(-100 * Constants.ONE_NXT - Constants.ONE_NXT, ALICE.getUnconfirmedBalanceDiff());
        // Recipient
        Assert.assertEquals(100 * Constants.ONE_NXT, BOB.getBalanceDiff());
        Assert.assertEquals(100 * Constants.ONE_NXT, BOB.getUnconfirmedBalanceDiff());
    }
}
