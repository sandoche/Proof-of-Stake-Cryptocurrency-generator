// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetExpectedAssetDeletesCall extends APICall.Builder<GetExpectedAssetDeletesCall> {
    private GetExpectedAssetDeletesCall() {
        super(ApiSpec.getExpectedAssetDeletes);
    }

    public static GetExpectedAssetDeletesCall create() {
        return new GetExpectedAssetDeletesCall();
    }

    public GetExpectedAssetDeletesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetExpectedAssetDeletesCall includeAssetInfo(boolean includeAssetInfo) {
        return param("includeAssetInfo", includeAssetInfo);
    }

    public GetExpectedAssetDeletesCall asset(String asset) {
        return param("asset", asset);
    }

    public GetExpectedAssetDeletesCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public GetExpectedAssetDeletesCall account(String account) {
        return param("account", account);
    }

    public GetExpectedAssetDeletesCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetExpectedAssetDeletesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
