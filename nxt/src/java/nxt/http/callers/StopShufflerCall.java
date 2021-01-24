// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class StopShufflerCall extends APICall.Builder<StopShufflerCall> {
    private StopShufflerCall() {
        super(ApiSpec.stopShuffler);
    }

    public static StopShufflerCall create() {
        return new StopShufflerCall();
    }

    public StopShufflerCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public StopShufflerCall account(String account) {
        return param("account", account);
    }

    public StopShufflerCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public StopShufflerCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }

    public StopShufflerCall shufflingFullHash(String shufflingFullHash) {
        return param("shufflingFullHash", shufflingFullHash);
    }

    public StopShufflerCall shufflingFullHash(byte[] shufflingFullHash) {
        return param("shufflingFullHash", shufflingFullHash);
    }
}
