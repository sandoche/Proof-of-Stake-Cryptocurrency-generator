// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAllCurrenciesCall extends APICall.Builder<GetAllCurrenciesCall> {
    private GetAllCurrenciesCall() {
        super(ApiSpec.getAllCurrencies);
    }

    public static GetAllCurrenciesCall create() {
        return new GetAllCurrenciesCall();
    }

    public GetAllCurrenciesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAllCurrenciesCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAllCurrenciesCall includeCounts(boolean includeCounts) {
        return param("includeCounts", includeCounts);
    }

    public GetAllCurrenciesCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAllCurrenciesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
