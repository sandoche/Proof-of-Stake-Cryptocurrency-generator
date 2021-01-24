// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAssetTransfersCall extends APICall.Builder<GetAssetTransfersCall> {
    private GetAssetTransfersCall() {
        super(ApiSpec.getAssetTransfers);
    }

    public static GetAssetTransfersCall create() {
        return new GetAssetTransfersCall();
    }

    public GetAssetTransfersCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAssetTransfersCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAssetTransfersCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAssetTransfersCall includeAssetInfo(boolean includeAssetInfo) {
        return param("includeAssetInfo", includeAssetInfo);
    }

    public GetAssetTransfersCall asset(String asset) {
        return param("asset", asset);
    }

    public GetAssetTransfersCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public GetAssetTransfersCall account(String account) {
        return param("account", account);
    }

    public GetAssetTransfersCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetAssetTransfersCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAssetTransfersCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }
}
