// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAccountCurrencyCountCall extends APICall.Builder<GetAccountCurrencyCountCall> {
    private GetAccountCurrencyCountCall() {
        super(ApiSpec.getAccountCurrencyCount);
    }

    public static GetAccountCurrencyCountCall create() {
        return new GetAccountCurrencyCountCall();
    }

    public GetAccountCurrencyCountCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAccountCurrencyCountCall account(String account) {
        return param("account", account);
    }

    public GetAccountCurrencyCountCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetAccountCurrencyCountCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAccountCurrencyCountCall height(int height) {
        return param("height", height);
    }
}
