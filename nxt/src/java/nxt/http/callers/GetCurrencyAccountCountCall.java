// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetCurrencyAccountCountCall extends APICall.Builder<GetCurrencyAccountCountCall> {
    private GetCurrencyAccountCountCall() {
        super(ApiSpec.getCurrencyAccountCount);
    }

    public static GetCurrencyAccountCountCall create() {
        return new GetCurrencyAccountCountCall();
    }

    public GetCurrencyAccountCountCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetCurrencyAccountCountCall currency(String currency) {
        return param("currency", currency);
    }

    public GetCurrencyAccountCountCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public GetCurrencyAccountCountCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetCurrencyAccountCountCall height(int height) {
        return param("height", height);
    }
}
