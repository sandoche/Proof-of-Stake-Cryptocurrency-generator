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

package nxt.http.monetarysystem;

import nxt.BlockchainTest;
import nxt.Constants;
import nxt.http.APICall;
import nxt.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class DeleteCurrencyTest extends BlockchainTest {

    @Test
    public void deleteByIssuer() {
        APICall apiCall = new TestCurrencyIssuance.Builder().naming("yjwcv", "YJWCV", "test1").build();
        TestCurrencyIssuance.issueCurrencyApi(apiCall);
        generateBlock();
        apiCall = new APICall.Builder("getCurrency").param("code", "YJWCV").build();
        JSONObject response = apiCall.invoke();
        String currencyId = (String)response.get("currency");
        String code = (String)response.get("code");

        // Delete the currency
        apiCall = new APICall.Builder("deleteCurrency").
                secretPhrase(ALICE.getSecretPhrase()).feeNQT(Constants.ONE_NXT).
                param("currency", currencyId).
                build();
        response = apiCall.invoke();
        Logger.logDebugMessage("deleteCurrencyResponse:" + response);
        generateBlock();
        apiCall = new APICall.Builder("getCurrency").param("code", code).build();
        response = apiCall.invoke();
        Logger.logDebugMessage("getCurrencyResponse:" + response);
        Assert.assertEquals((long)5, response.get("errorCode"));
        Assert.assertEquals("Unknown currency", response.get("errorDescription"));

        // Issue the same currency code again
        apiCall = new TestCurrencyIssuance.Builder().naming("yjwcv", "YJWCV", "test1").build();
        TestCurrencyIssuance.issueCurrencyApi(apiCall);
        generateBlock();
        apiCall = new APICall.Builder("getCurrency").param("code", "YJWCV").build();
        response = apiCall.invoke();
        String newCurrencyId = (String)response.get("currency");
        String newCode = (String)response.get("code");
        Assert.assertNotEquals(currencyId, newCurrencyId); // this check may fail once in 2^64 tests
        Assert.assertEquals(code, newCode);
    }

    @Test
    public void deleteByNonOwnerNotAllowed() {
        APICall apiCall = new TestCurrencyIssuance.Builder().naming("yjwcv", "YJWCV", "test1").build();
        TestCurrencyIssuance.issueCurrencyApi(apiCall);
        generateBlock();
        apiCall = new APICall.Builder("getAllCurrencies").build();
        JSONObject response = apiCall.invoke();
        JSONArray currencies = (JSONArray) response.get("currencies");
        String currencyId = (String)((JSONObject)currencies.get(0)).get("currency");
        String code = (String)((JSONObject)currencies.get(0)).get("code");

        // Delete the currency
        apiCall = new APICall.Builder("deleteCurrency").
                secretPhrase(BOB.getSecretPhrase()).feeNQT(Constants.ONE_NXT).
                param("currency", currencyId).
                build();
        response = apiCall.invoke();
        Logger.logDebugMessage("deleteCurrencyResponse:" + response);
        Assert.assertEquals((long)8, response.get("errorCode"));

        // Verify that currency still exists
        apiCall = new APICall.Builder("getCurrency").param("code", code).build();
        response = apiCall.invoke();
        Assert.assertEquals(currencyId, response.get("currency"));
    }

    @Test
    public void deleteByOwnerNonIssuer() {
        APICall apiCall = new TestCurrencyIssuance.Builder().naming("yjwcv", "YJWCV", "test1").build();
        TestCurrencyIssuance.issueCurrencyApi(apiCall);
        generateBlock();

        apiCall = new APICall.Builder("getCurrency").param("code", "YJWCV").build();
        JSONObject response = apiCall.invoke();
        String currencyId = (String)response.get("currency");
        String code = (String)response.get("code");

        // Transfer all units
        apiCall = new APICall.Builder("transferCurrency").
                secretPhrase(ALICE.getSecretPhrase()).feeNQT(Constants.ONE_NXT).
                param("recipient", Long.toUnsignedString(BOB.getId())).
                param("currency", currencyId).
                param("code", code).
                param("units", (String)response.get("maxSupply")).
                build();
        response = apiCall.invoke();
        Logger.logDebugMessage("transferCurrencyResponse:" + response);
        generateBlock();

        // Delete the currency
        apiCall = new APICall.Builder("deleteCurrency").
                secretPhrase(BOB.getSecretPhrase()).feeNQT(Constants.ONE_NXT).
                param("currency", currencyId).
                build();
        response = apiCall.invoke();
        Logger.logDebugMessage("deleteCurrencyResponse:" + response);
        generateBlock();
        apiCall = new APICall.Builder("getCurrency").param("code", code).build();
        response = apiCall.invoke();
        Assert.assertEquals((long)5, response.get("errorCode"));
        Assert.assertEquals("Unknown currency", response.get("errorDescription"));

        // Issue the same currency code again by the original issuer
        apiCall = new TestCurrencyIssuance.Builder().naming("yjwcv", "YJWCV", "test1").build();
        TestCurrencyIssuance.issueCurrencyApi(apiCall);
        generateBlock();
        apiCall = new APICall.Builder("getCurrency").param("code", "YJWCV").build();
        response = apiCall.invoke();
        String newCurrencyId = (String)response.get("currency");
        String newCode = (String)response.get("code");
        Assert.assertNotEquals(currencyId, newCurrencyId); // this check may fail once in 2^64 tests
        Assert.assertEquals(code, newCode);
    }

}
