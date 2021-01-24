// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetBidOrderCall extends APICall.Builder<GetBidOrderCall> {
    private GetBidOrderCall() {
        super(ApiSpec.getBidOrder);
    }

    public static GetBidOrderCall create() {
        return new GetBidOrderCall();
    }

    public GetBidOrderCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetBidOrderCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetBidOrderCall order(String order) {
        return param("order", order);
    }

    public GetBidOrderCall order(long order) {
        return unsignedLongParam("order", order);
    }
}
