/*
 * Copyright © 2016-2020 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of this software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package nxt.http.responses;

import nxt.addons.JO;
import org.json.simple.JSONObject;

import java.math.BigInteger;
import java.util.List;

public interface BlockResponse {

    static BlockResponse create(JSONObject object) {
        return new BlockResponseImpl(object);
    }

    static BlockResponse create(JO object) {
        return new BlockResponseImpl((JO) object);
    }

    long getBlockId();

    String getBlock();

    int getHeight();

    long getGeneratorId();

    String getGenerator();

    String getGeneratorRs();

    byte[] getGeneratorPublicKey();

    int getTimestamp();

    int getNumberOfTransactions();

    long getTotalFeeFQT();

    byte getVersion();

    long getBaseTarget();

    BigInteger getCumulativeDifficulty();

    long getPreviousBlockId();

    String getPreviousBlock();

    long getNextBlockId();

    String getNextBlock();

    byte[] getPayloadHash();

    byte[] getGenerationSignature();

    byte[] getPreviousBlockHash();

    byte[] getBlockSignature();

    List<byte[]> getParentTransactionFullHashes();

    List<TransactionResponse> getParentTransactions();

    List<Long> getExecutedPhasedTransactionIds();

    List<TransactionResponse> getExecutedPhasedTransactions();
}
