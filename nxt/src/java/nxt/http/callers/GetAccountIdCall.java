// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAccountIdCall extends APICall.Builder<GetAccountIdCall> {
    private GetAccountIdCall() {
        super(ApiSpec.getAccountId);
    }

    public static GetAccountIdCall create() {
        return new GetAccountIdCall();
    }

    public GetAccountIdCall publicKey(String publicKey) {
        return param("publicKey", publicKey);
    }

    public GetAccountIdCall publicKey(byte[] publicKey) {
        return param("publicKey", publicKey);
    }

    public GetAccountIdCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }
}
