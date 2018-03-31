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

package nxt.http.twophased;

import nxt.BlockchainTest;
import nxt.Constants;
import nxt.Nxt;
import nxt.http.APICall;
import nxt.http.twophased.TestCreateTwoPhased.TwoPhasedMoneyTransferBuilder;
import nxt.util.Logger;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class TestApproveTransaction extends BlockchainTest {

    @Test
    public void validVoteCasting() {
        int duration = 10;

        APICall apiCall = new TwoPhasedMoneyTransferBuilder()
                .finishHeight(Nxt.getBlockchain().getHeight() + duration)
                .build();

        JSONObject transactionJSON = TestCreateTwoPhased.issueCreateTwoPhased(apiCall, false);
        generateBlock();

        apiCall = new APICall.Builder("approveTransaction")
                .param("secretPhrase", CHUCK.getSecretPhrase())
                .param("transactionFullHash", (String) transactionJSON.get("fullHash"))
                .param("feeNQT", Constants.ONE_NXT)
                .build();

        JSONObject response = apiCall.invoke();
        Logger.logMessage("approvePhasedTransactionResponse:" + response.toJSONString());
        Assert.assertNotNull(response.get("transaction"));

        generateBlocks(duration);
        Assert.assertEquals(-50 * Constants.ONE_NXT - 2 * Constants.ONE_NXT, ALICE.getBalanceDiff());
        Assert.assertEquals(50 * Constants.ONE_NXT, BOB.getBalanceDiff());
        Assert.assertEquals(-Constants.ONE_NXT, CHUCK.getBalanceDiff());
    }

    @Test
    public void invalidVoteCasting() {
        int duration = 10;

        APICall apiCall = new TwoPhasedMoneyTransferBuilder()
                .finishHeight(Nxt.getBlockchain().getHeight() + duration)
                .build();

        JSONObject transactionJSON = TestCreateTwoPhased.issueCreateTwoPhased(apiCall, false);
        generateBlock();
        apiCall = new APICall.Builder("approveTransaction")
                .param("secretPhrase", DAVE.getSecretPhrase())
                .param("transactionFullHash", (String) transactionJSON.get("fullHash"))
                .param("feeNQT", Constants.ONE_NXT)
                .build();
        JSONObject response = apiCall.invoke();
        Assert.assertNotNull(response.get("error"));
        generateBlock();

        Assert.assertEquals("ALICE balance: ", -2 * Constants.ONE_NXT, ALICE.getBalanceDiff());
        Assert.assertEquals("BOB balance: ", 0, BOB.getBalanceDiff());
        Assert.assertEquals("CHUCK balance: ", 0, CHUCK.getBalanceDiff());
        Assert.assertEquals("DAVE balance: ", 0, DAVE.getBalanceDiff());

        generateBlocks(duration);

        Assert.assertEquals("ALICE balance: ", -2 * Constants.ONE_NXT, ALICE.getBalanceDiff());
        Assert.assertEquals("BOB balance: ", 0, BOB.getBalanceDiff());
        Assert.assertEquals("CHUCK balance: ", 0, CHUCK.getBalanceDiff());
        Assert.assertEquals("DAVE balance: ", 0, DAVE.getBalanceDiff());
    }

    @Test
    public void sendMoneyPhasedNoVoting() {
        long fee = 2*Constants.ONE_NXT;
        JSONObject response = new APICall.Builder("sendMoney").
                param("secretPhrase", ALICE.getSecretPhrase()).
                param("recipient", BOB.getStrId()).
                param("amountNQT", 100 * Constants.ONE_NXT).
                param("feeNQT", fee).
                param("phased", "true").
                param("phasingFinishHeight", baseHeight + 2).
                param("phasingVotingModel", -1).
                build().invoke();
        Logger.logDebugMessage("sendMoney: " + response);

        generateBlock();
        // Transaction is not applied yet, fee is paid
        // Forger
        Assert.assertEquals(fee, FORGY.getBalanceDiff());
        Assert.assertEquals(fee, FORGY.getUnconfirmedBalanceDiff());
        // Sender
        Assert.assertEquals(-fee, ALICE.getBalanceDiff());
        Assert.assertEquals(-100 * Constants.ONE_NXT - fee, ALICE.getUnconfirmedBalanceDiff());
        // Recipient
        Assert.assertEquals(0, BOB.getBalanceDiff());
        Assert.assertEquals(0, BOB.getUnconfirmedBalanceDiff());

        generateBlock();
        // Transaction is applied
        // Sender
        Assert.assertEquals(-100 * Constants.ONE_NXT - fee, ALICE.getBalanceDiff());
        Assert.assertEquals(-100 * Constants.ONE_NXT - fee, ALICE.getUnconfirmedBalanceDiff());
        // Recipient
        Assert.assertEquals(100 * Constants.ONE_NXT, BOB.getBalanceDiff());
        Assert.assertEquals(100 * Constants.ONE_NXT, BOB.getUnconfirmedBalanceDiff());
    }

    @Test
    public void sendMoneyPhasedByTransactionHash() {
        JSONObject response = getSignedBytes();
        Logger.logDebugMessage("signedSendMessage: " + response);
        String fullHash = (String)response.get("fullHash");
        Assert.assertEquals(64, fullHash.length());
        String approvalTransactionBytes = (String)response.get("transactionBytes");

        long fee = 3 * Constants.ONE_NXT;
        response = new APICall.Builder("sendMoney").
                param("secretPhrase", ALICE.getSecretPhrase()).
                param("recipient", BOB.getStrId()).
                param("amountNQT", 100 * Constants.ONE_NXT).
                param("feeNQT", fee).
                param("phased", "true").
                param("phasingFinishHeight", baseHeight + 3).
                param("phasingVotingModel", 4).
                param("phasingLinkedFullHash", fullHash).
                param("phasingQuorum", 1).
                build().invoke();
        Logger.logDebugMessage("sendMoney: " + response);

        generateBlock();
        // Transaction is not applied yet
        // Sender
        Assert.assertEquals(-fee, ALICE.getBalanceDiff());
        Assert.assertEquals(-100 * Constants.ONE_NXT - fee, ALICE.getUnconfirmedBalanceDiff());
        // Recipient
        Assert.assertEquals(0, BOB.getBalanceDiff());
        Assert.assertEquals(0, BOB.getUnconfirmedBalanceDiff());

        response = new APICall.Builder("broadcastTransaction").
                param("transactionBytes", approvalTransactionBytes).
                build().invoke();
        Logger.logDebugMessage("broadcastTransaction: " + response);
        generateBlock();

        // Transaction is applied before finish height
        // Sender
        Assert.assertEquals(-100 * Constants.ONE_NXT - fee, ALICE.getBalanceDiff());
        Assert.assertEquals(-100 * Constants.ONE_NXT - fee, ALICE.getUnconfirmedBalanceDiff());
        // Recipient
        Assert.assertEquals(100 * Constants.ONE_NXT, BOB.getBalanceDiff());
        Assert.assertEquals(100 * Constants.ONE_NXT, BOB.getUnconfirmedBalanceDiff());
    }

    @Test
    public void sendMoneyPhasedByTransactionHash2of3() {
        JSONObject response = getSignedBytes();
        Logger.logDebugMessage("signedSendMessage: " + response);
        String fullHash1 = (String)response.get("fullHash");
        Assert.assertEquals(64, fullHash1.length());
        String approvalTransactionBytes1 = (String)response.get("transactionBytes");
        response = getSignedBytes();
        Logger.logDebugMessage("signedSendMessage: " + response);
        String fullHash2 = (String)response.get("fullHash");
        Assert.assertEquals(64, fullHash1.length());
        response = getSignedBytes();
        Logger.logDebugMessage("signedSendMessage: " + response);
        String fullHash3 = (String)response.get("fullHash");
        Assert.assertEquals(64, fullHash1.length());
        String approvalTransactionBytes3 = (String)response.get("transactionBytes");

        long fee = 5 * Constants.ONE_NXT;
        response = new APICall.Builder("sendMoney").
                param("secretPhrase", ALICE.getSecretPhrase()).
                param("recipient", BOB.getStrId()).
                param("amountNQT", 100 * Constants.ONE_NXT).
                param("feeNQT", fee).
                param("phased", "true").
                param("phasingFinishHeight", baseHeight + 2).
                param("phasingVotingModel", 4).
                param("phasingLinkedFullHash", new String[] { fullHash1, fullHash2, fullHash3 }).
                param("phasingQuorum", 2).
                build().invoke();
        Logger.logDebugMessage("sendMoney: " + response);

        generateBlock();
        // Transaction is not applied yet
        // Sender
        Assert.assertEquals(-fee, ALICE.getBalanceDiff());
        Assert.assertEquals(-100 * Constants.ONE_NXT - fee, ALICE.getUnconfirmedBalanceDiff());
        // Recipient
        Assert.assertEquals(0, BOB.getBalanceDiff());
        Assert.assertEquals(0, BOB.getUnconfirmedBalanceDiff());

        response = new APICall.Builder("broadcastTransaction").
                param("transactionBytes", approvalTransactionBytes1).
                build().invoke();
        Logger.logDebugMessage("broadcastTransaction: " + response);
        response = new APICall.Builder("broadcastTransaction").
                param("transactionBytes", approvalTransactionBytes3).
                build().invoke();
        Logger.logDebugMessage("broadcastTransaction: " + response);
        generateBlock();

        // Transaction is applied since 2 out 3 hashes were provided
        // Sender
        Assert.assertEquals(-100 * Constants.ONE_NXT - fee, ALICE.getBalanceDiff());
        Assert.assertEquals(-100 * Constants.ONE_NXT - fee, ALICE.getUnconfirmedBalanceDiff());
        // Recipient
        Assert.assertEquals(100 * Constants.ONE_NXT, BOB.getBalanceDiff());
        Assert.assertEquals(100 * Constants.ONE_NXT, BOB.getUnconfirmedBalanceDiff());
    }

    @Test
    public void sendMoneyPhasedByTransactionHashNotApplied() {
        long fee = 3 * Constants.ONE_NXT;
        JSONObject response = new APICall.Builder("sendMoney").
                param("secretPhrase", ALICE.getSecretPhrase()).
                param("recipient", BOB.getStrId()).
                param("amountNQT", 100 * Constants.ONE_NXT).
                param("feeNQT", fee).
                param("phased", "true").
                param("phasingFinishHeight", baseHeight + 2).
                param("phasingVotingModel", 4).
                param("phasingLinkedFullHash", "a13bbe67211fea8d59b2621f1e0118bb242dc5000d428a23a8bd47491a05d681"). // this hash does not match any transaction
                param("phasingQuorum", 1).
                build().invoke();
        Logger.logDebugMessage("sendMoney: " + response);

        generateBlock();
        // Transaction is not applied yet
        // Sender
        Assert.assertEquals(-fee, ALICE.getBalanceDiff());
        Assert.assertEquals(-100 * Constants.ONE_NXT - fee, ALICE.getUnconfirmedBalanceDiff());
        // Recipient
        Assert.assertEquals(0, BOB.getBalanceDiff());
        Assert.assertEquals(0, BOB.getUnconfirmedBalanceDiff());

        generateBlock();
        // Transaction is rejected since full hash does not match
        // Sender
        Assert.assertEquals(-fee, ALICE.getBalanceDiff());
        Assert.assertEquals(-fee, ALICE.getUnconfirmedBalanceDiff());
        // Recipient
        Assert.assertEquals(0, BOB.getBalanceDiff());
        Assert.assertEquals(0, BOB.getUnconfirmedBalanceDiff());
    }

    @Test
    public void setAliasPhasedByTransactionHashInvalid() {
        JSONObject response = getSignedBytes();
        Logger.logDebugMessage("signedSendMessage: " + response);
        String fullHash = (String)response.get("fullHash");
        Assert.assertEquals(64, fullHash.length());
        String approvalTransactionBytes = (String)response.get("transactionBytes");

        long fee = 2 * Constants.ONE_NXT;
        String alias = "alias" + System.currentTimeMillis();
        response = new APICall.Builder("setAlias").
                param("secretPhrase", ALICE.getSecretPhrase()).
                param("aliasName", alias).
                param("feeNQT", fee).
                param("phased", "true").
                param("phasingFinishHeight", baseHeight + 4).
                param("phasingVotingModel", 4).
                param("phasingLinkedFullHash", fullHash).
                param("phasingQuorum", 1).
                build().invoke();
        Logger.logDebugMessage("setAlias: " + response);

        generateBlock();
        response = new APICall.Builder("getAlias").
                param("aliasName", alias).
                build().invoke();
        Logger.logDebugMessage("getAlias: " + response);
        Assert.assertEquals((long)5, response.get("errorCode"));

        response = new APICall.Builder("broadcastTransaction").
                param("transactionBytes", approvalTransactionBytes).
                build().invoke();
        Logger.logDebugMessage("broadcastTransaction: " + response);
        generateBlock();

        // allocate the same alias immediately
        response = new APICall.Builder("setAlias").
                param("secretPhrase", BOB.getSecretPhrase()).
                param("aliasName", alias).
                param("feeNQT", fee).
                build().invoke();
        Logger.logDebugMessage("setSameAlias: " + response);
        generateBlock();
        // phased setAlias transaction is applied but invalid
        response = new APICall.Builder("getAlias").
                param("aliasName", alias).
                build().invoke();
        Logger.logDebugMessage("getAlias: " + response);
        Assert.assertEquals(BOB.getStrId(), response.get("account"));
        generateBlock();
        // phased setAlias transaction is applied but invalid
        response = new APICall.Builder("getAlias").
                param("aliasName", alias).
                build().invoke();
        Logger.logDebugMessage("getAlias: " + response);
        Assert.assertEquals(BOB.getStrId(), response.get("account"));
    }

    private JSONObject getSignedBytes() {
        JSONObject response = new APICall.Builder("sendMessage").
                param("publicKey", CHUCK.getPublicKeyStr()).
                param("recipient", ALICE.getStrId()).
                param("message", "approval notice").
                param("feeNQT", Constants.ONE_NXT).
                build().invoke();
        Logger.logDebugMessage("sendMessage not broadcasted: " + response);
        response = new APICall.Builder("signTransaction").
                param("secretPhrase", CHUCK.getSecretPhrase()).
                param("unsignedTransactionBytes", (String)response.get("unsignedTransactionBytes")).
                build().invoke();
        return response;
    }
}