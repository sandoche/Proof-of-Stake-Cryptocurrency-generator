// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetForgingCall extends APICall.Builder<GetForgingCall> {
    private GetForgingCall() {
        super(ApiSpec.getForging);
    }

    public static GetForgingCall create() {
        return new GetForgingCall();
    }

    public GetForgingCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public GetForgingCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
