// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetDGSPurchaseCountCall extends APICall.Builder<GetDGSPurchaseCountCall> {
    private GetDGSPurchaseCountCall() {
        super(ApiSpec.getDGSPurchaseCount);
    }

    public static GetDGSPurchaseCountCall create() {
        return new GetDGSPurchaseCountCall();
    }

    public GetDGSPurchaseCountCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetDGSPurchaseCountCall seller(String seller) {
        return param("seller", seller);
    }

    public GetDGSPurchaseCountCall completed(String completed) {
        return param("completed", completed);
    }

    public GetDGSPurchaseCountCall withPublicFeedbacksOnly(String withPublicFeedbacksOnly) {
        return param("withPublicFeedbacksOnly", withPublicFeedbacksOnly);
    }

    public GetDGSPurchaseCountCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetDGSPurchaseCountCall buyer(String buyer) {
        return param("buyer", buyer);
    }

    public GetDGSPurchaseCountCall buyer(long buyer) {
        return unsignedLongParam("buyer", buyer);
    }
}
