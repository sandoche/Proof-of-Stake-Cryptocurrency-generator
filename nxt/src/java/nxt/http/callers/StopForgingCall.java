// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class StopForgingCall extends APICall.Builder<StopForgingCall> {
    private StopForgingCall() {
        super(ApiSpec.stopForging);
    }

    public static StopForgingCall create() {
        return new StopForgingCall();
    }

    public StopForgingCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public StopForgingCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
