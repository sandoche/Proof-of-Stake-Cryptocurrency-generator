// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAssetCall extends APICall.Builder<GetAssetCall> {
    private GetAssetCall() {
        super(ApiSpec.getAsset);
    }

    public static GetAssetCall create() {
        return new GetAssetCall();
    }

    public GetAssetCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAssetCall includeCounts(boolean includeCounts) {
        return param("includeCounts", includeCounts);
    }

    public GetAssetCall asset(String asset) {
        return param("asset", asset);
    }

    public GetAssetCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public GetAssetCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
