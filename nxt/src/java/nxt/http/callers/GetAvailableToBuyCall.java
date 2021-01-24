// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAvailableToBuyCall extends APICall.Builder<GetAvailableToBuyCall> {
    private GetAvailableToBuyCall() {
        super(ApiSpec.getAvailableToBuy);
    }

    public static GetAvailableToBuyCall create() {
        return new GetAvailableToBuyCall();
    }

    public GetAvailableToBuyCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAvailableToBuyCall currency(String currency) {
        return param("currency", currency);
    }

    public GetAvailableToBuyCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public GetAvailableToBuyCall units(long units) {
        return param("units", units);
    }

    public GetAvailableToBuyCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
