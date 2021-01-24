// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAccountAssetCountCall extends APICall.Builder<GetAccountAssetCountCall> {
    private GetAccountAssetCountCall() {
        super(ApiSpec.getAccountAssetCount);
    }

    public static GetAccountAssetCountCall create() {
        return new GetAccountAssetCountCall();
    }

    public GetAccountAssetCountCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAccountAssetCountCall account(String account) {
        return param("account", account);
    }

    public GetAccountAssetCountCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetAccountAssetCountCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAccountAssetCountCall height(int height) {
        return param("height", height);
    }
}
