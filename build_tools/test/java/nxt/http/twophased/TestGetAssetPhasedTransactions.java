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
import nxt.VoteWeighting;
import nxt.http.APICall;
import nxt.util.Convert;
import nxt.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class TestGetAssetPhasedTransactions extends BlockchainTest {
    private static String asset = "18055555436405339905";

    static APICall phasedTransactionsApiCall() {
        return new APICall.Builder("getAssetPhasedTransactions")
                .param("asset", asset)
                .param("firstIndex", 0)
                .param("lastIndex", 10)
                .build();
    }

    private APICall byAssetApiCall() {
        return new TestCreateTwoPhased.TwoPhasedMoneyTransferBuilder()
                .votingModel(VoteWeighting.VotingModel.ASSET.getCode())
                .holding(Convert.parseUnsignedLong(asset))
                .minBalance(1, VoteWeighting.MinBalanceModel.ASSET.getCode())
                .fee(21 * Constants.ONE_NXT)
                .build();
    }


    @Test
    public void simpleTransactionLookup() {
        JSONObject transactionJSON = TestCreateTwoPhased.issueCreateTwoPhased(byAssetApiCall(), false);

        JSONObject response = phasedTransactionsApiCall().invoke();
        Logger.logMessage("getAssetPhasedTransactionsResponse:" + response.toJSONString());
        JSONArray transactionsJson = (JSONArray) response.get("transactions");
        Assert.assertTrue(TwoPhasedSuite.searchForTransactionId(transactionsJson, (String) transactionJSON.get("transaction")));
    }

    @Test
    public void sorting() {
        for (int i = 0; i < 15; i++) {
            TestCreateTwoPhased.issueCreateTwoPhased(byAssetApiCall(), false);
        }

        JSONObject response = phasedTransactionsApiCall().invoke();
        Logger.logMessage("getAssetPhasedTransactionsResponse:" + response.toJSONString());
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
