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
import nxt.http.APICall;
import nxt.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class TestGetAccountPhasedTransactions extends BlockchainTest {

    static APICall phasedTransactionsApiCall(long id) {
        return new APICall.Builder("getAccountPhasedTransactions")
                .param("account", Long.toUnsignedString(id))
                .param("firstIndex", 0)
                .param("lastIndex", 10)
                .build();
    }

    static APICall phasedTransactionsApiCall() {
        return phasedTransactionsApiCall(ALICE.getId());
    }

    @Test
    public void simpleOutgoingLookup() {
        APICall apiCall = new TestCreateTwoPhased.TwoPhasedMoneyTransferBuilder().build();
        JSONObject transactionJSON = TestCreateTwoPhased.issueCreateTwoPhased(apiCall, false);
        generateBlock();

        JSONObject response = phasedTransactionsApiCall().invoke();
        Logger.logMessage("getAccountPhasedTransactionsResponse:" + response.toJSONString());
        JSONArray transactionsJson = (JSONArray) response.get("transactions");
        Assert.assertTrue(TwoPhasedSuite.searchForTransactionId(transactionsJson, (String) transactionJSON.get("transaction")));

        response = phasedTransactionsApiCall(CHUCK.getId()).invoke();
        Logger.logMessage("getAccountPhasedTransactionsResponse:" + response.toJSONString());
        transactionsJson = (JSONArray) response.get("transactions");
        Assert.assertFalse(TwoPhasedSuite.searchForTransactionId(transactionsJson, (String) transactionJSON.get("transaction")));
    }

    @Test
    public void simpleIngoingLookup() {
        APICall apiCall = new TestCreateTwoPhased.TwoPhasedMoneyTransferBuilder().build();
        JSONObject transactionJSON = TestCreateTwoPhased.issueCreateTwoPhased(apiCall, false);
        generateBlock();

        JSONObject response = phasedTransactionsApiCall(BOB.getId()).invoke();
        Logger.logMessage("getAccountPhasedTransactionsResponse:" + response.toJSONString());
        JSONArray transactionsJson = (JSONArray) response.get("transactions");
        Assert.assertTrue(TwoPhasedSuite.searchForTransactionId(transactionsJson, (String) transactionJSON.get("transaction")));

        response = phasedTransactionsApiCall(CHUCK.getId()).invoke();
        Logger.logMessage("getAccountPhasedTransactionsResponse:" + response.toJSONString());
        transactionsJson = (JSONArray) response.get("transactions");
        Assert.assertFalse(TwoPhasedSuite.searchForTransactionId(transactionsJson, (String) transactionJSON.get("transaction")));
    }

    @Test
    public void multiple() {
        JSONObject response = phasedTransactionsApiCall().invoke();
        Logger.logMessage("getAccountPhasedTransactionsResponse:" + response.toJSONString());
        JSONArray transactionsJson = (JSONArray) response.get("transactions");
        int transactionsSize0 = transactionsJson.size();

        APICall apiCall = new TestCreateTwoPhased.TwoPhasedMoneyTransferBuilder().build();
        JSONObject transactionJSON1 = TestCreateTwoPhased.issueCreateTwoPhased(apiCall, false);
        JSONObject transactionJSON2 = TestCreateTwoPhased.issueCreateTwoPhased(apiCall, false);
        generateBlock();

        response = phasedTransactionsApiCall().invoke();
        Logger.logMessage("getAccountPhasedTransactionsResponse:" + response.toJSONString());
        transactionsJson = (JSONArray) response.get("transactions");

        int transactionsSize = transactionsJson.size();

        Assert.assertTrue(transactionsSize - transactionsSize0 == 2);
        Assert.assertTrue(TwoPhasedSuite.searchForTransactionId(transactionsJson, (String) transactionJSON1.get("transaction")));
        Assert.assertTrue(TwoPhasedSuite.searchForTransactionId(transactionsJson, (String) transactionJSON2.get("transaction")));
    }

    @Test
    public void sorting() {
        for (int i = 0; i < 15; i++) {
            APICall apiCall = new TestCreateTwoPhased.TwoPhasedMoneyTransferBuilder().build();
            TestCreateTwoPhased.issueCreateTwoPhased(apiCall, false);
        }

        JSONObject response = phasedTransactionsApiCall().invoke();
        Logger.logMessage("getAccountPhasedTransactionsResponse:" + response.toJSONString());
        JSONArray transactionsJson = (JSONArray) response.get("transactions");

        //sorting check
        int prevHeight = Integer.MAX_VALUE;
        for (Object transactionsJsonObj : transactionsJson) {
            JSONObject transactionObject = (JSONObject) transactionsJsonObj;
            int height = ((Long) transactionObject.get("height")).intValue();
            Assert.assertTrue(height <= prevHeight);
            prevHeight = height;
        }
    }
}

