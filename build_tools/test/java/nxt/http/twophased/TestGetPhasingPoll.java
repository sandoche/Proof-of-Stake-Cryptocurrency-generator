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
import nxt.http.APICall;
import nxt.util.Logger;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class TestGetPhasingPoll extends BlockchainTest {

    @Test
    public void transactionVotes() {

        APICall apiCall = new TestCreateTwoPhased.TwoPhasedMoneyTransferBuilder()
                .quorum(1)
                .build();
        JSONObject transactionJSON = TestCreateTwoPhased.issueCreateTwoPhased(apiCall, false);
        String fullHash = (String) transactionJSON.get("fullHash");
        String transactionId = (String) transactionJSON.get("transaction");

        generateBlock();

        long fee = Constants.ONE_NXT;
        apiCall = new APICall.Builder("approveTransaction")
                .param("secretPhrase", CHUCK.getSecretPhrase())
                .param("transactionFullHash", fullHash)
                .param("feeNQT", fee)
                .build();
        JSONObject response = apiCall.invoke();
        Logger.logMessage("approveTransactionResponse:" + response.toJSONString());

        generateBlock();

        apiCall = new APICall.Builder("getPhasingPoll")
                .param("transaction", transactionId)
                .param("countVotes", "true")
                .build();
        response = apiCall.invoke();
        Logger.logMessage("getPhasingPollResponse:" + response.toJSONString());

        Assert.assertNull(response.get("errorCode"));
        Assert.assertEquals(1, Integer.parseInt((String) response.get("result")));
    }

}