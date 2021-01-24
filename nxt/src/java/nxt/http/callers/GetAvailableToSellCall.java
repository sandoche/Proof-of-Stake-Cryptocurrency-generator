// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAvailableToSellCall extends APICall.Builder<GetAvailableToSellCall> {
    private GetAvailableToSellCall() {
        super(ApiSpec.getAvailableToSell);
    }

    public static GetAvailableToSellCall create() {
        return new GetAvailableToSellCall();
    }

    public GetAvailableToSellCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAvailableToSellCall currency(String currency) {
        return param("currency", currency);
    }

    public GetAvailableToSellCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public GetAvailableToSellCall units(long units) {
        return param("units", units);
    }

    public GetAvailableToSellCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
