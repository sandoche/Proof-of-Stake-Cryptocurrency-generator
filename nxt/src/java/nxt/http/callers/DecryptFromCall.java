// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class DecryptFromCall extends APICall.Builder<DecryptFromCall> {
    private DecryptFromCall() {
        super(ApiSpec.decryptFrom);
    }

    public static DecryptFromCall create() {
        return new DecryptFromCall();
    }

    public DecryptFromCall decryptedMessageIsText(boolean decryptedMessageIsText) {
        return param("decryptedMessageIsText", decryptedMessageIsText);
    }

    public DecryptFromCall data(String data) {
        return param("data", data);
    }

    public DecryptFromCall uncompressDecryptedMessage(String uncompressDecryptedMessage) {
        return param("uncompressDecryptedMessage", uncompressDecryptedMessage);
    }

    public DecryptFromCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public DecryptFromCall nonce(String nonce) {
        return param("nonce", nonce);
    }

    public DecryptFromCall account(String account) {
        return param("account", account);
    }

    public DecryptFromCall account(long account) {
        return unsignedLongParam("account", account);
    }
}
