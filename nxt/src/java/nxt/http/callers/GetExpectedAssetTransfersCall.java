// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetExpectedAssetTransfersCall extends APICall.Builder<GetExpectedAssetTransfersCall> {
    private GetExpectedAssetTransfersCall() {
        super(ApiSpec.getExpectedAssetTransfers);
    }

    public static GetExpectedAssetTransfersCall create() {
        return new GetExpectedAssetTransfersCall();
    }

    public GetExpectedAssetTransfersCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetExpectedAssetTransfersCall includeAssetInfo(boolean includeAssetInfo) {
        return param("includeAssetInfo", includeAssetInfo);
    }

    public GetExpectedAssetTransfersCall asset(String asset) {
        return param("asset", asset);
    }

    public GetExpectedAssetTransfersCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public GetExpectedAssetTransfersCall account(String account) {
        return param("account", account);
    }

    public GetExpectedAssetTransfersCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetExpectedAssetTransfersCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
