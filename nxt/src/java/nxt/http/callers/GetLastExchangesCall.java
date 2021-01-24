// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetLastExchangesCall extends APICall.Builder<GetLastExchangesCall> {
    private GetLastExchangesCall() {
        super(ApiSpec.getLastExchanges);
    }

    public static GetLastExchangesCall create() {
        return new GetLastExchangesCall();
    }

    public GetLastExchangesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetLastExchangesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetLastExchangesCall currencies(String... currencies) {
        return param("currencies", currencies);
    }
}
