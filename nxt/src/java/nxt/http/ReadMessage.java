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

package nxt.http;

import nxt.Account;
import nxt.Appendix;
import nxt.Nxt;
import nxt.PrunableMessage;
import nxt.Transaction;
import nxt.crypto.Crypto;
import nxt.crypto.EncryptedData;
import nxt.util.Convert;
import nxt.util.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

import static nxt.http.JSONResponses.NO_MESSAGE;
import static nxt.http.JSONResponses.PRUNED_TRANSACTION;
import static nxt.http.JSONResponses.UNKNOWN_TRANSACTION;

public final class ReadMessage extends APIServlet.APIRequestHandler {

    static final ReadMessage instance = new ReadMessage();

    private ReadMessage() {
        super(new APITag[] {APITag.MESSAGES}, "transaction", "secretPhrase", "sharedKey", "retrieve");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {

        long transactionId = ParameterParser.getUnsignedLong(req, "transaction", true);
        boolean retrieve = "true".equalsIgnoreCase(req.getParameter("retrieve"));
        Transaction transaction = Nxt.getBlockchain().getTransaction(transactionId);
        if (transaction == null) {
            return UNKNOWN_TRANSACTION;
        }
        PrunableMessage prunableMessage = PrunableMessage.getPrunableMessage(transactionId);
        if (prunableMessage == null && (transaction.getPrunablePlainMessage() != null || transaction.getPrunableEncryptedMessage() != null) && retrieve) {
            if (Nxt.getBlockchainProcessor().restorePrunedTransaction(transactionId) == null) {
                return PRUNED_TRANSACTION;
            }
            prunableMessage = PrunableMessage.getPrunableMessage(transactionId);
        }

        JSONObject response = new JSONObject();
        Appendix.Message message = transaction.getMessage();
        Appendix.EncryptedMessage encryptedMessage = transaction.getEncryptedMessage();
        Appendix.EncryptToSelfMessage encryptToSelfMessage = transaction.getEncryptToSelfMessage();
        if (message == null && encryptedMessage == null && encryptToSelfMessage == null && prunableMessage == null) {
            return NO_MESSAGE;
        }
        if (message != null) {
            response.put("message", Convert.toString(message.getMessage(), message.isText()));
            response.put("messageIsPrunable", false);
        } else if (prunableMessage != null && prunableMessage.getMessage() != null) {
            response.put("message", Convert.toString(prunableMessage.getMessage(), prunableMessage.messageIsText()));
            response.put("messageIsPrunable", true);
        }
        String secretPhrase = ParameterParser.getSecretPhrase(req, false);
        byte[] sharedKey = ParameterParser.getBytes(req, "sharedKey", false);
        if (sharedKey.length != 0 && secretPhrase != null) {
            return JSONResponses.either("secretPhrase", "sharedKey");
        }
        if (secretPhrase != null || sharedKey.length > 0) {
            EncryptedData encryptedData = null;
            boolean isText = false;
            boolean uncompress = true;
            if (encryptedMessage != null) {
                encryptedData = encryptedMessage.getEncryptedData();
                isText = encryptedMessage.isText();
                uncompress = encryptedMessage.isCompressed();
                response.put("encryptedMessageIsPrunable", false);
            } else if (prunableMessage != null && prunableMessage.getEncryptedData() != null) {
                encryptedData = prunableMessage.getEncryptedData();
                isText = prunableMessage.encryptedMessageIsText();
                uncompress = prunableMessage.isCompressed();
                response.put("encryptedMessageIsPrunable", true);
            }
            if (encryptedData != null) {
                try {
                    byte[] decrypted = null;
                    if (secretPhrase != null) {
                        byte[] readerPublicKey = Crypto.getPublicKey(secretPhrase);
                        byte[] senderPublicKey = Account.getPublicKey(transaction.getSenderId());
                        byte[] recipientPublicKey = Account.getPublicKey(transaction.getRecipientId());
                        byte[] publicKey = Arrays.equals(senderPublicKey, readerPublicKey) ? recipientPublicKey : senderPublicKey;
                        if (publicKey != null) {
                            decrypted = Account.decryptFrom(publicKey, encryptedData, secretPhrase, uncompress);
                        }
                    } else {
                        decrypted = Crypto.aesDecrypt(encryptedData.getData(), sharedKey);
                        if (uncompress) {
                            decrypted = Convert.uncompress(decrypted);
                        }
                    }
                    response.put("decryptedMessage", Convert.toString(decrypted, isText));
                } catch (RuntimeException e) {
                    Logger.logDebugMessage("Decryption of message to recipient failed: " + e.toString());
                    JSONData.putException(response, e, "Wrong secretPhrase or sharedKey");
                }
            }
            if (encryptToSelfMessage != null && secretPhrase != null) {
                byte[] publicKey = Crypto.getPublicKey(secretPhrase);
                try {
                    byte[] decrypted = Account.decryptFrom(publicKey, encryptToSelfMessage.getEncryptedData(), secretPhrase, encryptToSelfMessage.isCompressed());
                    response.put("decryptedMessageToSelf", Convert.toString(decrypted, encryptToSelfMessage.isText()));
                } catch (RuntimeException e) {
                    Logger.logDebugMessage("Decryption of message to self failed: " + e.toString());
                }
            }
        }
        return response;
    }

}
