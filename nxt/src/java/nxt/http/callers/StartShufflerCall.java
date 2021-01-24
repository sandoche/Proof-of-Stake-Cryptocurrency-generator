// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class StartShufflerCall extends APICall.Builder<StartShufflerCall> {
    private StartShufflerCall() {
        super(ApiSpec.startShuffler);
    }

    public static StartShufflerCall create() {
        return new StartShufflerCall();
    }

    public StartShufflerCall recipientSecretPhrase(String recipientSecretPhrase) {
        return param("recipientSecretPhrase", recipientSecretPhrase);
    }

    public StartShufflerCall recipientPublicKey(String recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public StartShufflerCall recipientPublicKey(byte[] recipientPublicKey) {
        return param("recipientPublicKey", recipientPublicKey);
    }

    public StartShufflerCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public StartShufflerCall shufflingFullHash(String shufflingFullHash) {
        return param("shufflingFullHash", shufflingFullHash);
    }

    public StartShufflerCall shufflingFullHash(byte[] shufflingFullHash) {
        return param("shufflingFullHash", shufflingFullHash);
    }
}
