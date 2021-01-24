// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetDGSGoodCall extends APICall.Builder<GetDGSGoodCall> {
    private GetDGSGoodCall() {
        super(ApiSpec.getDGSGood);
    }

    public static GetDGSGoodCall create() {
        return new GetDGSGoodCall();
    }

    public GetDGSGoodCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetDGSGoodCall includeCounts(boolean includeCounts) {
        return param("includeCounts", includeCounts);
    }

    public GetDGSGoodCall goods(String goods) {
        return param("goods", goods);
    }

    public GetDGSGoodCall goods(long goods) {
        return unsignedLongParam("goods", goods);
    }

    public GetDGSGoodCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
