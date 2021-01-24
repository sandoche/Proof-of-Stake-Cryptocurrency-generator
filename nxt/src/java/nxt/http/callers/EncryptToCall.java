// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class EncryptToCall extends APICall.Builder<EncryptToCall> {
    private EncryptToCall() {
        super(ApiSpec.encryptTo);
    }

    public static EncryptToCall create() {
        return new EncryptToCall();
    }

    public EncryptToCall compressMessageToEncrypt(String compressMessageToEncrypt) {
        return param("compressMessageToEncrypt", compressMessageToEncrypt);
    }

    public EncryptToCall recipient(String recipient) {
        return param("recipient", recipient);
    }

    public EncryptToCall recipient(long recipient) {
        return unsignedLongParam("recipient", recipient);
    }

    public EncryptToCall messageToEncryptIsText(boolean messageToEncryptIsText) {
        return param("messageToEncryptIsText", messageToEncryptIsText);
    }

    public EncryptToCall messageToEncrypt(String messageToEncrypt) {
        return param("messageToEncrypt", messageToEncrypt);
    }

    public EncryptToCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }
}
