// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetDGSExpiredPurchasesCall extends APICall.Builder<GetDGSExpiredPurchasesCall> {
    private GetDGSExpiredPurchasesCall() {
        super(ApiSpec.getDGSExpiredPurchases);
    }

    public static GetDGSExpiredPurchasesCall create() {
        return new GetDGSExpiredPurchasesCall();
    }

    public GetDGSExpiredPurchasesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetDGSExpiredPurchasesCall seller(String seller) {
        return param("seller", seller);
    }

    public GetDGSExpiredPurchasesCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetDGSExpiredPurchasesCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetDGSExpiredPurchasesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
