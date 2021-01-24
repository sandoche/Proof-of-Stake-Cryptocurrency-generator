// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetDGSPurchasesCall extends APICall.Builder<GetDGSPurchasesCall> {
    private GetDGSPurchasesCall() {
        super(ApiSpec.getDGSPurchases);
    }

    public static GetDGSPurchasesCall create() {
        return new GetDGSPurchasesCall();
    }

    public GetDGSPurchasesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetDGSPurchasesCall seller(String seller) {
        return param("seller", seller);
    }

    public GetDGSPurchasesCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetDGSPurchasesCall completed(String completed) {
        return param("completed", completed);
    }

    public GetDGSPurchasesCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetDGSPurchasesCall withPublicFeedbacksOnly(String withPublicFeedbacksOnly) {
        return param("withPublicFeedbacksOnly", withPublicFeedbacksOnly);
    }

    public GetDGSPurchasesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetDGSPurchasesCall buyer(String buyer) {
        return param("buyer", buyer);
    }

    public GetDGSPurchasesCall buyer(long buyer) {
        return unsignedLongParam("buyer", buyer);
    }
}
