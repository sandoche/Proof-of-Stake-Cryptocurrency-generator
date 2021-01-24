// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAssetAccountCountCall extends APICall.Builder<GetAssetAccountCountCall> {
    private GetAssetAccountCountCall() {
        super(ApiSpec.getAssetAccountCount);
    }

    public static GetAssetAccountCountCall create() {
        return new GetAssetAccountCountCall();
    }

    public GetAssetAccountCountCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAssetAccountCountCall asset(String asset) {
        return param("asset", asset);
    }

    public GetAssetAccountCountCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public GetAssetAccountCountCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAssetAccountCountCall height(int height) {
        return param("height", height);
    }
}
