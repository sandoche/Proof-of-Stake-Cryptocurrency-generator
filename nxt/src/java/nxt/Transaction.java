/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2020 Jelurida IP B.V.
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

package nxt;

import nxt.util.Filter;
import org.json.simple.JSONObject;

import java.util.List;

public interface Transaction {

    interface Builder {

        Builder recipientId(long recipientId);

        Builder referencedTransactionFullHash(String referencedTransactionFullHash);

        Builder appendix(Appendix.Message message);

        Builder appendix(Appendix.EncryptedMessage encryptedMessage);

        Builder appendix(Appendix.EncryptToSelfMessage encryptToSelfMessage);

        Builder appendix(Appendix.PublicKeyAnnouncement publicKeyAnnouncement);

        Builder appendix(Appendix.PrunablePlainMessage prunablePlainMessage);

        Builder appendix(Appendix.PrunableEncryptedMessage prunableEncryptedMessage);

        Builder appendix(Appendix.Phasing phasing);

        Builder timestamp(int timestamp);

        Builder ecBlockHeight(int height);

        Builder ecBlockId(long blockId);

        Transaction build() throws NxtException.NotValidException;

        Transaction build(String secretPhrase) throws NxtException.NotValidException;

    }

    long getId();

    String getStringId();

    long getSenderId();

    byte[] getSenderPublicKey();

    long getRecipientId();

    int getHeight();

    long getBlockId();

    Block getBlock();

    short getIndex();

    int getTimestamp();

    int getBlockTimestamp();

    short getDeadline();

    int getExpiration();

    long getAmountNQT();

    long getFeeNQT();

    String getReferencedTransactionFullHash();

    byte[] getSignature();

    String getFullHash();

    TransactionType getType();

    Attachment getAttachment();

    boolean verifySignature();

    void validate() throws NxtException.ValidationException;

    byte[] getBytes();

    byte[] getUnsignedBytes();

    JSONObject getJSONObject();

    JSONObject getPrunableAttachmentJSON();

    byte getVersion();

    int getFullSize();

    Appendix.Message getMessage();

    Appendix.EncryptedMessage getEncryptedMessage();

    Appendix.EncryptToSelfMessage getEncryptToSelfMessage();

    Appendix.Phasing getPhasing();

    Appendix.PrunablePlainMessage getPrunablePlainMessage();

    Appendix.PrunableEncryptedMessage getPrunableEncryptedMessage();

    List<? extends Appendix> getAppendages();

    List<? extends Appendix> getAppendages(boolean includeExpiredPrunable);

    List<? extends Appendix> getAppendages(Filter<Appendix> filter, boolean includeExpiredPrunable);

    int getECBlockHeight();

    long getECBlockId();
}
