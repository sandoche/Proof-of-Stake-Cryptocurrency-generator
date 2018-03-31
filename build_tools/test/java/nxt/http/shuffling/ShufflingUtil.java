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

import nxt.Constants;
import nxt.HoldingType;
import nxt.Tester;
import nxt.http.APICall;
import nxt.util.Convert;
import nxt.util.Logger;
import org.json.simple.JSONObject;

class ShufflingUtil {

    static final Tester ALICE_RECIPIENT = new Tester("oiketrdgfxyjqhwds");
    static final Tester BOB_RECIPIENT = new Tester("5ehtrd9oijnkter");
    static final Tester CHUCK_RECIPIENT = new Tester("sdfxbejytdgfqrwefsrd");
    static final Tester DAVE_RECIPIENT = new Tester("gh-=e49rsiufzn4^");

    static final long defaultShufflingAmount = 1500000000;
    static final long defaultHoldingShufflingAmount = 40000;
    static final long shufflingAsset = 3320741880585366286L;
    static final long shufflingCurrency = -5643814336689018857L;

    static JSONObject create(Tester creator) {
        return create(creator, 4);
    }

    static JSONObject create(Tester creator, int participantCount) {
        APICall apiCall = new APICall.Builder("shufflingCreate").
                secretPhrase(creator.getSecretPhrase()).
                feeNQT(Constants.ONE_NXT).
                param("amount", String.valueOf(defaultShufflingAmount)).
                param("participantCount", String.valueOf(participantCount)).
                param("registrationPeriod", 10).
                build();
        JSONObject response = apiCall.invoke();
        Logger.logMessage("shufflingCreateResponse: " + response.toJSONString());
        return response;
    }

    static JSONObject createAssetShuffling(Tester creator) {
        APICall apiCall = new APICall.Builder("shufflingCreate").
                secretPhrase(creator.getSecretPhrase()).
                feeNQT(Constants.ONE_NXT).
                param("amount", String.valueOf(defaultHoldingShufflingAmount)).
                param("participantCount", "4").
                param("registrationPeriod", 10).
                param("holding", Long.toUnsignedString(shufflingAsset)).
                param("holdingType", String.valueOf(HoldingType.ASSET.getCode())).
                build();
        JSONObject response = apiCall.invoke();
        Logger.logMessage("shufflingCreateResponse: " + response.toJSONString());
        return response;
    }

    static JSONObject createCurrencyShuffling(Tester creator) {
        APICall apiCall = new APICall.Builder("shufflingCreate").
                secretPhrase(creator.getSecretPhrase()).
                feeNQT(Constants.ONE_NXT).
                param("amount", String.valueOf(defaultHoldingShufflingAmount)).
                param("participantCount", "4").
                param("registrationPeriod", 10).
                param("holding", Long.toUnsignedString(shufflingCurrency)).
                param("holdingType", String.valueOf(HoldingType.CURRENCY.getCode())).
                build();
        JSONObject response = apiCall.invoke();
        Logger.logMessage("shufflingCreateResponse: " + response.toJSONString());
        return response;
    }

    static JSONObject register(String shufflingFullHash, Tester tester) {
        APICall apiCall = new APICall.Builder("shufflingRegister").
                secretPhrase(tester.getSecretPhrase()).
                feeNQT(Constants.ONE_NXT).
                param("shufflingFullHash", shufflingFullHash).
                build();
        JSONObject response = apiCall.invoke();
        Logger.logMessage("shufflingRegisterResponse: " + response.toJSONString());
        return response;
    }

    static JSONObject getShuffling(String shufflingId) {
        APICall apiCall = new APICall.Builder("getShuffling").
                param("shuffling", shufflingId).
                build();
        JSONObject getShufflingResponse = apiCall.invoke();
        Logger.logMessage("getShufflingResponse: " + getShufflingResponse.toJSONString());
        return getShufflingResponse;
    }

    static JSONObject getShufflingParticipants(String shufflingId) {
        APICall apiCall = new APICall.Builder("getShufflingParticipants").
                param("shuffling", shufflingId).
                build();
        JSONObject getParticipantsResponse = apiCall.invoke();
        Logger.logMessage("getShufflingParticipantsResponse: " + getParticipantsResponse.toJSONString());
        return getParticipantsResponse;
    }

    static JSONObject process(String shufflingId, Tester tester, Tester recipient) {
        return process(shufflingId, tester, recipient, true);
    }

    static JSONObject process(String shufflingId, Tester tester, Tester recipient, boolean broadcast) {
        APICall.Builder builder = new APICall.Builder("shufflingProcess").
                param("shuffling", shufflingId).
                param("secretPhrase", tester.getSecretPhrase()).
                param("recipientSecretPhrase", recipient.getSecretPhrase()).
                feeNQT(0);
        if (!broadcast) {
            builder.param("broadcast", "false");
        }
        APICall apiCall = builder.build();
        JSONObject shufflingProcessResponse = apiCall.invoke();
        Logger.logMessage("shufflingProcessResponse: " + shufflingProcessResponse.toJSONString());
        return shufflingProcessResponse;
    }

    static JSONObject verify(String shufflingId, Tester tester, String shufflingStateHash) {
        APICall apiCall = new APICall.Builder("shufflingVerify").
                param("shuffling", shufflingId).
                param("secretPhrase", tester.getSecretPhrase()).
                param("shufflingStateHash", shufflingStateHash).
                feeNQT(Constants.ONE_NXT).
                build();
        JSONObject response = apiCall.invoke();
        Logger.logDebugMessage("shufflingVerifyResponse:" + response);
        return response;
    }

    static JSONObject cancel(String shufflingId, Tester tester, String shufflingStateHash, long cancellingAccountId) {
        return cancel(shufflingId, tester, shufflingStateHash, cancellingAccountId, true);
    }

    static JSONObject cancel(String shufflingId, Tester tester, String shufflingStateHash, long cancellingAccountId, boolean broadcast) {
        APICall.Builder builder = new APICall.Builder("shufflingCancel").
                param("shuffling", shufflingId).
                param("secretPhrase", tester.getSecretPhrase()).
                param("shufflingStateHash", shufflingStateHash).
                feeNQT(10 * Constants.ONE_NXT);
        if (cancellingAccountId != 0) {
            builder.param("cancellingAccount", Long.toUnsignedString(cancellingAccountId));
        }
        if (!broadcast) {
            builder.param("broadcast", "false");
        }
        APICall apiCall = builder.build();
        JSONObject response = apiCall.invoke();
        Logger.logDebugMessage("shufflingCancelResponse:" + response);
        return response;
    }

    static JSONObject broadcast(JSONObject transaction, Tester tester) {
        transaction.remove("signature");
        APICall apiCall = new APICall.Builder("signTransaction")
                .param("unsignedTransactionJSON", transaction.toJSONString())
                .param("validate", "false")
                .param("secretPhrase", tester.getSecretPhrase())
                .build();
        JSONObject response = apiCall.invoke();
        if (response.get("transactionJSON") == null) {
            return response;
        }
        apiCall = new APICall.Builder("broadcastTransaction").
                param("transactionJSON", ((JSONObject)response.get("transactionJSON")).toJSONString()).
                build();
        response = apiCall.invoke();
        Logger.logDebugMessage("broadcastTransactionResponse:" + response);
        return response;
    }

    static JSONObject startShuffler(Tester tester, Tester recipient, String shufflingFullHash) {
        APICall apiCall = new APICall.Builder("startShuffler").
                secretPhrase(tester.getSecretPhrase()).
                param("recipientPublicKey", Convert.toHexString(recipient.getPublicKey())).
                param("shufflingFullHash", shufflingFullHash).
                build();
        JSONObject response = apiCall.invoke();
        Logger.logMessage("startShufflerResponse: " + response.toJSONString());
        return response;
    }

    static JSONObject stopShuffler(Tester tester, String shufflingFullHash) {
        APICall apiCall = new APICall.Builder("stopShuffler").
                secretPhrase(tester.getSecretPhrase()).
                param("shufflingFullHash", shufflingFullHash).
                build();
        JSONObject response = apiCall.invoke();
        Logger.logMessage("stopShufflerResponse: " + response.toJSONString());
        return response;
    }

    static JSONObject sendMoney(Tester sender, Tester recipient, long amountNXT) {
        JSONObject response = new APICall.Builder("sendMoney").
                param("secretPhrase", sender.getSecretPhrase()).
                param("recipient", recipient.getStrId()).
                param("amountNQT", amountNXT * Constants.ONE_NXT).
                param("feeNQT", Constants.ONE_NXT).
                build().invoke();
        Logger.logMessage("sendMoneyReponse: " + response.toJSONString());
        return response;
    }

    private ShufflingUtil() {}

}
