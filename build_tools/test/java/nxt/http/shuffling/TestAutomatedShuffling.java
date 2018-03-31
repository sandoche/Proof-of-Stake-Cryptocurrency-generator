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

package nxt.http.shuffling;

import nxt.Block;
import nxt.BlockchainTest;
import nxt.Constants;
import nxt.Nxt;
import nxt.Shuffler;
import nxt.Shuffling;
import nxt.ShufflingTransaction;
import nxt.Tester;
import nxt.Transaction;
import nxt.crypto.AnonymouslyEncryptedData;
import nxt.util.Convert;
import nxt.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static nxt.http.shuffling.ShufflingUtil.ALICE_RECIPIENT;
import static nxt.http.shuffling.ShufflingUtil.BOB_RECIPIENT;
import static nxt.http.shuffling.ShufflingUtil.CHUCK_RECIPIENT;
import static nxt.http.shuffling.ShufflingUtil.DAVE_RECIPIENT;
import static nxt.http.shuffling.ShufflingUtil.broadcast;
import static nxt.http.shuffling.ShufflingUtil.cancel;
import static nxt.http.shuffling.ShufflingUtil.create;
import static nxt.http.shuffling.ShufflingUtil.createAssetShuffling;
import static nxt.http.shuffling.ShufflingUtil.createCurrencyShuffling;
import static nxt.http.shuffling.ShufflingUtil.defaultHoldingShufflingAmount;
import static nxt.http.shuffling.ShufflingUtil.defaultShufflingAmount;
import static nxt.http.shuffling.ShufflingUtil.getShuffling;
import static nxt.http.shuffling.ShufflingUtil.getShufflingParticipants;
import static nxt.http.shuffling.ShufflingUtil.process;
import static nxt.http.shuffling.ShufflingUtil.register;
import static nxt.http.shuffling.ShufflingUtil.shufflingAsset;
import static nxt.http.shuffling.ShufflingUtil.shufflingCurrency;
import static nxt.http.shuffling.ShufflingUtil.startShuffler;
import static nxt.http.shuffling.ShufflingUtil.stopShuffler;
import static nxt.http.shuffling.ShufflingUtil.verify;

public class TestAutomatedShuffling extends BlockchainTest {

    @Before
    public void stopAllShufflers() {
        Shuffler.stopAllShufflers();
    }

    @Test
    public void successfulShuffling() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        startShuffler(CHUCK, CHUCK_RECIPIENT, shufflingFullHash);
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);
        generateBlock();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignore) {}
        Assert.assertEquals(-Constants.ONE_NXT, ALICE.getBalanceDiff());
        Assert.assertEquals(-(defaultShufflingAmount + 11 * Constants.ONE_NXT), ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-Constants.ONE_NXT, BOB.getBalanceDiff());
        Assert.assertEquals(-(defaultShufflingAmount + Constants.ONE_NXT), BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-Constants.ONE_NXT, CHUCK.getBalanceDiff());
        Assert.assertEquals(-(defaultShufflingAmount + Constants.ONE_NXT), CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-Constants.ONE_NXT, DAVE.getBalanceDiff());
        Assert.assertEquals(-(defaultShufflingAmount + Constants.ONE_NXT), DAVE.getUnconfirmedBalanceDiff());

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject getParticipantsResponse = getShufflingParticipants(shufflingId);
        JSONArray participants = (JSONArray)getParticipantsResponse.get("participants");
        Assert.assertEquals(4, participants.size());
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(ALICE.getId()), shufflingAssignee);

        for (int i = 0; i < 4; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.VERIFICATION.getCode(), getShufflingResponse.get("stage"));

        generateBlock();
        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.DONE.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertNull(shufflingAssignee);

        Assert.assertEquals(-(defaultShufflingAmount + 12 * Constants.ONE_NXT), ALICE.getBalanceDiff());
        Assert.assertEquals(-(defaultShufflingAmount + 12 * Constants.ONE_NXT), ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(defaultShufflingAmount + 12 * Constants.ONE_NXT), BOB.getBalanceDiff());
        Assert.assertEquals(-(defaultShufflingAmount + 12 * Constants.ONE_NXT), BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(defaultShufflingAmount + 12 * Constants.ONE_NXT), CHUCK.getBalanceDiff());
        Assert.assertEquals(-(defaultShufflingAmount + 12 * Constants.ONE_NXT), CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(defaultShufflingAmount + 12 * Constants.ONE_NXT), DAVE.getBalanceDiff());
        Assert.assertEquals(-(defaultShufflingAmount + 12 * Constants.ONE_NXT), DAVE.getUnconfirmedBalanceDiff());

        Assert.assertEquals(defaultShufflingAmount, ALICE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(defaultShufflingAmount, ALICE_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(defaultShufflingAmount, BOB_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(defaultShufflingAmount, BOB_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(defaultShufflingAmount, CHUCK_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(defaultShufflingAmount, CHUCK_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(defaultShufflingAmount, DAVE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(defaultShufflingAmount, DAVE_RECIPIENT.getUnconfirmedBalanceDiff());

        Assert.assertEquals(48 * Constants.ONE_NXT, FORGY.getBalanceDiff());
        Assert.assertEquals(48 * Constants.ONE_NXT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void successfulRestartShuffling() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        startShuffler(CHUCK, CHUCK_RECIPIENT, shufflingFullHash);
        stopShuffler(CHUCK, shufflingFullHash);
        startShuffler(CHUCK, CHUCK_RECIPIENT, shufflingFullHash);
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);
        generateBlock();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignore) {}
        Assert.assertEquals(-Constants.ONE_NXT, ALICE.getBalanceDiff());
        Assert.assertEquals(-(defaultShufflingAmount + 11 * Constants.ONE_NXT), ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-Constants.ONE_NXT, BOB.getBalanceDiff());
        Assert.assertEquals(-(defaultShufflingAmount + Constants.ONE_NXT), BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-Constants.ONE_NXT, CHUCK.getBalanceDiff());
        Assert.assertEquals(-(defaultShufflingAmount + Constants.ONE_NXT), CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-Constants.ONE_NXT, DAVE.getBalanceDiff());
        Assert.assertEquals(-(defaultShufflingAmount + Constants.ONE_NXT), DAVE.getUnconfirmedBalanceDiff());

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject getParticipantsResponse = getShufflingParticipants(shufflingId);
        JSONArray participants = (JSONArray)getParticipantsResponse.get("participants");
        Assert.assertEquals(4, participants.size());
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(ALICE.getId()), shufflingAssignee);

        stopShuffler(CHUCK, shufflingFullHash);
        startShuffler(CHUCK, CHUCK_RECIPIENT, shufflingFullHash);

        for (int i = 0; i < 2; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        stopShuffler(BOB, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);

        for (int i = 0; i < 2; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        stopShuffler(ALICE, shufflingFullHash);
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.VERIFICATION.getCode(), getShufflingResponse.get("stage"));

        generateBlock();
        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.DONE.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertNull(shufflingAssignee);

        Assert.assertEquals(-(defaultShufflingAmount + 12 * Constants.ONE_NXT), ALICE.getBalanceDiff());
        Assert.assertEquals(-(defaultShufflingAmount + 12 * Constants.ONE_NXT), ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(defaultShufflingAmount + 12 * Constants.ONE_NXT), BOB.getBalanceDiff());
        Assert.assertEquals(-(defaultShufflingAmount + 12 * Constants.ONE_NXT), BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(defaultShufflingAmount + 12 * Constants.ONE_NXT), CHUCK.getBalanceDiff());
        Assert.assertEquals(-(defaultShufflingAmount + 12 * Constants.ONE_NXT), CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(defaultShufflingAmount + 12 * Constants.ONE_NXT), DAVE.getBalanceDiff());
        Assert.assertEquals(-(defaultShufflingAmount + 12 * Constants.ONE_NXT), DAVE.getUnconfirmedBalanceDiff());

        Assert.assertEquals(defaultShufflingAmount, ALICE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(defaultShufflingAmount, ALICE_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(defaultShufflingAmount, BOB_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(defaultShufflingAmount, BOB_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(defaultShufflingAmount, CHUCK_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(defaultShufflingAmount, CHUCK_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(defaultShufflingAmount, DAVE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(defaultShufflingAmount, DAVE_RECIPIENT.getUnconfirmedBalanceDiff());

        Assert.assertEquals(48 * Constants.ONE_NXT, FORGY.getBalanceDiff());
        Assert.assertEquals(48 * Constants.ONE_NXT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void successfulAssetShuffling() {
        JSONObject shufflingCreate = createAssetShuffling(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        startShuffler(CHUCK, CHUCK_RECIPIENT, shufflingFullHash);
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);

        for (int i = 0; i < 6; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.DONE.getCode(), getShufflingResponse.get("stage"));
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertNull(shufflingAssignee);

        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 12 * Constants.ONE_NXT), ALICE.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 12 * Constants.ONE_NXT), ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 12 * Constants.ONE_NXT), BOB.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 12 * Constants.ONE_NXT), BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 12 * Constants.ONE_NXT), CHUCK.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 12 * Constants.ONE_NXT), CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 12 * Constants.ONE_NXT), DAVE.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 12 * Constants.ONE_NXT), DAVE.getUnconfirmedBalanceDiff());

        Assert.assertEquals(Constants.SHUFFLING_DEPOSIT_NQT, ALICE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(Constants.SHUFFLING_DEPOSIT_NQT, ALICE_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(Constants.SHUFFLING_DEPOSIT_NQT, BOB_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(Constants.SHUFFLING_DEPOSIT_NQT, BOB_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(Constants.SHUFFLING_DEPOSIT_NQT, CHUCK_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(Constants.SHUFFLING_DEPOSIT_NQT, CHUCK_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(Constants.SHUFFLING_DEPOSIT_NQT, DAVE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(Constants.SHUFFLING_DEPOSIT_NQT, DAVE_RECIPIENT.getUnconfirmedBalanceDiff());

        Assert.assertEquals(48 * Constants.ONE_NXT, FORGY.getBalanceDiff());
        Assert.assertEquals(48 * Constants.ONE_NXT, FORGY.getUnconfirmedBalanceDiff());

        Assert.assertEquals(defaultHoldingShufflingAmount, ALICE_RECIPIENT.getAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(defaultHoldingShufflingAmount, ALICE_RECIPIENT.getUnconfirmedAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(defaultHoldingShufflingAmount, BOB_RECIPIENT.getAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(defaultHoldingShufflingAmount, BOB_RECIPIENT.getUnconfirmedAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(defaultHoldingShufflingAmount, CHUCK_RECIPIENT.getAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(defaultHoldingShufflingAmount, CHUCK_RECIPIENT.getUnconfirmedAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(defaultHoldingShufflingAmount, DAVE_RECIPIENT.getAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(defaultHoldingShufflingAmount, DAVE_RECIPIENT.getUnconfirmedAssetQuantityDiff(shufflingAsset));

        Assert.assertEquals(-defaultHoldingShufflingAmount, ALICE.getAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(-defaultHoldingShufflingAmount, ALICE.getUnconfirmedAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(-defaultHoldingShufflingAmount, BOB.getAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(-defaultHoldingShufflingAmount, BOB.getUnconfirmedAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(-defaultHoldingShufflingAmount, CHUCK.getAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(-defaultHoldingShufflingAmount, CHUCK.getUnconfirmedAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(-defaultHoldingShufflingAmount, DAVE.getAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(-defaultHoldingShufflingAmount, DAVE.getUnconfirmedAssetQuantityDiff(shufflingAsset));

    }

    @Test
    public void successfulCurrencyShuffling() {
        JSONObject shufflingCreate = createCurrencyShuffling(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        startShuffler(CHUCK, CHUCK_RECIPIENT, shufflingFullHash);
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);

        for (int i = 0; i < 6; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.DONE.getCode(), getShufflingResponse.get("stage"));
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertNull(shufflingAssignee);

        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 12 * Constants.ONE_NXT), ALICE.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 12 * Constants.ONE_NXT), ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 12 * Constants.ONE_NXT), BOB.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 12 * Constants.ONE_NXT), BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 12 * Constants.ONE_NXT), CHUCK.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 12 * Constants.ONE_NXT), CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 12 * Constants.ONE_NXT), DAVE.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 12 * Constants.ONE_NXT), DAVE.getUnconfirmedBalanceDiff());

        Assert.assertEquals(Constants.SHUFFLING_DEPOSIT_NQT, ALICE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(Constants.SHUFFLING_DEPOSIT_NQT, ALICE_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(Constants.SHUFFLING_DEPOSIT_NQT, BOB_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(Constants.SHUFFLING_DEPOSIT_NQT, BOB_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(Constants.SHUFFLING_DEPOSIT_NQT, CHUCK_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(Constants.SHUFFLING_DEPOSIT_NQT, CHUCK_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(Constants.SHUFFLING_DEPOSIT_NQT, DAVE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(Constants.SHUFFLING_DEPOSIT_NQT, DAVE_RECIPIENT.getUnconfirmedBalanceDiff());

        Assert.assertEquals(48 * Constants.ONE_NXT, FORGY.getBalanceDiff());
        Assert.assertEquals(48 * Constants.ONE_NXT, FORGY.getUnconfirmedBalanceDiff());

        Assert.assertEquals(defaultHoldingShufflingAmount, ALICE_RECIPIENT.getCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(defaultHoldingShufflingAmount, ALICE_RECIPIENT.getUnconfirmedCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(defaultHoldingShufflingAmount, BOB_RECIPIENT.getCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(defaultHoldingShufflingAmount, BOB_RECIPIENT.getUnconfirmedCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(defaultHoldingShufflingAmount, CHUCK_RECIPIENT.getCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(defaultHoldingShufflingAmount, CHUCK_RECIPIENT.getUnconfirmedCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(defaultHoldingShufflingAmount, DAVE_RECIPIENT.getCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(defaultHoldingShufflingAmount, DAVE_RECIPIENT.getUnconfirmedCurrencyUnitsDiff(shufflingCurrency));

        Assert.assertEquals(-defaultHoldingShufflingAmount, ALICE.getCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(-defaultHoldingShufflingAmount, ALICE.getUnconfirmedCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(-defaultHoldingShufflingAmount, BOB.getCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(-defaultHoldingShufflingAmount, BOB.getUnconfirmedCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(-defaultHoldingShufflingAmount, CHUCK.getCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(-defaultHoldingShufflingAmount, CHUCK.getUnconfirmedCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(-defaultHoldingShufflingAmount, DAVE.getCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(-defaultHoldingShufflingAmount, DAVE.getUnconfirmedCurrencyUnitsDiff(shufflingCurrency));

    }

    @Test
    public void registrationNotFinished() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);

        for (int i = 0; i < 9; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));

        JSONObject getParticipantsResponse = getShufflingParticipants(shufflingId);
        JSONArray participants = (JSONArray)getParticipantsResponse.get("participants");
        Assert.assertEquals(2, participants.size());
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertNull(shufflingAssignee);

        Assert.assertEquals(-Constants.ONE_NXT, ALICE.getBalanceDiff());
        Assert.assertEquals(-Constants.ONE_NXT, ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-Constants.ONE_NXT, BOB.getBalanceDiff());
        Assert.assertEquals(-Constants.ONE_NXT, BOB.getUnconfirmedBalanceDiff());

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());

        Assert.assertEquals(2 * Constants.ONE_NXT, FORGY.getBalanceDiff());
        Assert.assertEquals(2 * Constants.ONE_NXT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void registrationNotFinishedAsset() {
        JSONObject shufflingCreate = createAssetShuffling(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);

        for (int i = 0; i < 9; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));

        JSONObject getParticipantsResponse = getShufflingParticipants(shufflingId);
        JSONArray participants = (JSONArray)getParticipantsResponse.get("participants");
        Assert.assertEquals(2, participants.size());
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertNull(shufflingAssignee);

        Assert.assertEquals(-Constants.ONE_NXT, ALICE.getBalanceDiff());
        Assert.assertEquals(-Constants.ONE_NXT, ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-Constants.ONE_NXT, BOB.getBalanceDiff());
        Assert.assertEquals(-Constants.ONE_NXT, BOB.getUnconfirmedBalanceDiff());

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());

        Assert.assertEquals(0, ALICE.getAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(0, ALICE.getUnconfirmedAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(0, BOB.getAssetQuantityDiff(shufflingAsset));
        Assert.assertEquals(0, BOB.getUnconfirmedAssetQuantityDiff(shufflingAsset));

        Assert.assertEquals(2 * Constants.ONE_NXT, FORGY.getBalanceDiff());
        Assert.assertEquals(2 * Constants.ONE_NXT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void processingNotStarted() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        startShuffler(CHUCK, CHUCK_RECIPIENT, shufflingFullHash);
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);

        for (int i = 0; i < 10; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));

        JSONObject getParticipantsResponse = getShufflingParticipants(shufflingId);
        JSONArray participants = (JSONArray)getParticipantsResponse.get("participants");
        Assert.assertEquals(4, participants.size());
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(ALICE.getStrId(), shufflingAssignee);

        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + Constants.ONE_NXT), ALICE.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + Constants.ONE_NXT), ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-Constants.ONE_NXT, BOB.getBalanceDiff());
        Assert.assertEquals(-Constants.ONE_NXT, BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-Constants.ONE_NXT, CHUCK.getBalanceDiff());
        Assert.assertEquals(-Constants.ONE_NXT, CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-Constants.ONE_NXT, DAVE.getBalanceDiff());
        Assert.assertEquals(-Constants.ONE_NXT, DAVE.getUnconfirmedBalanceDiff());

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());
        Assert.assertNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(4 * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals(4 * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void processingNotStartedCurrency() {
        JSONObject shufflingCreate = createCurrencyShuffling(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        startShuffler(CHUCK, CHUCK_RECIPIENT, shufflingFullHash);
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);

        for (int i = 0; i < 10; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));

        JSONObject getParticipantsResponse = getShufflingParticipants(shufflingId);
        JSONArray participants = (JSONArray)getParticipantsResponse.get("participants");
        Assert.assertEquals(4, participants.size());
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(ALICE.getStrId(), shufflingAssignee);

        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + Constants.ONE_NXT), ALICE.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + Constants.ONE_NXT), ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-Constants.ONE_NXT, BOB.getBalanceDiff());
        Assert.assertEquals(-Constants.ONE_NXT, BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-Constants.ONE_NXT, CHUCK.getBalanceDiff());
        Assert.assertEquals(-Constants.ONE_NXT, CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-Constants.ONE_NXT, DAVE.getBalanceDiff());
        Assert.assertEquals(-Constants.ONE_NXT, DAVE.getUnconfirmedBalanceDiff());

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());
        Assert.assertNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(0, ALICE.getCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(0, ALICE.getUnconfirmedCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(0, BOB.getCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(0, BOB.getUnconfirmedCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(0, CHUCK.getCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(0, CHUCK.getUnconfirmedCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(0, DAVE.getCurrencyUnitsDiff(shufflingCurrency));
        Assert.assertEquals(0, DAVE.getUnconfirmedCurrencyUnitsDiff(shufflingCurrency));

        Assert.assertEquals(4 * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals(4 * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void tooManyParticipants() {
        JSONObject shufflingCreate = create(ALICE, 3);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        startShuffler(CHUCK, CHUCK_RECIPIENT, shufflingFullHash);
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);

        for (int i = 0; i < 10; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));

        JSONObject getParticipantsResponse = getShufflingParticipants(shufflingId);
        JSONArray participants = (JSONArray)getParticipantsResponse.get("participants");
        Assert.assertEquals(3, participants.size());
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(ALICE.getStrId(), shufflingAssignee);

        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + Constants.ONE_NXT), ALICE.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + Constants.ONE_NXT), ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-Constants.ONE_NXT, BOB.getBalanceDiff());
        Assert.assertEquals(-Constants.ONE_NXT, BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-Constants.ONE_NXT, CHUCK.getBalanceDiff());
        Assert.assertEquals(-Constants.ONE_NXT, CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, DAVE.getBalanceDiff());
        Assert.assertEquals(0, DAVE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(3 * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals(3 * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());
        Assert.assertNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNull(DAVE_RECIPIENT.getAccount());
    }

    @Test
    public void processingNotFinished() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        startShuffler(CHUCK, CHUCK_RECIPIENT, shufflingFullHash);
        generateBlock();
        register(shufflingFullHash, DAVE);
        generateBlock();

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        for (int i = 0; i < 15; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(DAVE.getId()), shufflingAssignee);

        Assert.assertEquals(-11 * Constants.ONE_NXT, ALICE.getBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_NXT, ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_NXT, BOB.getBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_NXT, BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_NXT, CHUCK.getBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_NXT, CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + Constants.ONE_NXT), DAVE.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + Constants.ONE_NXT), DAVE.getUnconfirmedBalanceDiff());

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());
        Assert.assertNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(34 * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals(34 * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void verifyNotFinished() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        generateBlock();
        register(shufflingFullHash, CHUCK);
        generateBlock();
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);
        generateBlock();

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        for (int i = 0; i < 5; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        process(shufflingId, CHUCK, CHUCK_RECIPIENT);

        for (int i = 0; i < 2; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.VERIFICATION.getCode(), getShufflingResponse.get("stage"));

        for (int i = 0; i < 15; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);

        Assert.assertEquals(-12 * Constants.ONE_NXT, ALICE.getBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_NXT, ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_NXT, BOB.getBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_NXT, BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 11 * Constants.ONE_NXT), CHUCK.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 11 * Constants.ONE_NXT), CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_NXT, DAVE.getBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_NXT, DAVE.getUnconfirmedBalanceDiff());

        Assert.assertNotNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNotNull(BOB_RECIPIENT.getAccount());
        Assert.assertNotNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNotNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(0, ALICE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, ALICE_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, BOB_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, BOB_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, CHUCK_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, CHUCK_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, DAVE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, DAVE_RECIPIENT.getUnconfirmedBalanceDiff());

        Assert.assertEquals(47 * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals(47 * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void verifyNotFinishedRestart() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        generateBlock();
        register(shufflingFullHash, CHUCK);
        generateBlock();
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);
        generateBlock();

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        for (int i = 0; i < 5; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        stopShuffler(BOB, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);

        process(shufflingId, CHUCK, CHUCK_RECIPIENT);

        for (int i = 0; i < 2; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.VERIFICATION.getCode(), getShufflingResponse.get("stage"));

        stopShuffler(ALICE, shufflingFullHash);
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);

        for (int i = 0; i < 15; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);

        Assert.assertEquals(-12 * Constants.ONE_NXT, ALICE.getBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_NXT, ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_NXT, BOB.getBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_NXT, BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 11 * Constants.ONE_NXT), CHUCK.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 11 * Constants.ONE_NXT), CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_NXT, DAVE.getBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_NXT, DAVE.getUnconfirmedBalanceDiff());

        Assert.assertNotNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNotNull(BOB_RECIPIENT.getAccount());
        Assert.assertNotNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNotNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(0, ALICE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, ALICE_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, BOB_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, BOB_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, CHUCK_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, CHUCK_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, DAVE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, DAVE_RECIPIENT.getUnconfirmedBalanceDiff());

        Assert.assertEquals(47 * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals(47 * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void cancelAfterVerifyChuck() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        generateBlock();
        register(shufflingFullHash, CHUCK);
        generateBlock();
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);
        generateBlock();

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        for (int i = 0; i < 5; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        process(shufflingId, CHUCK, CHUCK_RECIPIENT);

        for (int i = 0; i < 5; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.VERIFICATION.getCode(), getShufflingResponse.get("stage"));
        String shufflingStateHash = (String)getShufflingResponse.get("shufflingStateHash");
        cancel(shufflingId, CHUCK, shufflingStateHash, 0);
        generateBlock();
        getShufflingResponse = getShuffling(shufflingId);
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);

        for (int i = 0; i < 15; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);

        Assert.assertEquals(-22 * Constants.ONE_NXT, ALICE.getBalanceDiff());
        Assert.assertEquals(-22 * Constants.ONE_NXT, ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-22 * Constants.ONE_NXT, BOB.getBalanceDiff());
        Assert.assertEquals(-22 * Constants.ONE_NXT, BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 21 * Constants.ONE_NXT), CHUCK.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 21 * Constants.ONE_NXT), CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_NXT, DAVE.getBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_NXT, DAVE.getUnconfirmedBalanceDiff());

        Assert.assertNotNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNotNull(BOB_RECIPIENT.getAccount());
        Assert.assertNotNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNotNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(0, ALICE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, ALICE_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, BOB_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, BOB_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, CHUCK_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, CHUCK_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, DAVE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, DAVE_RECIPIENT.getUnconfirmedBalanceDiff());

        Assert.assertEquals(77 * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals(77 * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void cancelAfterVerifyChuckRestart() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        generateBlock();
        register(shufflingFullHash, CHUCK);
        generateBlock();
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);
        stopShuffler(BOB, shufflingFullHash);
        generateBlock();

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        for (int i = 0; i < 5; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        generateBlock();
        process(shufflingId, CHUCK, CHUCK_RECIPIENT);

        for (int i = 0; i < 5; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        stopShuffler(ALICE, shufflingFullHash);
        stopShuffler(DAVE, shufflingFullHash);

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.VERIFICATION.getCode(), getShufflingResponse.get("stage"));
        String shufflingStateHash = (String)getShufflingResponse.get("shufflingStateHash");
        cancel(shufflingId, CHUCK, shufflingStateHash, 0);
        generateBlock();
        getShufflingResponse = getShuffling(shufflingId);
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);

        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);

        for (int i = 0; i < 5; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);

        for (int i = 0; i < 15; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);

        Assert.assertEquals(-22 * Constants.ONE_NXT, ALICE.getBalanceDiff());
        Assert.assertEquals(-22 * Constants.ONE_NXT, ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-22 * Constants.ONE_NXT, BOB.getBalanceDiff());
        Assert.assertEquals(-22 * Constants.ONE_NXT, BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 21 * Constants.ONE_NXT), CHUCK.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 21 * Constants.ONE_NXT), CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_NXT, DAVE.getBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_NXT, DAVE.getUnconfirmedBalanceDiff());

        Assert.assertNotNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNotNull(BOB_RECIPIENT.getAccount());
        Assert.assertNotNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNotNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(0, ALICE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, ALICE_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, BOB_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, BOB_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, CHUCK_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, CHUCK_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, DAVE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, DAVE_RECIPIENT.getUnconfirmedBalanceDiff());

        Assert.assertEquals(77 * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals(77 * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void cancelAfterVerifyChuckInvalidKeys() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        generateBlock();
        register(shufflingFullHash, CHUCK);
        generateBlock();
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);
        generateBlock();

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        for (int i = 0; i < 5; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        process(shufflingId, CHUCK, CHUCK_RECIPIENT);

        for (int i = 0; i < 5; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.VERIFICATION.getCode(), getShufflingResponse.get("stage"));
        String shufflingStateHash = (String)getShufflingResponse.get("shufflingStateHash");

        JSONObject cancelResponse = cancel(shufflingId, CHUCK, shufflingStateHash, 0, false);
        JSONObject transactionJSON = (JSONObject)cancelResponse.get("transactionJSON");
        JSONArray keySeeds = (JSONArray)((JSONObject)transactionJSON.get("attachment")).get("keySeeds");
        String s = (String)keySeeds.get(0);
        keySeeds.set(0, "0000000000" + s.substring(10));
        broadcast(transactionJSON, CHUCK);
        generateBlock();

        getShufflingResponse = getShuffling(shufflingId);
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);

        for (int i = 0; i < 15; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);

        Assert.assertEquals(-22 * Constants.ONE_NXT, ALICE.getBalanceDiff());
        Assert.assertEquals(-22 * Constants.ONE_NXT, ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-22 * Constants.ONE_NXT, BOB.getBalanceDiff());
        Assert.assertEquals(-22 * Constants.ONE_NXT, BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 21 * Constants.ONE_NXT), CHUCK.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 21 * Constants.ONE_NXT), CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_NXT, DAVE.getBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_NXT, DAVE.getUnconfirmedBalanceDiff());

        Assert.assertNotNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNotNull(BOB_RECIPIENT.getAccount());
        Assert.assertNotNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNotNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(0, ALICE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, ALICE_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, BOB_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, BOB_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, CHUCK_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, CHUCK_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, DAVE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, DAVE_RECIPIENT.getUnconfirmedBalanceDiff());

        Assert.assertEquals(77 * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals(77 * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void cancelAfterVerifyChuckInvalidKeysAlice() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        generateBlock();
        register(shufflingFullHash, CHUCK);
        generateBlock();
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);
        generateBlock();

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));
        process(shufflingId, ALICE, ALICE_RECIPIENT);
        for (int i = 0; i < 2; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }
        process(shufflingId, CHUCK, CHUCK_RECIPIENT);
        for (int i = 0; i < 5; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.VERIFICATION.getCode(), getShufflingResponse.get("stage"));
        String shufflingStateHash = (String)getShufflingResponse.get("shufflingStateHash");
        verify(shufflingId, ALICE, shufflingStateHash);
        generateBlock();
        cancel(shufflingId, CHUCK, shufflingStateHash, 0);
        generateBlock();
        getShufflingResponse = getShuffling(shufflingId);
        shufflingStateHash = (String)getShufflingResponse.get("shufflingStateHash");
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);
        JSONObject cancelResponse = cancel(shufflingId, ALICE, shufflingStateHash, CHUCK.getId(), false);
        JSONObject transactionJSON = (JSONObject)cancelResponse.get("transactionJSON");
        JSONArray keySeeds = (JSONArray)((JSONObject)transactionJSON.get("attachment")).get("keySeeds");
        String s = (String)keySeeds.get(0);
        keySeeds.set(0, "0000000000" + s.substring(10));
        broadcast(transactionJSON, ALICE);
        for (int i = 0; i < 15; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }
        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(ALICE.getId()), shufflingAssignee);

        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 22 * Constants.ONE_NXT), ALICE.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 22 * Constants.ONE_NXT), ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-22 * Constants.ONE_NXT, BOB.getBalanceDiff());
        Assert.assertEquals(-22 * Constants.ONE_NXT, BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-21 * Constants.ONE_NXT, CHUCK.getBalanceDiff());
        Assert.assertEquals(-21 * Constants.ONE_NXT, CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_NXT, DAVE.getBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_NXT, DAVE.getUnconfirmedBalanceDiff());

        Assert.assertNotNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNotNull(BOB_RECIPIENT.getAccount());
        Assert.assertNotNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNotNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(0, ALICE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, ALICE_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, BOB_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, BOB_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, CHUCK_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, CHUCK_RECIPIENT.getUnconfirmedBalanceDiff());
        Assert.assertEquals(0, DAVE_RECIPIENT.getBalanceDiff());
        Assert.assertEquals(0, DAVE_RECIPIENT.getUnconfirmedBalanceDiff());

        Assert.assertEquals(77 * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals(77 * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void badProcessDataAlice() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        startShuffler(CHUCK, CHUCK_RECIPIENT, shufflingFullHash);
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);
        generateBlock();

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject processResponse = process(shufflingId, ALICE, ALICE_RECIPIENT, false);
        JSONObject transactionJSON = (JSONObject)processResponse.get("transactionJSON");
        JSONArray data = (JSONArray)((JSONObject)transactionJSON.get("attachment")).get("data");
        String s = (String)data.get(0);
        data.set(0, "8080808080" + s.substring(10));
        broadcast(transactionJSON, ALICE);
        for (int i = 0; i < 15; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }
        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(ALICE.getId()), shufflingAssignee);

        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 11 * Constants.ONE_NXT), ALICE.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 11 * Constants.ONE_NXT), ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_NXT, BOB.getBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_NXT, BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-1 * Constants.ONE_NXT, CHUCK.getBalanceDiff());
        Assert.assertEquals(-1 * Constants.ONE_NXT, CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-1 * Constants.ONE_NXT, DAVE.getBalanceDiff());
        Assert.assertEquals(-1 * Constants.ONE_NXT, DAVE.getUnconfirmedBalanceDiff());

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());
        Assert.assertNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(24 * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals(24 * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void modifiedProcessDataBob() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        register(shufflingFullHash, BOB);
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        startShuffler(CHUCK, CHUCK_RECIPIENT, shufflingFullHash);
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);
        for (int i = 0; i < 3; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject processResponse = process(shufflingId, BOB, BOB_RECIPIENT, false);
        JSONObject transactionJSON = (JSONObject)processResponse.get("transactionJSON");
        JSONArray data = (JSONArray)((JSONObject)transactionJSON.get("attachment")).get("data");
        String s = (String)data.get(0);
        data.set(0, "8080808080" + s.substring(10));
        broadcast(transactionJSON, BOB);
        generateBlock();
        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        for (int i = 0; i < 5; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.BLAME.getCode(), getShufflingResponse.get("stage"));
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);
        String shufflingStateHash = (String)getShufflingResponse.get("shufflingStateHash");

        JSONObject cancelResponse = cancel(shufflingId, BOB, shufflingStateHash, CHUCK.getId());
        boolean bobCancelFailed = cancelResponse.get("error") != null; // if he happened to modify his own piece

        for (int i = 0; i < 15; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(BOB.getId()), shufflingAssignee);

        Assert.assertEquals(-21 * Constants.ONE_NXT, ALICE.getBalanceDiff());
        Assert.assertEquals(-21 * Constants.ONE_NXT, ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + (bobCancelFailed ? 11 : 21) * Constants.ONE_NXT), BOB.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + (bobCancelFailed ? 11 : 21) * Constants.ONE_NXT), BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_NXT, CHUCK.getBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_NXT, CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-1 * Constants.ONE_NXT, DAVE.getBalanceDiff());
        Assert.assertEquals(-1 * Constants.ONE_NXT, DAVE.getUnconfirmedBalanceDiff());

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());
        Assert.assertNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals((bobCancelFailed ? 44 : 54) * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals((bobCancelFailed ? 44 : 54) * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void duplicateProcessDataBob() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        register(shufflingFullHash, BOB);
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        startShuffler(CHUCK, CHUCK_RECIPIENT, shufflingFullHash);
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);
        for (int i = 0; i < 3; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject processResponse = process(shufflingId, BOB, BOB_RECIPIENT, false);
        JSONObject transactionJSON = (JSONObject)processResponse.get("transactionJSON");
        JSONArray data = (JSONArray)((JSONObject)transactionJSON.get("attachment")).get("data");

        byte[] nonce = Convert.toBytes(Convert.parseUnsignedLong(shufflingId));
        byte[] bytesToEncrypt = BOB_RECIPIENT.getPublicKey();
        byte[] nonce2 = Convert.toBytes(Convert.parseUnsignedLong(shufflingId) + 1);
        bytesToEncrypt = AnonymouslyEncryptedData.encrypt(bytesToEncrypt, BOB.getSecretPhrase(), DAVE.getPublicKey(), nonce).getBytes();
        byte[] bobBytes = AnonymouslyEncryptedData.encrypt(bytesToEncrypt, BOB.getSecretPhrase(), CHUCK.getPublicKey(), nonce).getBytes();
        byte[] modifiedBytes = AnonymouslyEncryptedData.encrypt(bytesToEncrypt, BOB.getSecretPhrase(), CHUCK.getPublicKey(), nonce2).getBytes();

        if (Convert.byteArrayComparator.compare(bobBytes, modifiedBytes) < 0) {
            data.set(0, Convert.toHexString(bobBytes));
            data.set(1, Convert.toHexString(modifiedBytes));
        } else {
            data.set(0, Convert.toHexString(modifiedBytes));
            data.set(1, Convert.toHexString(bobBytes));
        }

        broadcast(transactionJSON, BOB);
        generateBlock();
        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        for (int i = 0; i < 10; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.BLAME.getCode(), getShufflingResponse.get("stage"));
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);
        String shufflingStateHash = (String)getShufflingResponse.get("shufflingStateHash");

        cancel(shufflingId, BOB, shufflingStateHash, CHUCK.getId());

        for (int i = 0; i < 15; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(BOB.getId()), shufflingAssignee);

        Assert.assertEquals(-21 * Constants.ONE_NXT, ALICE.getBalanceDiff());
        Assert.assertEquals(-21 * Constants.ONE_NXT, ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 21 * Constants.ONE_NXT), BOB.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 21 * Constants.ONE_NXT), BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_NXT, CHUCK.getBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_NXT, CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-1 * Constants.ONE_NXT, DAVE.getBalanceDiff());
        Assert.assertEquals(-1 * Constants.ONE_NXT, DAVE.getUnconfirmedBalanceDiff());

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());
        Assert.assertNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(54 * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals(54 * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void modifiedProcessDataChuck() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        generateBlock();
        register(shufflingFullHash, CHUCK);
        generateBlock();
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);
        for (int i = 0; i < 3; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject processResponse = process(shufflingId, CHUCK, CHUCK_RECIPIENT, false);
        JSONObject transactionJSON = (JSONObject)processResponse.get("transactionJSON");
        JSONArray data = (JSONArray)((JSONObject)transactionJSON.get("attachment")).get("data");
        String s = (String)data.get(0);
        data.set(0, "8080808080" + s.substring(10));
        broadcast(transactionJSON, CHUCK);
        generateBlock();

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        for (int i = 0; i < 5; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.BLAME.getCode(), getShufflingResponse.get("stage"));
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(DAVE.getId()), shufflingAssignee);
        String shufflingStateHash = (String)getShufflingResponse.get("shufflingStateHash");

        JSONObject cancelResponse = cancel(shufflingId, CHUCK, shufflingStateHash, DAVE.getId());
        boolean chuckCancelFailed = cancelResponse.get("error") != null; // if he happened to modify his own piece

        for (int i = 0; i < 15; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);

        Assert.assertEquals(-21 * Constants.ONE_NXT, ALICE.getBalanceDiff());
        Assert.assertEquals(-21 * Constants.ONE_NXT, ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-21 * Constants.ONE_NXT, BOB.getBalanceDiff());
        Assert.assertEquals(-21 * Constants.ONE_NXT, BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + (chuckCancelFailed ? 11 : 21) * Constants.ONE_NXT), CHUCK.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + (chuckCancelFailed ? 11 : 21) * Constants.ONE_NXT), CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_NXT, DAVE.getBalanceDiff());
        Assert.assertEquals(-12 * Constants.ONE_NXT, DAVE.getUnconfirmedBalanceDiff());

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());
        Assert.assertNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals((chuckCancelFailed ? 65 : 75) * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals((chuckCancelFailed ? 65 : 75) * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void modifiedRecipientKeysDave() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        startShuffler(CHUCK, CHUCK_RECIPIENT, shufflingFullHash);
        generateBlock();
        register(shufflingFullHash, DAVE);

        for (int i = 0; i < 5; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject processResponse = process(shufflingId, DAVE, DAVE_RECIPIENT, false);
        JSONObject transactionJSON = (JSONObject)processResponse.get("transactionJSON");
        JSONArray data = (JSONArray)((JSONObject)transactionJSON.get("attachment")).get("recipientPublicKeys");
        String s = (String)data.get(0);
        if (!s.equals(DAVE_RECIPIENT.getPublicKeyStr())) {
            data.set(0, "0000000000" + s.substring(10));
        } else {
            s = (String)data.get(1);
            data.set(1, "0000000000" + s.substring(10));
        }
        broadcast(transactionJSON, DAVE);

        for (int i = 0; i < 15; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(DAVE.getId()), shufflingAssignee);

        Assert.assertEquals(-65 * Constants.ONE_NXT, ALICE.getBalanceDiff() + BOB.getBalanceDiff() + CHUCK.getBalanceDiff());
        Assert.assertEquals(ALICE.getBalanceDiff(), ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(BOB.getBalanceDiff(), BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(CHUCK.getBalanceDiff(), CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 12 * Constants.ONE_NXT), DAVE.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 12 * Constants.ONE_NXT), DAVE.getUnconfirmedBalanceDiff());

        Assert.assertTrue(ALICE_RECIPIENT.getAccount() == null || ALICE_RECIPIENT.getBalanceDiff() == 0);
        Assert.assertTrue(ALICE_RECIPIENT.getAccount() == null || ALICE_RECIPIENT.getUnconfirmedBalanceDiff() == 0);
        Assert.assertTrue(BOB_RECIPIENT.getAccount() == null || BOB_RECIPIENT.getBalanceDiff() == 0);
        Assert.assertTrue(BOB_RECIPIENT.getAccount() == null || BOB_RECIPIENT.getUnconfirmedBalanceDiff() == 0);
        Assert.assertTrue(CHUCK_RECIPIENT.getAccount() == null || CHUCK_RECIPIENT.getBalanceDiff() == 0);
        Assert.assertTrue(CHUCK_RECIPIENT.getAccount() == null || CHUCK_RECIPIENT.getUnconfirmedBalanceDiff() == 0);
        Assert.assertTrue(DAVE_RECIPIENT.getAccount() == null || DAVE_RECIPIENT.getBalanceDiff() == 0);
        Assert.assertTrue(DAVE_RECIPIENT.getAccount() == null || DAVE_RECIPIENT.getUnconfirmedBalanceDiff() == 0);

        Assert.assertEquals(77 * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals(77 * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void duplicateRecipientKeysDave() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        startShuffler(CHUCK, CHUCK_RECIPIENT, shufflingFullHash);
        generateBlock();
        register(shufflingFullHash, DAVE);

        for (int i = 0; i < 5; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject processResponse = process(shufflingId, DAVE, DAVE_RECIPIENT, false);
        JSONObject transactionJSON = (JSONObject)processResponse.get("transactionJSON");
        JSONArray data = (JSONArray)((JSONObject)transactionJSON.get("attachment")).get("recipientPublicKeys");
        String s = (String)data.get(0);
        data.set(1, s);
        JSONObject broadcastResponse = broadcast(transactionJSON, DAVE);
        Assert.assertTrue(broadcastResponse.get("error") != null);

        for (int i = 0; i < 15; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(DAVE.getId()), shufflingAssignee);

        Assert.assertEquals(-11 * Constants.ONE_NXT, ALICE.getBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_NXT, ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_NXT, BOB.getBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_NXT, BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_NXT, CHUCK.getBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_NXT, CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + Constants.ONE_NXT), DAVE.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + Constants.ONE_NXT), DAVE.getUnconfirmedBalanceDiff());

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());
        Assert.assertNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(34 * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals(34 * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void duplicateProcessDataChuck() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        generateBlock();
        register(shufflingFullHash, CHUCK);
        generateBlock();
        startShuffler(DAVE, DAVE_RECIPIENT, shufflingFullHash);
        for (int i = 0; i < 3; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        JSONObject processResponse = process(shufflingId, CHUCK, CHUCK_RECIPIENT, false);
        JSONObject transactionJSON = (JSONObject)processResponse.get("transactionJSON");
        JSONArray data = (JSONArray)((JSONObject)transactionJSON.get("attachment")).get("data");
        String s = (String)data.get(0);
        data.set(1, s);
        JSONObject broadcastResponse = broadcast(transactionJSON, CHUCK);
        Assert.assertTrue(broadcastResponse.get("error") != null);

        for (int i = 0; i < 15; i++) {
            generateBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(CHUCK.getId()), shufflingAssignee);

        Assert.assertEquals(-11 * Constants.ONE_NXT, ALICE.getBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_NXT, ALICE.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_NXT, BOB.getBalanceDiff());
        Assert.assertEquals(-11 * Constants.ONE_NXT, BOB.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + Constants.ONE_NXT), CHUCK.getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + Constants.ONE_NXT), CHUCK.getUnconfirmedBalanceDiff());
        Assert.assertEquals(-1 * Constants.ONE_NXT, DAVE.getBalanceDiff());
        Assert.assertEquals(-1 * Constants.ONE_NXT, DAVE.getUnconfirmedBalanceDiff());

        Assert.assertNull(ALICE_RECIPIENT.getAccount());
        Assert.assertNull(BOB_RECIPIENT.getAccount());
        Assert.assertNull(CHUCK_RECIPIENT.getAccount());
        Assert.assertNull(DAVE_RECIPIENT.getAccount());

        Assert.assertEquals(24 * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getBalanceDiff());
        Assert.assertEquals(24 * Constants.ONE_NXT + Constants.SHUFFLING_DEPOSIT_NQT, FORGY.getUnconfirmedBalanceDiff());

    }

    @Test
    public void duplicateRecipientsBobChuck() {
        JSONObject shufflingCreate = create(ALICE);
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();
        startShuffler(ALICE, ALICE_RECIPIENT, shufflingFullHash);
        generateBlock();
        startShuffler(BOB, BOB_RECIPIENT, shufflingFullHash);
        generateBlock();
        JSONObject startShufflerResponse = startShuffler(CHUCK, BOB_RECIPIENT, shufflingFullHash);
        Assert.assertTrue(((String)startShufflerResponse.get("errorDescription")).startsWith("Incorrect \"recipientPublicKey\""));
    }

    @Test
    public void maxShufflingSize() {
        int n = Constants.MAX_NUMBER_OF_SHUFFLING_PARTICIPANTS;
        Tester[] participants = new Tester[n];
        Tester[] recipients = new Tester[n];
        participants[0] = ALICE;
        recipients[0] = ALICE_RECIPIENT;
        for (int i = 1; i < n; i++) {
            participants[i] = new Tester("tester " + i);
            ShufflingUtil.sendMoney(ALICE, participants[i], 100);
            recipients[i] = new Tester("recipient " + i);
        }
        generateBlock();
        JSONObject shufflingCreate = create(ALICE, n);
        String shufflingId = (String)shufflingCreate.get("transaction");
        String shufflingFullHash = (String)shufflingCreate.get("fullHash");
        generateBlock();

        for (int i = 0; i < n - 1; i++) {
            startShuffler(participants[i], recipients[i], shufflingFullHash);
        }
        generateBlock();
        register(shufflingFullHash, participants[n - 1]);
        generateBlock();
        JSONObject getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.PROCESSING.getCode(), getShufflingResponse.get("stage"));

        int maxBlockJsonSize = 0;
        int maxBlockPayload = 0;
        int maxProcessTransactionFullSize = 0;
        int maxCancelTransactionFullSize = 0;
        int maxProcessTransactionJsonSize = 0;
        int maxCancelTransactionJsonSize = 0;
        for (int i = 0; i < n + 5; i++) {
            generateBlock();
            Block block = Nxt.getBlockchain().getLastBlock();
            int blockJsonSize = block.getJSONObject().toJSONString().length();
            Logger.logMessage("Block size " + blockJsonSize + " block payload " + block.getPayloadLength() + " tx count " + block.getTransactions().size());
            if (blockJsonSize > maxBlockJsonSize) {
                maxBlockJsonSize = blockJsonSize;
            }
            if (block.getPayloadLength() > maxBlockPayload) {
                maxBlockPayload = block.getPayloadLength();
            }
            for (Transaction transaction : block.getTransactions()) {
                if (transaction.getType() == ShufflingTransaction.SHUFFLING_PROCESSING) {
                    if (transaction.getFullSize() > maxProcessTransactionFullSize) {
                        maxProcessTransactionFullSize = transaction.getFullSize();
                    }
                    if (transaction.getJSONObject().toString().length() > maxProcessTransactionJsonSize) {
                        maxProcessTransactionJsonSize = transaction.getJSONObject().toString().length();
                    }
                }
                if (transaction.getType() == ShufflingTransaction.SHUFFLING_CANCELLATION) {
                    if (transaction.getFullSize() > maxProcessTransactionFullSize) {
                        maxCancelTransactionFullSize = transaction.getFullSize();
                    }
                    if (transaction.getJSONObject().toString().length() > maxCancelTransactionJsonSize) {
                        maxCancelTransactionJsonSize = transaction.getJSONObject().toString().length();
                    }
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        JSONObject processResponse = process(shufflingId, participants[n - 1], recipients[n - 1], false);
        JSONObject transactionJSON = (JSONObject)processResponse.get("transactionJSON");
        JSONArray data = (JSONArray)((JSONObject)transactionJSON.get("attachment")).get("recipientPublicKeys");
        String s = (String)data.get(0);
        if (!s.equals(recipients[n - 1].getPublicKeyStr())) {
            data.set(0, "0000000000" + s.substring(10));
        } else {
            s = (String)data.get(1);
            data.set(1, "0000000000" + s.substring(10));
        }
        broadcast(transactionJSON, participants[n - 1]);
        generateBlock();

        for (int i = 0; i < n + 20; i++) {
            generateBlock();
            Block block = Nxt.getBlockchain().getLastBlock();
            int blockJsonSize = block.getJSONObject().toJSONString().length();
            Logger.logMessage("Block size " + blockJsonSize + " block payload " + block.getPayloadLength() + " tx count " + block.getTransactions().size());
            if (blockJsonSize > maxBlockJsonSize) {
                maxBlockJsonSize = blockJsonSize;
            }
            if (block.getPayloadLength() > maxBlockPayload) {
                maxBlockPayload = block.getPayloadLength();
            }
            for (Transaction transaction : block.getTransactions()) {
                if (transaction.getType() == ShufflingTransaction.SHUFFLING_PROCESSING) {
                    if (transaction.getFullSize() > maxProcessTransactionFullSize) {
                        maxProcessTransactionFullSize = transaction.getFullSize();
                    }
                    if (transaction.getJSONObject().toString().length() > maxProcessTransactionJsonSize) {
                        maxProcessTransactionJsonSize = transaction.getJSONObject().toString().length();
                    }
                }
                if (transaction.getType() == ShufflingTransaction.SHUFFLING_CANCELLATION) {
                    if (transaction.getFullSize() > maxProcessTransactionFullSize) {
                        maxCancelTransactionFullSize = transaction.getFullSize();
                    }
                    if (transaction.getJSONObject().toString().length() > maxCancelTransactionJsonSize) {
                        maxCancelTransactionJsonSize = transaction.getJSONObject().toString().length();
                    }
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
        }

        getShufflingResponse = getShuffling(shufflingId);
        Assert.assertEquals((long) Shuffling.Stage.CANCELLED.getCode(), getShufflingResponse.get("stage"));
        String shufflingAssignee = (String) getShufflingResponse.get("assignee");
        Assert.assertEquals(Long.toUnsignedString(participants[n - 1].getId()), shufflingAssignee);

        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 12 * Constants.ONE_NXT) + 100 * Constants.ONE_NXT, participants[n - 1].getBalanceDiff());
        Assert.assertEquals(-(Constants.SHUFFLING_DEPOSIT_NQT + 12 * Constants.ONE_NXT) + 100 * Constants.ONE_NXT, participants[n - 1].getUnconfirmedBalanceDiff());
        Logger.logMessage("Max block json size " + maxBlockJsonSize);
        Logger.logMessage("Max block payload " + maxBlockPayload);
        Logger.logMessage("Max process transaction full size " + maxProcessTransactionFullSize);
        Logger.logMessage("Max cancel transaction full size " + maxCancelTransactionFullSize);
        Logger.logMessage("Max process transaction json size " + maxProcessTransactionJsonSize);
        Logger.logMessage("Max cancel transaction json size " + maxCancelTransactionJsonSize);
    }

}
