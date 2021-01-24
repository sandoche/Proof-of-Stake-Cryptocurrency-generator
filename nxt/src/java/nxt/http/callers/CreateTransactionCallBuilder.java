// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class CreateTransactionCallBuilder<T extends APICall.Builder> extends APICall.Builder<T> {
    protected CreateTransactionCallBuilder(ApiSpec apiSpec) {
        super(apiSpec);
    }

    public T broadcast(boolean broadcast) {
        return param("broadcast", broadcast);
    }

    public T phasingQuorum(long phasingQuorum) {
        return param("phasingQuorum", phasingQuorum);
    }

    public T encryptedMessageIsPrunable(boolean encryptedMessageIsPrunable) {
        return param("encryptedMessageIsPrunable", encryptedMessageIsPrunable);
    }

    public T messageIsPrunable(boolean messageIsPrunable) {
        return param("messageIsPrunable", messageIsPrunable);
    }

    public T compressMessageToEncryptToSelf(String compressMessageToEncryptToSelf) {
        return param("compressMessageToEncryptToSelf", compressMessageToEncryptToSelf);
    }

    public T phasingLinkedFullHash(String... phasingLinkedFullHash) {
        return param("phasingLinkedFullHash", phasingLinkedFullHash);
    }

    public T phasingLinkedFullHash(byte[]... phasingLinkedFullHash) {
        return param("phasingLinkedFullHash", phasingLinkedFullHash);
    }

    public T publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public T publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public T secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public T ecBlockId(String ecBlockId) {
        return param("ecBlockId", ecBlockId);
    }

    public T ecBlockId(long ecBlockId) {
        return unsignedLongParam("ecBlockId", ecBlockId);
    }

    public T phased(boolean phased) {
        return param("phased", phased);
    }

    public T phasingHashedSecret(String phasingHashedSecret) {
        return param("phasingHashedSecret", phasingHashedSecret);
    }

    public T phasingHolding(String phasingHolding) {
        return param("phasingHolding", phasingHolding);
    }

    public T phasingHolding(long phasingHolding) {
        return unsignedLongParam("phasingHolding", phasingHolding);
    }

    public T encryptedMessageData(String encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public T encryptedMessageData(byte[] encryptedMessageData) {
        return param("encryptedMessageData", encryptedMessageData);
    }

    public T messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public T messageToEncryptToSelf(String messageToEncryptToSelf) {
        return param("messageToEncryptToSelf", messageToEncryptToSelf);
    }

    public T messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public T messageIsText(boolean messageIsText) {
        return param("messageIsText", messageIsText);
    }

    public T phasingMinBalance(long phasingMinBalance) {
        return param("phasingMinBalance", phasingMinBalance);
    }

    public T deadline(int deadline) {
        return param("deadline", deadline);
    }

    public T phasingWhitelisted(String... phasingWhitelisted) {
        return param("phasingWhitelisted", phasingWhitelisted);
    }

    public T messageToEncryptToSelfIsText(boolean messageToEncryptToSelfIsText) {
        return param("messageToEncryptToSelfIsText", messageToEncryptToSelfIsText);
    }

    public T recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public T recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public T feeNQT(long feeNQT) {
        return param("feeNQT", feeNQT);
    }

    public T encryptedMessageNonce(String encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public T encryptedMessageNonce(byte[] encryptedMessageNonce) {
        return param("encryptedMessageNonce", encryptedMessageNonce);
    }

    public T message(String message) {
        return param("message", message);
    }

    public T encryptToSelfMessageData(String encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public T encryptToSelfMessageData(byte[] encryptToSelfMessageData) {
        return param("encryptToSelfMessageData", encryptToSelfMessageData);
    }

    public T encryptToSelfMessageNonce(String encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public T encryptToSelfMessageNonce(byte[] encryptToSelfMessageNonce) {
        return param("encryptToSelfMessageNonce", encryptToSelfMessageNonce);
    }

    public T phasingFinishHeight(int phasingFinishHeight) {
        return param("phasingFinishHeight", phasingFinishHeight);
    }

    public T referencedTransactionFullHash(String referencedTransactionFullHash) {
        return param("referencedTransactionFullHash", referencedTransactionFullHash);
    }

    public T referencedTransactionFullHash(byte[] referencedTransactionFullHash) {
        return param("referencedTransactionFullHash", referencedTransactionFullHash);
    }

    public T compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public T phasingHashedSecretAlgorithm(byte phasingHashedSecretAlgorithm) {
        return param("phasingHashedSecretAlgorithm", phasingHashedSecretAlgorithm);
    }

    public T ecBlockHeight(int ecBlockHeight) {
        return param("ecBlockHeight", ecBlockHeight);
    }

    public T phasingMinBalanceModel(byte phasingMinBalanceModel) {
        return param("phasingMinBalanceModel", phasingMinBalanceModel);
    }

    public T phasingVotingModel(byte phasingVotingModel) {
        return param("phasingVotingModel", phasingVotingModel);
    }
}
