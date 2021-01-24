// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetDGSGoodsPurchaseCountCall extends APICall.Builder<GetDGSGoodsPurchaseCountCall> {
    private GetDGSGoodsPurchaseCountCall() {
        super(ApiSpec.getDGSGoodsPurchaseCount);
    }

    public static GetDGSGoodsPurchaseCountCall create() {
        return new GetDGSGoodsPurchaseCountCall();
    }

    public GetDGSGoodsPurchaseCountCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetDGSGoodsPurchaseCountCall goods(String goods) {
        return param("goods", goods);
    }

    public GetDGSGoodsPurchaseCountCall goods(long goods) {
        return unsignedLongParam("goods", goods);
    }

    public GetDGSGoodsPurchaseCountCall completed(String completed) {
        return param("completed", completed);
    }

    public GetDGSGoodsPurchaseCountCall withPublicFeedbacksOnly(String withPublicFeedbacksOnly) {
        return param("withPublicFeedbacksOnly", withPublicFeedbacksOnly);
    }

    public GetDGSGoodsPurchaseCountCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
