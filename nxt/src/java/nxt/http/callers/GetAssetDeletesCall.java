// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAssetDeletesCall extends APICall.Builder<GetAssetDeletesCall> {
    private GetAssetDeletesCall() {
        super(ApiSpec.getAssetDeletes);
    }

    public static GetAssetDeletesCall create() {
        return new GetAssetDeletesCall();
    }

    public GetAssetDeletesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAssetDeletesCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAssetDeletesCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAssetDeletesCall includeAssetInfo(boolean includeAssetInfo) {
        return param("includeAssetInfo", includeAssetInfo);
    }

    public GetAssetDeletesCall asset(String asset) {
        return param("asset", asset);
    }

    public GetAssetDeletesCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public GetAssetDeletesCall account(String account) {
        return param("account", account);
    }

    public GetAssetDeletesCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetAssetDeletesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAssetDeletesCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }
}
