// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAccountAssetsCall extends APICall.Builder<GetAccountAssetsCall> {
    private GetAccountAssetsCall() {
        super(ApiSpec.getAccountAssets);
    }

    public static GetAccountAssetsCall create() {
        return new GetAccountAssetsCall();
    }

    public GetAccountAssetsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAccountAssetsCall includeAssetInfo(boolean includeAssetInfo) {
        return param("includeAssetInfo", includeAssetInfo);
    }

    public GetAccountAssetsCall asset(String asset) {
        return param("asset", asset);
    }

    public GetAccountAssetsCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public GetAccountAssetsCall account(String account) {
        return param("account", account);
    }

    public GetAccountAssetsCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetAccountAssetsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAccountAssetsCall height(int height) {
        return param("height", height);
    }
}
