// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAllExchangesCall extends APICall.Builder<GetAllExchangesCall> {
    private GetAllExchangesCall() {
        super(ApiSpec.getAllExchanges);
    }

    public static GetAllExchangesCall create() {
        return new GetAllExchangesCall();
    }

    public GetAllExchangesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAllExchangesCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAllExchangesCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAllExchangesCall includeCurrencyInfo(boolean includeCurrencyInfo) {
        return param("includeCurrencyInfo", includeCurrencyInfo);
    }

    public GetAllExchangesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAllExchangesCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }
}
