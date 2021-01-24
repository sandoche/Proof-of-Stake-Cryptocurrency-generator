// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetCurrencyIdsCall extends APICall.Builder<GetCurrencyIdsCall> {
    private GetCurrencyIdsCall() {
        super(ApiSpec.getCurrencyIds);
    }

    public static GetCurrencyIdsCall create() {
        return new GetCurrencyIdsCall();
    }

    public GetCurrencyIdsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetCurrencyIdsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetCurrencyIdsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetCurrencyIdsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
