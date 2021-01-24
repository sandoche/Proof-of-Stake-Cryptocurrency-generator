// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAssetsCall extends APICall.Builder<GetAssetsCall> {
    private GetAssetsCall() {
        super(ApiSpec.getAssets);
    }

    public static GetAssetsCall create() {
        return new GetAssetsCall();
    }

    public GetAssetsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAssetsCall assets(String... assets) {
        return param("assets", assets);
    }

    public GetAssetsCall includeCounts(boolean includeCounts) {
        return param("includeCounts", includeCounts);
    }

    public GetAssetsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
