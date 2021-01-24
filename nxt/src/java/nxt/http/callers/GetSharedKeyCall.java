// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetSharedKeyCall extends APICall.Builder<GetSharedKeyCall> {
    private GetSharedKeyCall() {
        super(ApiSpec.getSharedKey);
    }

    public static GetSharedKeyCall create() {
        return new GetSharedKeyCall();
    }

    public GetSharedKeyCall nonce(String nonce) {
        return param("nonce", nonce);
    }

    public GetSharedKeyCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public GetSharedKeyCall account(String account) {
        return param("account", account);
    }

    public GetSharedKeyCall account(long account) {
        return unsignedLongParam("account", account);
    }
}
