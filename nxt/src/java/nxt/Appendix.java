/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2018 Jelurida IP B.V.
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

import nxt.AccountLedger.LedgerEvent;
import nxt.crypto.Crypto;
import nxt.crypto.EncryptedData;
import nxt.util.Convert;
import nxt.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public interface Appendix {

    int getSize();
    int getFullSize();
    void putBytes(ByteBuffer buffer);
    JSONObject getJSONObject();
    byte getVersion();
    int getBaselineFeeHeight();
    Fee getBaselineFee(Transaction transaction);
    int getNextFeeHeight();
    Fee getNextFee(Transaction transaction);
    boolean isPhased(Transaction transaction);

    interface Prunable {
        byte[] getHash();
        boolean hasPrunableData();
        void restorePrunableData(Transaction transaction, int blockTimestamp, int height);
        default boolean shouldLoadPrunable(Transaction transaction, boolean includeExpiredPrunable) {
            return Nxt.getEpochTime() - transaction.getTimestamp() <
                    (includeExpiredPrunable && Constants.INCLUDE_EXPIRED_PRUNABLE ?
                            Constants.MAX_PRUNABLE_LIFETIME : Constants.MIN_PRUNABLE_LIFETIME);
        }
    }

    interface Encryptable {
        void encrypt(String secretPhrase);
    }


    abstract class AbstractAppendix implements Appendix {

        private final byte version;

        AbstractAppendix(JSONObject attachmentData) {
            Long l = (Long) attachmentData.get("version." + getAppendixName());
            version = (byte) (l == null ? 0 : l);
        }

        AbstractAppendix(ByteBuffer buffer, byte transactionVersion) {
            if (transactionVersion == 0) {
                version = 0;
            } else {
                version = buffer.get();
            }
        }

        AbstractAppendix(int version) {
            this.version = (byte) version;
        }

        AbstractAppendix() {
            this.version = 1;
        }

        abstract String getAppendixName();

        @Override
        public final int getSize() {
            return getMySize() + (version > 0 ? 1 : 0);
        }

        @Override
        public final int getFullSize() {
            return getMyFullSize() + (version > 0 ? 1 : 0);
        }

        abstract int getMySize();

        int getMyFullSize() {
            return getMySize();
        }

        @Override
        public final void putBytes(ByteBuffer buffer) {
            if (version > 0) {
                buffer.put(version);
            }
            putMyBytes(buffer);
        }

        abstract void putMyBytes(ByteBuffer buffer);

        @Override
        public final JSONObject getJSONObject() {
            JSONObject json = new JSONObject();
            json.put("version." + getAppendixName(), version);
            putMyJSON(json);
            return json;
        }

        abstract void putMyJSON(JSONObject json);

        @Override
        public final byte getVersion() {
            return version;
        }

        boolean verifyVersion(byte transactionVersion) {
            return transactionVersion == 0 ? version == 0 : version == 1;
        }

        @Override
        public int getBaselineFeeHeight() {
            return Constants.SHUFFLING_BLOCK;
        }

        @Override
        public Fee getBaselineFee(Transaction transaction) {
            return Fee.NONE;
        }

        @Override
        public int getNextFeeHeight() {
            return Integer.MAX_VALUE;
        }

        @Override
        public Fee getNextFee(Transaction transaction) {
            return getBaselineFee(transaction);
        }

        abstract void validate(Transaction transaction) throws NxtException.ValidationException;

        void validateAtFinish(Transaction transaction) throws NxtException.ValidationException {
            if (!isPhased(transaction)) {
                return;
            }
            validate(transaction);
        }

        abstract void apply(Transaction transaction, Account senderAccount, Account recipientAccount);

        final void loadPrunable(Transaction transaction) {
            loadPrunable(transaction, false);
        }

        void loadPrunable(Transaction transaction, boolean includeExpiredPrunable) {}

        abstract boolean isPhasable();

        @Override
        public final boolean isPhased(Transaction transaction) {
            return isPhasable() && transaction.getPhasing() != null;
        }

    }

    static boolean hasAppendix(String appendixName, JSONObject attachmentData) {
        return attachmentData.get("version." + appendixName) != null;
    }

    class Message extends AbstractAppendix {

        private static final String appendixName = "Message";

        static Message parse(JSONObject attachmentData) {
            if (!hasAppendix(appendixName, attachmentData)) {
                return null;
            }
            return new Message(attachmentData);
        }

        private static final Fee MESSAGE_FEE = new Fee.SizeBasedFee(0, Constants.ONE_NXT, 32) {
            @Override
            public int getSize(TransactionImpl transaction, Appendix appendage) {
                return ((Message)appendage).getMessage().length;
            }
        };

        private final byte[] message;
        private final boolean isText;

        Message(ByteBuffer buffer, byte transactionVersion) throws NxtException.NotValidException {
            super(buffer, transactionVersion);
            int messageLength = buffer.getInt();
            this.isText = messageLength < 0; // ugly hack
            if (messageLength < 0) {
                messageLength &= Integer.MAX_VALUE;
            }
            if (messageLength > 1000) {
                throw new NxtException.NotValidException("Invalid arbitrary message length: " + messageLength);
            }
            this.message = new byte[messageLength];
            buffer.get(this.message);
            if (isText && !Arrays.equals(message, Convert.toBytes(Convert.toString(message)))) {
                throw new NxtException.NotValidException("Message is not UTF-8 text");
            }
        }

        private Message(JSONObject attachmentData) {
            super(attachmentData);
            String messageString = (String)attachmentData.get("message");
            this.isText = Boolean.TRUE.equals(attachmentData.get("messageIsText"));
            this.message = isText ? Convert.toBytes(messageString) : Convert.parseHexString(messageString);
        }

        public Message(byte[] message) {
            this(message, false);
        }

        public Message(String string) {
            this(Convert.toBytes(string), true);
        }

        public Message(String string, boolean isText) {
            this(isText ? Convert.toBytes(string) : Convert.parseHexString(string), isText);
        }

        public Message(byte[] message, boolean isText) {
            this.message = message;
            this.isText = isText;
        }

        @Override
        String getAppendixName() {
            return appendixName;
        }

        @Override
        int getMySize() {
            return 4 + message.length;
        }

        @Override
        void putMyBytes(ByteBuffer buffer) {
            buffer.putInt(isText ? (message.length | Integer.MIN_VALUE) : message.length);
            buffer.put(message);
        }

        @Override
        void putMyJSON(JSONObject json) {
            json.put("message", Convert.toString(message, isText));
            json.put("messageIsText", isText);
        }

        @Override
        public Fee getBaselineFee(Transaction transaction) {
            return MESSAGE_FEE;
        }

        @Override
        void validate(Transaction transaction) throws NxtException.ValidationException {
            if (Nxt.getBlockchain().getHeight() > Constants.SHUFFLING_BLOCK && message.length > Constants.MAX_ARBITRARY_MESSAGE_LENGTH) {
                throw new NxtException.NotValidException("Invalid arbitrary message length: " + message.length);
            }
        }

        @Override
        void apply(Transaction transaction, Account senderAccount, Account recipientAccount) {}

        public byte[] getMessage() {
            return message;
        }

        public boolean isText() {
            return isText;
        }

        @Override
        boolean isPhasable() {
            return false;
        }

    }

    class PrunablePlainMessage extends Appendix.AbstractAppendix implements Prunable {

        private static final String appendixName = "PrunablePlainMessage";

        private static final Fee PRUNABLE_MESSAGE_FEE = new Fee.SizeBasedFee(Constants.ONE_NXT/10) {
            @Override
            public int getSize(TransactionImpl transaction, Appendix appendix) {
                return appendix.getFullSize();
            }
        };

        static PrunablePlainMessage parse(JSONObject attachmentData) {
            if (!hasAppendix(appendixName, attachmentData)) {
                return null;
            }
            return new PrunablePlainMessage(attachmentData);
        }

        private final byte[] hash;
        private final byte[] message;
        private final boolean isText;
        private volatile PrunableMessage prunableMessage;

        PrunablePlainMessage(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
            this.hash = new byte[32];
            buffer.get(this.hash);
            this.message = null;
            this.isText = false;
        }

        private PrunablePlainMessage(JSONObject attachmentData) {
            super(attachmentData);
            String hashString = Convert.emptyToNull((String) attachmentData.get("messageHash"));
            String messageString = Convert.emptyToNull((String) attachmentData.get("message"));
            if (hashString != null && messageString == null) {
                this.hash = Convert.parseHexString(hashString);
                this.message = null;
                this.isText = false;
            } else {
                this.hash = null;
                this.isText = Boolean.TRUE.equals(attachmentData.get("messageIsText"));
                this.message = Convert.toBytes(messageString, isText);
            }
        }

        public PrunablePlainMessage(byte[] message) {
            this(message, false);
        }

        public PrunablePlainMessage(String string) {
            this(Convert.toBytes(string), true);
        }

        public PrunablePlainMessage(String string, boolean isText) {
            this(Convert.toBytes(string, isText), isText);
        }

        public PrunablePlainMessage(byte[] message, boolean isText) {
            this.message = message;
            this.isText = isText;
            this.hash = null;
        }

        @Override
        String getAppendixName() {
            return appendixName;
        }

        @Override
        public Fee getBaselineFee(Transaction transaction) {
            return PRUNABLE_MESSAGE_FEE;
        }

        @Override
        int getMySize() {
            return 32;
        }

        @Override
        int getMyFullSize() {
            return getMessage() == null ? 0 : getMessage().length;
        }

        @Override
        void putMyBytes(ByteBuffer buffer) {
            buffer.put(getHash());
        }

        @Override
        void putMyJSON(JSONObject json) {
            if (prunableMessage != null) {
                json.put("message", Convert.toString(prunableMessage.getMessage(), prunableMessage.messageIsText()));
                json.put("messageIsText", prunableMessage.messageIsText());
            } else if (message != null) {
                json.put("message", Convert.toString(message, isText));
                json.put("messageIsText", isText);
            }
            json.put("messageHash", Convert.toHexString(getHash()));
        }

        @Override
        void validate(Transaction transaction) throws NxtException.ValidationException {
            if (transaction.getMessage() != null) {
                throw new NxtException.NotValidException("Cannot have both message and prunable message attachments");
            }
            byte[] msg = getMessage();
            if (msg != null && msg.length > Constants.MAX_PRUNABLE_MESSAGE_LENGTH) {
                throw new NxtException.NotValidException("Invalid prunable message length: " + msg.length);
            }
            if (msg == null && Nxt.getEpochTime() - transaction.getTimestamp() < Constants.MIN_PRUNABLE_LIFETIME) {
                throw new NxtException.NotCurrentlyValidException("Message has been pruned prematurely");
            }
        }

        @Override
        void apply(Transaction transaction, Account senderAccount, Account recipientAccount) {
            if (Nxt.getEpochTime() - transaction.getTimestamp() < Constants.MAX_PRUNABLE_LIFETIME) {
                PrunableMessage.add((TransactionImpl)transaction, this);
            }
        }

        public byte[] getMessage() {
            if (prunableMessage != null) {
                return prunableMessage.getMessage();
            }
            return message;
        }

        public boolean isText() {
            if (prunableMessage != null) {
                return prunableMessage.messageIsText();
            }
            return isText;
        }

        @Override
        public byte[] getHash() {
            if (hash != null) {
                return hash;
            }
            MessageDigest digest = Crypto.sha256();
            digest.update((byte)(isText ? 1 : 0));
            digest.update(message);
            return digest.digest();
        }

        @Override
        final void loadPrunable(Transaction transaction, boolean includeExpiredPrunable) {
            if (!hasPrunableData() && shouldLoadPrunable(transaction, includeExpiredPrunable)) {
                PrunableMessage prunableMessage = PrunableMessage.getPrunableMessage(transaction.getId());
                if (prunableMessage != null && prunableMessage.getMessage() != null) {
                    this.prunableMessage = prunableMessage;
                }
            }
        }

        @Override
        boolean isPhasable() {
            return false;
        }

        @Override
        public final boolean hasPrunableData() {
            return (prunableMessage != null || message != null);
        }

        @Override
        public void restorePrunableData(Transaction transaction, int blockTimestamp, int height) {
            PrunableMessage.add((TransactionImpl)transaction, this, blockTimestamp, height);
        }
    }

    abstract class AbstractEncryptedMessage extends AbstractAppendix {

        private static final Fee ENCRYPTED_MESSAGE_FEE = new Fee.SizeBasedFee(Constants.ONE_NXT, Constants.ONE_NXT, 32) {
            @Override
            public int getSize(TransactionImpl transaction, Appendix appendage) {
                return ((AbstractEncryptedMessage)appendage).getEncryptedDataLength() - 16;
            }
        };

        private EncryptedData encryptedData;
        private final boolean isText;
        private final boolean isCompressed;

        private AbstractEncryptedMessage(ByteBuffer buffer, byte transactionVersion) throws NxtException.NotValidException {
            super(buffer, transactionVersion);
            int length = buffer.getInt();
            this.isText = length < 0;
            if (length < 0) {
                length &= Integer.MAX_VALUE;
            }
            this.encryptedData = EncryptedData.readEncryptedData(buffer, length, 1000);
            this.isCompressed = getVersion() != 2;
        }

        private AbstractEncryptedMessage(JSONObject attachmentJSON, JSONObject encryptedMessageJSON) {
            super(attachmentJSON);
            byte[] data = Convert.parseHexString((String)encryptedMessageJSON.get("data"));
            byte[] nonce = Convert.parseHexString((String) encryptedMessageJSON.get("nonce"));
            this.encryptedData = new EncryptedData(data, nonce);
            this.isText = Boolean.TRUE.equals(encryptedMessageJSON.get("isText"));
            Object isCompressed = encryptedMessageJSON.get("isCompressed");
            this.isCompressed = isCompressed == null || Boolean.TRUE.equals(isCompressed);
        }

        private AbstractEncryptedMessage(EncryptedData encryptedData, boolean isText, boolean isCompressed) {
            super(isCompressed ? 1 : 2);
            this.encryptedData = encryptedData;
            this.isText = isText;
            this.isCompressed = isCompressed;
        }

        @Override
        int getMySize() {
            return 4 + encryptedData.getSize();
        }

        @Override
        void putMyBytes(ByteBuffer buffer) {
            buffer.putInt(isText ? (encryptedData.getData().length | Integer.MIN_VALUE) : encryptedData.getData().length);
            buffer.put(encryptedData.getData());
            buffer.put(encryptedData.getNonce());
        }

        @Override
        void putMyJSON(JSONObject json) {
            json.put("data", Convert.toHexString(encryptedData.getData()));
            json.put("nonce", Convert.toHexString(encryptedData.getNonce()));
            json.put("isText", isText);
            json.put("isCompressed", isCompressed);
        }

        @Override
        public Fee getBaselineFee(Transaction transaction) {
            return ENCRYPTED_MESSAGE_FEE;
        }

        @Override
        void validate(Transaction transaction) throws NxtException.ValidationException {
            if (Nxt.getBlockchain().getHeight() > Constants.SHUFFLING_BLOCK && getEncryptedDataLength() > Constants.MAX_ENCRYPTED_MESSAGE_LENGTH) {
                throw new NxtException.NotValidException("Max encrypted message length exceeded");
            }
            if (encryptedData != null) {
                if ((encryptedData.getNonce().length != 32 && encryptedData.getData().length > 0)
                        || (encryptedData.getNonce().length != 0 && encryptedData.getData().length == 0)) {
                    throw new NxtException.NotValidException("Invalid nonce length " + encryptedData.getNonce().length);
                }
            }
            if ((getVersion() != 2 && !isCompressed) || (getVersion() == 2 && isCompressed)) {
                throw new NxtException.NotValidException("Version mismatch - version " + getVersion() + ", isCompressed " + isCompressed);
            }
        }

        @Override
        final boolean verifyVersion(byte transactionVersion) {
            return transactionVersion == 0 ? getVersion() == 0 : (getVersion() == 1 || getVersion() == 2);
        }

        @Override
        void apply(Transaction transaction, Account senderAccount, Account recipientAccount) {}

        public final EncryptedData getEncryptedData() {
            return encryptedData;
        }

        final void setEncryptedData(EncryptedData encryptedData) {
            this.encryptedData = encryptedData;
        }

        int getEncryptedDataLength() {
            return encryptedData.getData().length;
        }

        public final boolean isText() {
            return isText;
        }

        public final boolean isCompressed() {
            return isCompressed;
        }

        @Override
        final boolean isPhasable() {
            return false;
        }

    }

    class PrunableEncryptedMessage extends AbstractAppendix implements Prunable {

        private static final String appendixName = "PrunableEncryptedMessage";

        private static final Fee PRUNABLE_ENCRYPTED_DATA_FEE = new Fee.SizeBasedFee(Constants.ONE_NXT/10) {
            @Override
            public int getSize(TransactionImpl transaction, Appendix appendix) {
                return appendix.getFullSize();
            }
        };

        static PrunableEncryptedMessage parse(JSONObject attachmentData) {
            if (!hasAppendix(appendixName, attachmentData)) {
                return null;
            }
            JSONObject encryptedMessageJSON = (JSONObject)attachmentData.get("encryptedMessage");
            if (encryptedMessageJSON != null && encryptedMessageJSON.get("data") == null) {
                return new UnencryptedPrunableEncryptedMessage(attachmentData);
            }
            return new PrunableEncryptedMessage(attachmentData);
        }

        private final byte[] hash;
        private EncryptedData encryptedData;
        private final boolean isText;
        private final boolean isCompressed;
        private volatile PrunableMessage prunableMessage;

        PrunableEncryptedMessage(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
            this.hash = new byte[32];
            buffer.get(this.hash);
            this.encryptedData = null;
            this.isText = false;
            this.isCompressed = false;
        }

        private PrunableEncryptedMessage(JSONObject attachmentJSON) {
            super(attachmentJSON);
            String hashString = Convert.emptyToNull((String) attachmentJSON.get("encryptedMessageHash"));
            JSONObject encryptedMessageJSON = (JSONObject) attachmentJSON.get("encryptedMessage");
            if (hashString != null && encryptedMessageJSON == null) {
                this.hash = Convert.parseHexString(hashString);
                this.encryptedData = null;
                this.isText = false;
                this.isCompressed = false;
            } else {
                this.hash = null;
                byte[] data = Convert.parseHexString((String) encryptedMessageJSON.get("data"));
                byte[] nonce = Convert.parseHexString((String) encryptedMessageJSON.get("nonce"));
                this.encryptedData = new EncryptedData(data, nonce);
                this.isText = Boolean.TRUE.equals(encryptedMessageJSON.get("isText"));
                this.isCompressed = Boolean.TRUE.equals(encryptedMessageJSON.get("isCompressed"));
            }
        }

        public PrunableEncryptedMessage(EncryptedData encryptedData, boolean isText, boolean isCompressed) {
            this.encryptedData = encryptedData;
            this.isText = isText;
            this.isCompressed = isCompressed;
            this.hash = null;
        }

        @Override
        public final Fee getBaselineFee(Transaction transaction) {
            return PRUNABLE_ENCRYPTED_DATA_FEE;
        }

        @Override
        final int getMySize() {
            return 32;
        }

        @Override
        final int getMyFullSize() {
            return getEncryptedDataLength();
        }

        @Override
        void putMyBytes(ByteBuffer buffer) {
            buffer.put(getHash());
        }

        @Override
        void putMyJSON(JSONObject json) {
            if (prunableMessage != null) {
                JSONObject encryptedMessageJSON = new JSONObject();
                json.put("encryptedMessage", encryptedMessageJSON);
                encryptedMessageJSON.put("data", Convert.toHexString(prunableMessage.getEncryptedData().getData()));
                encryptedMessageJSON.put("nonce", Convert.toHexString(prunableMessage.getEncryptedData().getNonce()));
                encryptedMessageJSON.put("isText", prunableMessage.encryptedMessageIsText());
                encryptedMessageJSON.put("isCompressed", prunableMessage.isCompressed());
            } else if (encryptedData != null) {
                JSONObject encryptedMessageJSON = new JSONObject();
                json.put("encryptedMessage", encryptedMessageJSON);
                encryptedMessageJSON.put("data", Convert.toHexString(encryptedData.getData()));
                encryptedMessageJSON.put("nonce", Convert.toHexString(encryptedData.getNonce()));
                encryptedMessageJSON.put("isText", isText);
                encryptedMessageJSON.put("isCompressed", isCompressed);
            }
            json.put("encryptedMessageHash", Convert.toHexString(getHash()));
        }

        @Override
        final String getAppendixName() {
            return appendixName;
        }

        @Override
        void validate(Transaction transaction) throws NxtException.ValidationException {
            if (transaction.getEncryptedMessage() != null) {
                throw new NxtException.NotValidException("Cannot have both encrypted and prunable encrypted message attachments");
            }
            EncryptedData ed = getEncryptedData();
            if (ed == null && Nxt.getEpochTime() - transaction.getTimestamp() < Constants.MIN_PRUNABLE_LIFETIME) {
                throw new NxtException.NotCurrentlyValidException("Encrypted message has been pruned prematurely");
            }
            if (ed != null) {
                if (ed.getData().length > Constants.MAX_PRUNABLE_ENCRYPTED_MESSAGE_LENGTH) {
                    throw new NxtException.NotValidException(String.format("Message length %d exceeds max prunable encrypted message length %d",
                            ed.getData().length, Constants.MAX_PRUNABLE_ENCRYPTED_MESSAGE_LENGTH));
                }
                if ((ed.getNonce().length != 32 && ed.getData().length > 0)
                        || (ed.getNonce().length != 0 && ed.getData().length == 0)) {
                    throw new NxtException.NotValidException("Invalid nonce length " + ed.getNonce().length);
                }
            }
            if (transaction.getRecipientId() == 0) {
                throw new NxtException.NotValidException("Encrypted messages cannot be attached to transactions with no recipient");
            }
        }

        @Override
        void apply(Transaction transaction, Account senderAccount, Account recipientAccount) {
            if (Nxt.getEpochTime() - transaction.getTimestamp() < Constants.MAX_PRUNABLE_LIFETIME) {
                PrunableMessage.add((TransactionImpl)transaction, this);
            }
        }

        public final EncryptedData getEncryptedData() {
            if (prunableMessage != null) {
                return prunableMessage.getEncryptedData();
            }
            return encryptedData;
        }

        final void setEncryptedData(EncryptedData encryptedData) {
            this.encryptedData = encryptedData;
        }

        int getEncryptedDataLength() {
            return getEncryptedData() == null ? 0 : getEncryptedData().getData().length;
        }

        public final boolean isText() {
            if (prunableMessage != null) {
                return prunableMessage.encryptedMessageIsText();
            }
            return isText;
        }

        public final boolean isCompressed() {
            if (prunableMessage != null) {
                return prunableMessage.isCompressed();
            }
            return isCompressed;
        }

        @Override
        public final byte[] getHash() {
            if (hash != null) {
                return hash;
            }
            MessageDigest digest = Crypto.sha256();
            digest.update((byte)(isText ? 1 : 0));
            digest.update((byte)(isCompressed ? 1 : 0));
            digest.update(encryptedData.getData());
            digest.update(encryptedData.getNonce());
            return digest.digest();
        }

        @Override
        void loadPrunable(Transaction transaction, boolean includeExpiredPrunable) {
            if (!hasPrunableData() && shouldLoadPrunable(transaction, includeExpiredPrunable)) {
                PrunableMessage prunableMessage = PrunableMessage.getPrunableMessage(transaction.getId());
                if (prunableMessage != null && prunableMessage.getEncryptedData() != null) {
                    this.prunableMessage = prunableMessage;
                }
            }
        }

        @Override
        final boolean isPhasable() {
            return false;
        }

        @Override
        public final boolean hasPrunableData() {
            return (prunableMessage != null || encryptedData != null);
        }

        @Override
        public void restorePrunableData(Transaction transaction, int blockTimestamp, int height) {
            PrunableMessage.add((TransactionImpl)transaction, this, blockTimestamp, height);
        }
    }

    final class UnencryptedPrunableEncryptedMessage extends PrunableEncryptedMessage implements Encryptable {

        private final byte[] messageToEncrypt;
        private final byte[] recipientPublicKey;

        private UnencryptedPrunableEncryptedMessage(JSONObject attachmentJSON) {
            super(attachmentJSON);
            setEncryptedData(null);
            JSONObject encryptedMessageJSON = (JSONObject)attachmentJSON.get("encryptedMessage");
            String messageToEncryptString = (String)encryptedMessageJSON.get("messageToEncrypt");
            this.messageToEncrypt = isText() ? Convert.toBytes(messageToEncryptString) : Convert.parseHexString(messageToEncryptString);
            this.recipientPublicKey = Convert.parseHexString((String)attachmentJSON.get("recipientPublicKey"));
        }

        public UnencryptedPrunableEncryptedMessage(byte[] messageToEncrypt, boolean isText, boolean isCompressed, byte[] recipientPublicKey) {
            super(null, isText, isCompressed);
            this.messageToEncrypt = messageToEncrypt;
            this.recipientPublicKey = recipientPublicKey;
        }

        @Override
        void putMyBytes(ByteBuffer buffer) {
            if (getEncryptedData() == null) {
                throw new NxtException.NotYetEncryptedException("Prunable encrypted message not yet encrypted");
            }
            super.putMyBytes(buffer);
        }

        @Override
        void putMyJSON(JSONObject json) {
            if (getEncryptedData() == null) {
                JSONObject encryptedMessageJSON = new JSONObject();
                encryptedMessageJSON.put("messageToEncrypt", isText() ? Convert.toString(messageToEncrypt) : Convert.toHexString(messageToEncrypt));
                encryptedMessageJSON.put("isText", isText());
                encryptedMessageJSON.put("isCompressed", isCompressed());
                json.put("recipientPublicKey", Convert.toHexString(recipientPublicKey));
                json.put("encryptedMessage", encryptedMessageJSON);
            } else {
                super.putMyJSON(json);
            }
        }

        @Override
        void validate(Transaction transaction) throws NxtException.ValidationException {
            if (getEncryptedData() == null) {
                int dataLength = getEncryptedDataLength();
                if (dataLength > Constants.MAX_PRUNABLE_ENCRYPTED_MESSAGE_LENGTH) {
                    throw new NxtException.NotValidException(String.format("Message length %d exceeds max prunable encrypted message length %d",
                            dataLength, Constants.MAX_PRUNABLE_ENCRYPTED_MESSAGE_LENGTH));
                }
            } else {
                super.validate(transaction);
            }
        }

        @Override
        void apply(Transaction transaction, Account senderAccount, Account recipientAccount) {
            if (getEncryptedData() == null) {
                throw new NxtException.NotYetEncryptedException("Prunable encrypted message not yet encrypted");
            }
            super.apply(transaction, senderAccount, recipientAccount);
        }

        @Override
        void loadPrunable(Transaction transaction, boolean includeExpiredPrunable) {}

        @Override
        public void encrypt(String secretPhrase) {
            setEncryptedData(EncryptedData.encrypt(getPlaintext(), secretPhrase, recipientPublicKey));
        }

        @Override
        int getEncryptedDataLength() {
            return EncryptedData.getEncryptedDataLength(getPlaintext());
        }

        private byte[] getPlaintext() {
            return isCompressed() && messageToEncrypt.length > 0 ? Convert.compress(messageToEncrypt) : messageToEncrypt;
        }

    }

    class EncryptedMessage extends AbstractEncryptedMessage {

        private static final String appendixName = "EncryptedMessage";

        static EncryptedMessage parse(JSONObject attachmentData) {
            if (!hasAppendix(appendixName, attachmentData)) {
                return null;
            }
            if (((JSONObject)attachmentData.get("encryptedMessage")).get("data") == null) {
                return new UnencryptedEncryptedMessage(attachmentData);
            }
            return new EncryptedMessage(attachmentData);
        }

        EncryptedMessage(ByteBuffer buffer, byte transactionVersion) throws NxtException.NotValidException {
            super(buffer, transactionVersion);
        }

        EncryptedMessage(JSONObject attachmentData) {
            super(attachmentData, (JSONObject)attachmentData.get("encryptedMessage"));
        }

        public EncryptedMessage(EncryptedData encryptedData, boolean isText, boolean isCompressed) {
            super(encryptedData, isText, isCompressed);
        }

        @Override
        final String getAppendixName() {
            return appendixName;
        }

        @Override
        void putMyJSON(JSONObject json) {
            JSONObject encryptedMessageJSON = new JSONObject();
            super.putMyJSON(encryptedMessageJSON);
            json.put("encryptedMessage", encryptedMessageJSON);
        }

        @Override
        void validate(Transaction transaction) throws NxtException.ValidationException {
            super.validate(transaction);
            if (transaction.getRecipientId() == 0) {
                throw new NxtException.NotValidException("Encrypted messages cannot be attached to transactions with no recipient");
            }
        }

    }

    final class UnencryptedEncryptedMessage extends EncryptedMessage implements Encryptable {

        private final byte[] messageToEncrypt;
        private final byte[] recipientPublicKey;

        UnencryptedEncryptedMessage(JSONObject attachmentData) {
            super(attachmentData);
            setEncryptedData(null);
            JSONObject encryptedMessageJSON = (JSONObject)attachmentData.get("encryptedMessage");
            String messageToEncryptString = (String)encryptedMessageJSON.get("messageToEncrypt");
            messageToEncrypt = isText() ? Convert.toBytes(messageToEncryptString) : Convert.parseHexString(messageToEncryptString);
            recipientPublicKey = Convert.parseHexString((String)attachmentData.get("recipientPublicKey"));
        }

        public UnencryptedEncryptedMessage(byte[] messageToEncrypt, boolean isText, boolean isCompressed, byte[] recipientPublicKey) {
            super(null, isText, isCompressed);
            this.messageToEncrypt = messageToEncrypt;
            this.recipientPublicKey = recipientPublicKey;
        }

        @Override
        int getMySize() {
            if (getEncryptedData() != null) {
                return super.getMySize();
            }
            return 4 + EncryptedData.getEncryptedSize(getPlaintext());
        }

        @Override
        void putMyBytes(ByteBuffer buffer) {
            if (getEncryptedData() == null) {
                throw new NxtException.NotYetEncryptedException("Message not yet encrypted");
            }
            super.putMyBytes(buffer);
        }

        @Override
        void putMyJSON(JSONObject json) {
            if (getEncryptedData() == null) {
                JSONObject encryptedMessageJSON = new JSONObject();
                encryptedMessageJSON.put("messageToEncrypt", isText() ? Convert.toString(messageToEncrypt) : Convert.toHexString(messageToEncrypt));
                encryptedMessageJSON.put("isText", isText());
                encryptedMessageJSON.put("isCompressed", isCompressed());
                json.put("encryptedMessage", encryptedMessageJSON);
                json.put("recipientPublicKey", Convert.toHexString(recipientPublicKey));
            } else {
                super.putMyJSON(json);
            }
        }

        @Override
        void apply(Transaction transaction, Account senderAccount, Account recipientAccount) {
            if (getEncryptedData() == null) {
                throw new NxtException.NotYetEncryptedException("Message not yet encrypted");
            }
            super.apply(transaction, senderAccount, recipientAccount);
        }

        @Override
        public void encrypt(String secretPhrase) {
            setEncryptedData(EncryptedData.encrypt(getPlaintext(), secretPhrase, recipientPublicKey));
        }

        private byte[] getPlaintext() {
            return isCompressed() && messageToEncrypt.length > 0 ? Convert.compress(messageToEncrypt) : messageToEncrypt;
        }

        @Override
        int getEncryptedDataLength() {
            return EncryptedData.getEncryptedDataLength(getPlaintext());
        }

    }

    class EncryptToSelfMessage extends AbstractEncryptedMessage {

        private static final String appendixName = "EncryptToSelfMessage";

        static EncryptToSelfMessage parse(JSONObject attachmentData) {
            if (!hasAppendix(appendixName, attachmentData)) {
                return null;
            }
            if (((JSONObject)attachmentData.get("encryptToSelfMessage")).get("data") == null) {
                return new UnencryptedEncryptToSelfMessage(attachmentData);
            }
            return new EncryptToSelfMessage(attachmentData);
        }

        EncryptToSelfMessage(ByteBuffer buffer, byte transactionVersion) throws NxtException.NotValidException {
            super(buffer, transactionVersion);
        }

        EncryptToSelfMessage(JSONObject attachmentData) {
            super(attachmentData, (JSONObject)attachmentData.get("encryptToSelfMessage"));
        }

        public EncryptToSelfMessage(EncryptedData encryptedData, boolean isText, boolean isCompressed) {
            super(encryptedData, isText, isCompressed);
        }

        @Override
        final String getAppendixName() {
            return appendixName;
        }

        @Override
        void putMyJSON(JSONObject json) {
            JSONObject encryptToSelfMessageJSON = new JSONObject();
            super.putMyJSON(encryptToSelfMessageJSON);
            json.put("encryptToSelfMessage", encryptToSelfMessageJSON);
        }

    }

    final class UnencryptedEncryptToSelfMessage extends EncryptToSelfMessage implements Encryptable {

        private final byte[] messageToEncrypt;

        UnencryptedEncryptToSelfMessage(JSONObject attachmentData) {
            super(attachmentData);
            setEncryptedData(null);
            JSONObject encryptedMessageJSON = (JSONObject)attachmentData.get("encryptToSelfMessage");
            String messageToEncryptString = (String)encryptedMessageJSON.get("messageToEncrypt");
            messageToEncrypt = isText() ? Convert.toBytes(messageToEncryptString) : Convert.parseHexString(messageToEncryptString);
        }

        public UnencryptedEncryptToSelfMessage(byte[] messageToEncrypt, boolean isText, boolean isCompressed) {
            super(null, isText, isCompressed);
            this.messageToEncrypt = messageToEncrypt;
        }

        @Override
        int getMySize() {
            if (getEncryptedData() != null) {
                return super.getMySize();
            }
            return 4 + EncryptedData.getEncryptedSize(getPlaintext());
        }

        @Override
        void putMyBytes(ByteBuffer buffer) {
            if (getEncryptedData() == null) {
                throw new NxtException.NotYetEncryptedException("Message not yet encrypted");
            }
            super.putMyBytes(buffer);
        }

        @Override
        void putMyJSON(JSONObject json) {
            if (getEncryptedData() == null) {
                JSONObject encryptedMessageJSON = new JSONObject();
                encryptedMessageJSON.put("messageToEncrypt", isText() ? Convert.toString(messageToEncrypt) : Convert.toHexString(messageToEncrypt));
                encryptedMessageJSON.put("isText", isText());
                encryptedMessageJSON.put("isCompressed", isCompressed());
                json.put("encryptToSelfMessage", encryptedMessageJSON);
            } else {
                super.putMyJSON(json);
            }
        }

        @Override
        void apply(Transaction transaction, Account senderAccount, Account recipientAccount) {
            if (getEncryptedData() == null) {
                throw new NxtException.NotYetEncryptedException("Message not yet encrypted");
            }
            super.apply(transaction, senderAccount, recipientAccount);
        }

        @Override
        public void encrypt(String secretPhrase) {
            setEncryptedData(EncryptedData.encrypt(getPlaintext(), secretPhrase, Crypto.getPublicKey(secretPhrase)));
        }

        @Override
        int getEncryptedDataLength() {
            return EncryptedData.getEncryptedDataLength(getPlaintext());
        }

        private byte[] getPlaintext() {
            return isCompressed() && messageToEncrypt.length > 0 ? Convert.compress(messageToEncrypt) : messageToEncrypt;
        }

    }

    final class PublicKeyAnnouncement extends AbstractAppendix {

        private static final String appendixName = "PublicKeyAnnouncement";

        static PublicKeyAnnouncement parse(JSONObject attachmentData) {
            if (!hasAppendix(appendixName, attachmentData)) {
                return null;
            }
            return new PublicKeyAnnouncement(attachmentData);
        }

        private final byte[] publicKey;

        PublicKeyAnnouncement(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
            this.publicKey = new byte[32];
            buffer.get(this.publicKey);
        }

        private PublicKeyAnnouncement(JSONObject attachmentData) {
            super(attachmentData);
            this.publicKey = Convert.parseHexString((String)attachmentData.get("recipientPublicKey"));
        }

        public PublicKeyAnnouncement(byte[] publicKey) {
            this.publicKey = publicKey;
        }

        @Override
        String getAppendixName() {
            return appendixName;
        }

        @Override
        int getMySize() {
            return 32;
        }

        @Override
        void putMyBytes(ByteBuffer buffer) {
            buffer.put(publicKey);
        }

        @Override
        void putMyJSON(JSONObject json) {
            json.put("recipientPublicKey", Convert.toHexString(publicKey));
        }

        @Override
        void validate(Transaction transaction) throws NxtException.ValidationException {
            if (transaction.getRecipientId() == 0) {
                throw new NxtException.NotValidException("PublicKeyAnnouncement cannot be attached to transactions with no recipient");
            }
            if (!Crypto.isCanonicalPublicKey(publicKey)) {
                throw new NxtException.NotValidException("Invalid recipient public key: " + Convert.toHexString(publicKey));
            }
            long recipientId = transaction.getRecipientId();
            if (Account.getId(this.publicKey) != recipientId) {
                throw new NxtException.NotValidException("Announced public key does not match recipient accountId");
            }
            byte[] recipientPublicKey = Account.getPublicKey(recipientId);
            if (recipientPublicKey != null && ! Arrays.equals(publicKey, recipientPublicKey)) {
                throw new NxtException.NotCurrentlyValidException("A different public key for this account has already been announced");
            }
        }

        @Override
        void apply(Transaction transaction, Account senderAccount, Account recipientAccount) {
            if (Account.setOrVerify(recipientAccount.getId(), publicKey)) {
                recipientAccount.apply(this.publicKey);
            }
        }

        @Override
        boolean isPhasable() {
            return false;
        }

        public byte[] getPublicKey() {
            return publicKey;
        }

    }

    final class Phasing extends AbstractAppendix {

        private static final String appendixName = "Phasing";

        private static final Fee PHASING_FEE = (transaction, appendage) -> {
            long fee = 0;
            Phasing phasing = (Phasing)appendage;
            if (!phasing.params.getVoteWeighting().isBalanceIndependent()) {
                fee += 20 * Constants.ONE_NXT;
            } else {
                fee += Constants.ONE_NXT;
            }
            if (phasing.hashedSecret.length > 0) {
                fee += (1 + (phasing.hashedSecret.length - 1) / 32) * Constants.ONE_NXT;
            }
            fee += Constants.ONE_NXT * phasing.linkedFullHashes.length;
            return fee;
        };

        static Phasing parse(JSONObject attachmentData) {
            if (!hasAppendix(appendixName, attachmentData)) {
                return null;
            }
            return new Phasing(attachmentData);
        }

        private final int finishHeight;
        private final PhasingParams params;
        private final byte[][] linkedFullHashes;
        private final byte[] hashedSecret;
        private final byte algorithm;

        Phasing(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
            finishHeight = buffer.getInt();
            params = new PhasingParams(buffer);
            
            byte linkedFullHashesSize = buffer.get();
            if (linkedFullHashesSize > 0) {
                linkedFullHashes = new byte[linkedFullHashesSize][];
                for (int i = 0; i < linkedFullHashesSize; i++) {
                    linkedFullHashes[i] = new byte[32];
                    buffer.get(linkedFullHashes[i]);
                }
            } else {
                linkedFullHashes = Convert.EMPTY_BYTES;
            }
            byte hashedSecretLength = buffer.get();
            if (hashedSecretLength > 0) {
                hashedSecret = new byte[hashedSecretLength];
                buffer.get(hashedSecret);
            } else {
                hashedSecret = Convert.EMPTY_BYTE;
            }
            algorithm = buffer.get();
        }

        private Phasing(JSONObject attachmentData) {
            super(attachmentData);
            finishHeight = ((Long) attachmentData.get("phasingFinishHeight")).intValue();
            params = new PhasingParams(attachmentData);
            JSONArray linkedFullHashesJson = (JSONArray) attachmentData.get("phasingLinkedFullHashes");
            if (linkedFullHashesJson != null && linkedFullHashesJson.size() > 0) {
                linkedFullHashes = new byte[linkedFullHashesJson.size()][];
                for (int i = 0; i < linkedFullHashes.length; i++) {
                    linkedFullHashes[i] = Convert.parseHexString((String) linkedFullHashesJson.get(i));
                }
            } else {
                linkedFullHashes = Convert.EMPTY_BYTES;
            }
            String hashedSecret = Convert.emptyToNull((String)attachmentData.get("phasingHashedSecret"));
            if (hashedSecret != null) {
                this.hashedSecret = Convert.parseHexString(hashedSecret);
                this.algorithm = ((Long) attachmentData.get("phasingHashedSecretAlgorithm")).byteValue();
            } else {
                this.hashedSecret = Convert.EMPTY_BYTE;
                this.algorithm = 0;
            }
        }

        public Phasing(int finishHeight, PhasingParams phasingParams, byte[][] linkedFullHashes, byte[] hashedSecret, byte algorithm) {
            this.finishHeight = finishHeight;
            this.params = phasingParams;
            this.linkedFullHashes = Convert.nullToEmpty(linkedFullHashes);
            this.hashedSecret = hashedSecret != null ? hashedSecret : Convert.EMPTY_BYTE;
            this.algorithm = algorithm;
        }

        @Override
        String getAppendixName() {
            return appendixName;
        }

        @Override
        int getMySize() {
            return 4 + params.getMySize() + 1 + 32 * linkedFullHashes.length + 1 + hashedSecret.length + 1;
        }

        @Override
        void putMyBytes(ByteBuffer buffer) {
            buffer.putInt(finishHeight);
            params.putMyBytes(buffer);
            buffer.put((byte) linkedFullHashes.length);
            for (byte[] hash : linkedFullHashes) {
                buffer.put(hash);
            }
            buffer.put((byte)hashedSecret.length);
            buffer.put(hashedSecret);
            buffer.put(algorithm);
        }

        @Override
        void putMyJSON(JSONObject json) {
            json.put("phasingFinishHeight", finishHeight);
            params.putMyJSON(json);
            if (linkedFullHashes.length > 0) {
                JSONArray linkedFullHashesJson = new JSONArray();
                for (byte[] hash : linkedFullHashes) {
                    linkedFullHashesJson.add(Convert.toHexString(hash));
                }
                json.put("phasingLinkedFullHashes", linkedFullHashesJson);
            }
            if (hashedSecret.length > 0) {
                json.put("phasingHashedSecret", Convert.toHexString(hashedSecret));
                json.put("phasingHashedSecretAlgorithm", algorithm);
            }
        }

        @Override
        void validate(Transaction transaction) throws NxtException.ValidationException {
            params.validate();
            int currentHeight = Nxt.getBlockchain().getHeight();
            if (params.getVoteWeighting().getVotingModel() == VoteWeighting.VotingModel.TRANSACTION) {
                if (linkedFullHashes.length == 0 || linkedFullHashes.length > Constants.MAX_PHASING_LINKED_TRANSACTIONS) {
                    throw new NxtException.NotValidException("Invalid number of linkedFullHashes " + linkedFullHashes.length);
                }
                Set<Long> linkedTransactionIds = new HashSet<>(linkedFullHashes.length);
                for (byte[] hash : linkedFullHashes) {
                    if (Convert.emptyToNull(hash) == null || hash.length != 32) {
                        throw new NxtException.NotValidException("Invalid linkedFullHash " + Convert.toHexString(hash));
                    }
                    if (Nxt.getBlockchain().getHeight() > Constants.SHUFFLING_BLOCK) {
                        if (!linkedTransactionIds.add(Convert.fullHashToId(hash))) {
                            throw new NxtException.NotValidException("Duplicate linked transaction ids");
                        }
                    }
                    TransactionImpl linkedTransaction = TransactionDb.findTransactionByFullHash(hash, currentHeight);
                    if (linkedTransaction != null) {
                        if (transaction.getTimestamp() - linkedTransaction.getTimestamp() > Constants.MAX_REFERENCED_TRANSACTION_TIMESPAN) {
                            throw new NxtException.NotValidException("Linked transaction cannot be more than 60 days older than the phased transaction");
                        }
                        if (linkedTransaction.getPhasing() != null) {
                            throw new NxtException.NotCurrentlyValidException("Cannot link to an already existing phased transaction");
                        }
                    }
                }
                if (params.getQuorum() > linkedFullHashes.length) {
                    throw new NxtException.NotValidException("Quorum of " + params.getQuorum() + " cannot be achieved in by-transaction voting with "
                            + linkedFullHashes.length + " linked full hashes only");
                }
            } else {
                if (linkedFullHashes.length != 0) {
                    throw new NxtException.NotValidException("LinkedFullHashes can only be used with VotingModel.TRANSACTION");
                }
            }

            if (params.getVoteWeighting().getVotingModel() == VoteWeighting.VotingModel.HASH) {
                if (params.getQuorum() != 1) {
                    throw new NxtException.NotValidException("Quorum must be 1 for by-hash voting");
                }
                if (hashedSecret.length == 0 || hashedSecret.length > Byte.MAX_VALUE) {
                    throw new NxtException.NotValidException("Invalid hashedSecret " + Convert.toHexString(hashedSecret));
                }
                if (PhasingPoll.getHashFunction(algorithm) == null) {
                    throw new NxtException.NotValidException("Invalid hashedSecretAlgorithm " + algorithm);
                }
            } else {
                if (hashedSecret.length != 0) {
                    throw new NxtException.NotValidException("HashedSecret can only be used with VotingModel.HASH");
                }
                if (algorithm != 0) {
                    throw new NxtException.NotValidException("HashedSecretAlgorithm can only be used with VotingModel.HASH");
                }
            }

            if (finishHeight <= currentHeight + (params.getVoteWeighting().acceptsVotes() ? 2 : 1)
                    || finishHeight >= currentHeight + Constants.MAX_PHASING_DURATION) {
                throw new NxtException.NotCurrentlyValidException("Invalid finish height " + finishHeight);
            }
        }

        @Override
        void validateAtFinish(Transaction transaction) throws NxtException.ValidationException {
            params.checkApprovable();
        }

        @Override
        void apply(Transaction transaction, Account senderAccount, Account recipientAccount) {
            PhasingPoll.addPoll(transaction, this);
        }

        @Override
        boolean isPhasable() {
            return false;
        }

        @Override
        public Fee getBaselineFee(Transaction transaction) {
            return PHASING_FEE;
        }

        private void release(TransactionImpl transaction) {
            Account senderAccount = Account.getAccount(transaction.getSenderId());
            Account recipientAccount = transaction.getRecipientId() == 0 ? null : Account.getAccount(transaction.getRecipientId());
            transaction.getAppendages().forEach(appendage -> {
                if (appendage.isPhasable()) {
                    appendage.apply(transaction, senderAccount, recipientAccount);
                }
            });
            TransactionProcessorImpl.getInstance().notifyListeners(Collections.singletonList(transaction), TransactionProcessor.Event.RELEASE_PHASED_TRANSACTION);
            Logger.logDebugMessage("Transaction " + transaction.getStringId() + " has been released");
        }

        void reject(TransactionImpl transaction) {
            Account senderAccount = Account.getAccount(transaction.getSenderId());
            transaction.getType().undoAttachmentUnconfirmed(transaction, senderAccount);
            senderAccount.addToUnconfirmedBalanceNQT(LedgerEvent.REJECT_PHASED_TRANSACTION, transaction.getId(),
                                                     transaction.getAmountNQT());
            TransactionProcessorImpl.getInstance()
                    .notifyListeners(Collections.singletonList(transaction), TransactionProcessor.Event.REJECT_PHASED_TRANSACTION);
            Logger.logDebugMessage("Transaction " + transaction.getStringId() + " has been rejected");
        }

        void countVotes(TransactionImpl transaction) {
            if (Nxt.getBlockchain().getHeight() > Constants.SHUFFLING_BLOCK && PhasingPoll.getResult(transaction.getId()) != null) {
                return;
            }
            PhasingPoll poll = PhasingPoll.getPoll(transaction.getId());
            long result = poll.countVotes();
            poll.finish(result);
            if (result >= poll.getQuorum()) {
                try {
                    release(transaction);
                } catch (RuntimeException e) {
                    Logger.logErrorMessage("Failed to release phased transaction " + transaction.getJSONObject().toJSONString(), e);
                    reject(transaction);
                }
            } else {
                reject(transaction);
            }
        }

        void tryCountVotes(TransactionImpl transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            PhasingPoll poll = PhasingPoll.getPoll(transaction.getId());
            long result = poll.countVotes();
            if (result >= poll.getQuorum()) {
                if (!transaction.attachmentIsDuplicate(duplicates, false)) {
                    try {
                        release(transaction);
                        poll.finish(result);
                        Logger.logDebugMessage("Early finish of transaction " + transaction.getStringId() + " at height " + Nxt.getBlockchain().getHeight());
                    } catch (RuntimeException e) {
                        Logger.logErrorMessage("Failed to release phased transaction " + transaction.getJSONObject().toJSONString(), e);
                    }
                } else {
                    Logger.logDebugMessage("At height " + Nxt.getBlockchain().getHeight() + " phased transaction " + transaction.getStringId()
                            + " is duplicate, cannot finish early");
                }
            } else {
                Logger.logDebugMessage("At height " + Nxt.getBlockchain().getHeight() + " phased transaction " + transaction.getStringId()
                        + " does not yet meet quorum, cannot finish early");
            }
        }

        public int getFinishHeight() {
            return finishHeight;
        }

        public long getQuorum() {
            return params.getQuorum();
        }

        public long[] getWhitelist() {
            return params.getWhitelist();
        }

        public VoteWeighting getVoteWeighting() {
            return params.getVoteWeighting();
        }

        public byte[][] getLinkedFullHashes() {
            return linkedFullHashes;
        }

        public byte[] getHashedSecret() {
            return hashedSecret;
        }

        public byte getAlgorithm() {
            return algorithm;
        }

        public PhasingParams getParams() {
            return params;
        }
    }
}
