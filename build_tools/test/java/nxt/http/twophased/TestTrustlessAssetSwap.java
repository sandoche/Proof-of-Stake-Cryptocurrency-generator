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

import nxt.Account;
import nxt.BlockchainTest;
import nxt.Constants;
import nxt.http.APICall;
import nxt.util.Convert;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Test;

public class TestTrustlessAssetSwap extends BlockchainTest {

    @Test
    public void assetSwap() {
        // Alice and Bob each has its own asset
        JSONObject aliceAsset = new APICall.Builder("issueAsset").
                param("secretPhrase", ALICE.getSecretPhrase()).
                param("name", "AliceAsset").
                param("description", "AliceAssetDescription").
                param("quantityQNT", 1000).
                param("decimals", 0).
                param("feeNQT", 1000 * Constants.ONE_NXT).
                build().invoke();
        generateBlock();
        JSONObject bobAsset = new APICall.Builder("issueAsset").
                param("secretPhrase", BOB.getSecretPhrase()).
                param("name", "BobAsset").
                param("description", "BobAssetDescription").
                param("quantityQNT", 1000).
                param("decimals", 0).
                param("feeNQT", 2000 * Constants.ONE_NXT).
                build().invoke();
        generateBlock();

        // Alice prepares and signs a transaction #1, an asset transfer to Bob.
        // She does not broadcast it, but sends to Bob the unsigned bytes, the
        // full transaction hash, and the signature hash.
        String aliceAssetId = (String) aliceAsset.get("transaction");
        JSONObject aliceUnsignedTransfer = new APICall.Builder("transferAsset").
                param("publicKey", ALICE.getPublicKeyStr()).
                param("recipient", BOB.getStrId()).
                param("asset", aliceAssetId).
                param("quantityQNT", 100).
                param("feeNQT", Constants.ONE_NXT).
                build().invoke();

        JSONObject aliceSignedTransfer = new APICall.Builder("signTransaction").
                param("secretPhrase", ALICE.getSecretPhrase()).
                param("unsignedTransactionBytes", (String)aliceUnsignedTransfer.get("unsignedTransactionBytes")).
                build().invoke();

        String aliceTransferFullHash = (String)aliceSignedTransfer.get("fullHash");
        Assert.assertEquals(64, aliceTransferFullHash.length());
        String aliceTransferTransactionBytes = (String)aliceSignedTransfer.get("transactionBytes");

        // Bob submits transaction #2, an asset transfer to Alice, making it phased using a by-transaction voting model
        // with a quorum of 1 and just the full hash of #1 in the phasing transaction full hashes list.
        String bobAssetId = (String) bobAsset.get("transaction");
        JSONObject bobTransfer = new APICall.Builder("transferAsset").
                param("secretPhrase", BOB.getSecretPhrase()).
                param("recipient", ALICE.getStrId()).
                param("asset", bobAssetId).
                param("quantityQNT", 200).
                param("feeNQT", 3 * Constants.ONE_NXT).
                param("phased", "true").
                param("phasingFinishHeight", baseHeight + 5).
                param("phasingVotingModel", 4).
                param("phasingLinkedFullHash", aliceTransferFullHash).
                param("phasingQuorum", 1).
                build().invoke();
        generateBlock();

        // Alice sees Bob's transaction #2 in the blockchain, waits to make sure it is confirmed irreversibly.
        JSONObject bobTransferValidation = new APICall.Builder("getTransaction").
                param("transaction", (String) bobTransfer.get("transaction")).
                build().invoke();
        Assert.assertEquals(bobTransfer.get("transaction"), bobTransferValidation.get("transaction"));

        // She then submits her transaction #1.
        new APICall.Builder("broadcastTransaction").
                param("transactionBytes", aliceTransferTransactionBytes).
                build().invoke();
        generateBlock();

        // Both transactions have executed
        Assert.assertEquals(200, Account.getAssetBalanceQNT(ALICE.getId(), Convert.parseUnsignedLong(bobAssetId)));
        Assert.assertEquals(100, Account.getAssetBalanceQNT(BOB.getId(), Convert.parseUnsignedLong(aliceAssetId)));
    }

}
