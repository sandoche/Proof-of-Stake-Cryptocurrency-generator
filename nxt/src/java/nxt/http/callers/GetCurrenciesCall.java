// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetCurrenciesCall extends APICall.Builder<GetCurrenciesCall> {
    private GetCurrenciesCall() {
        super(ApiSpec.getCurrencies);
    }

    public static GetCurrenciesCall create() {
        return new GetCurrenciesCall();
    }

    public GetCurrenciesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetCurrenciesCall includeCounts(boolean includeCounts) {
        return param("includeCounts", includeCounts);
    }

    public GetCurrenciesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetCurrenciesCall currencies(String... currencies) {
        return param("currencies", currencies);
    }
}
