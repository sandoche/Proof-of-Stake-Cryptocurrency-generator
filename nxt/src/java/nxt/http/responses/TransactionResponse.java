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

import nxt.TransactionType;
import nxt.addons.JO;
import org.json.simple.JSONObject;

public interface TransactionResponse {

    static TransactionResponse create(JSONObject object) {
        return new TransactionResponseImpl(object);
    }

    static TransactionResponse create(JO object) {
        return new TransactionResponseImpl((JO) object);
    }

    TransactionType getTransactionType();

    long getSenderId();

    String getSender();

    String getSenderRs();

    byte[] getSenderPublicKey();

    long getRecipientId();

    String getRecipient();

    String getRecipientRs();

    int getHeight();

    long getBlockId();

    short getIndex();

    int getTimestamp();

    int getBlockTimestamp();

    short getDeadline();

    int getExpiration();

    long getAmount();

    long getFee();

    byte[] getSignature();

    byte[] getFullHash();

    byte getType();

    byte getSubType();

    byte getVersion();

    int getECBlockHeight();

    long getECBlockId();

    boolean isPhased();

    long getFxtTransaction();

    long getTransactionId();

    String getUnsignedLongTransactionId();

    int getConfirmations();

    int getBlockTimeStamp();

    long getReferencedTransaction();

    byte[] getSignatureHash();

    int getEcBlockHeight();

    long getEcBlockId();

    JO getAttachmentJson();

    JO getJson();

    boolean similar(TransactionResponse transactionResponse);
}
