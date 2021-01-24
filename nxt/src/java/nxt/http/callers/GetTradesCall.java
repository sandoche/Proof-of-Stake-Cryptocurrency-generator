// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetTradesCall extends APICall.Builder<GetTradesCall> {
    private GetTradesCall() {
        super(ApiSpec.getTrades);
    }

    public static GetTradesCall create() {
        return new GetTradesCall();
    }

    public GetTradesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetTradesCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetTradesCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetTradesCall includeAssetInfo(boolean includeAssetInfo) {
        return param("includeAssetInfo", includeAssetInfo);
    }

    public GetTradesCall asset(String asset) {
        return param("asset", asset);
    }

    public GetTradesCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public GetTradesCall account(String account) {
        return param("account", account);
    }

    public GetTradesCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetTradesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetTradesCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }
}
