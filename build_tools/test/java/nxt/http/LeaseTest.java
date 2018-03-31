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

public class LeaseTest extends BlockchainTest {

    @Test
    public void lease() {
        // #2 & #3 lease their balance to %1
        JSONObject response = new APICall.Builder("leaseBalance").
                param("secretPhrase", BOB.getSecretPhrase()).
                param("recipient", ALICE.getStrId()).
                param("period", "2").
                param("feeNQT", Constants.ONE_NXT).
                build().invoke();
        Logger.logDebugMessage("leaseBalance: " + response);
        response = new APICall.Builder("leaseBalance").
                param("secretPhrase", CHUCK.getSecretPhrase()).
                param("recipient", ALICE.getStrId()).
                param("period", "3").
                param("feeNQT", Constants.ONE_NXT).
                build().invoke();
        Logger.logDebugMessage("leaseBalance: " + response);
        generateBlock();

        // effective balance hasn't changed since lease is not in effect yet
        JSONObject lesseeResponse = new APICall.Builder("getAccount").
                param("account", ALICE.getRsAccount()).
                param("includeEffectiveBalance", "true").
                build().invoke();
        Logger.logDebugMessage("getLesseeAccount: " + lesseeResponse);
        Assert.assertEquals(ALICE.getInitialEffectiveBalance(), lesseeResponse.get("effectiveBalanceNXT"));

        // lease is registered
        JSONObject leasedResponse1 = new APICall.Builder("getAccount").
                param("account", BOB.getRsAccount()).
                build().invoke();
        Logger.logDebugMessage("getLeasedAccount: " + leasedResponse1);
        Assert.assertEquals(ALICE.getRsAccount(), leasedResponse1.get("currentLesseeRS"));
        Assert.assertEquals((long) (baseHeight + 1 + 1), leasedResponse1.get("currentLeasingHeightFrom"));
        Assert.assertEquals((long) (baseHeight + 1 + 1 + 2), leasedResponse1.get("currentLeasingHeightTo"));
        JSONObject leasedResponse2 = new APICall.Builder("getAccount").
                param("account", CHUCK.getRsAccount()).
                build().invoke();
        Logger.logDebugMessage("getLeasedAccount: " + leasedResponse1);
        Assert.assertEquals(ALICE.getRsAccount(), leasedResponse2.get("currentLesseeRS"));
        Assert.assertEquals((long) (baseHeight + 1 + 1), leasedResponse2.get("currentLeasingHeightFrom"));
        Assert.assertEquals((long) (baseHeight + 1 + 1 + 3), leasedResponse2.get("currentLeasingHeightTo"));
        generateBlock();


        lesseeResponse = new APICall.Builder("getAccount").
                param("account", ALICE.getRsAccount()).
                param("includeEffectiveBalance", "true").
                build().invoke();
        Logger.logDebugMessage("getLesseeAccount: " + lesseeResponse);
        Assert.assertEquals((ALICE.getInitialBalance() + BOB.getInitialBalance() + CHUCK.getInitialBalance()) / Constants.ONE_NXT - 2,
                lesseeResponse.get("effectiveBalanceNXT"));
        generateBlock();
        generateBlock();
        lesseeResponse = new APICall.Builder("getAccount").
                param("account", ALICE.getRsAccount()).
                param("includeEffectiveBalance", "true").
                build().invoke();
        Logger.logDebugMessage("getLesseeAccount: " + lesseeResponse);
        Assert.assertEquals((ALICE.getInitialBalance() + CHUCK.getInitialBalance()) / Constants.ONE_NXT - 1 /* fees */,
                lesseeResponse.get("effectiveBalanceNXT"));
        generateBlock();
        lesseeResponse = new APICall.Builder("getAccount").
                param("account", ALICE.getRsAccount()).
                param("includeEffectiveBalance", "true").
                build().invoke();
        Logger.logDebugMessage("getLesseeAccount: " + lesseeResponse);
        Assert.assertEquals((ALICE.getInitialBalance()) / Constants.ONE_NXT,
                lesseeResponse.get("effectiveBalanceNXT"));
    }
}
