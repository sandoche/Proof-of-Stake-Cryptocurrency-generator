// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAssetDividendsCall extends APICall.Builder<GetAssetDividendsCall> {
    private GetAssetDividendsCall() {
        super(ApiSpec.getAssetDividends);
    }

    public static GetAssetDividendsCall create() {
        return new GetAssetDividendsCall();
    }

    public GetAssetDividendsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAssetDividendsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAssetDividendsCall includeHoldingInfo(boolean includeHoldingInfo) {
        return param("includeHoldingInfo", includeHoldingInfo);
    }

    public GetAssetDividendsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAssetDividendsCall asset(String asset) {
        return param("asset", asset);
    }

    public GetAssetDividendsCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public GetAssetDividendsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAssetDividendsCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }
}
