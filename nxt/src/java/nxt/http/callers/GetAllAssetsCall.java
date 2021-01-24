// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAllAssetsCall extends APICall.Builder<GetAllAssetsCall> {
    private GetAllAssetsCall() {
        super(ApiSpec.getAllAssets);
    }

    public static GetAllAssetsCall create() {
        return new GetAllAssetsCall();
    }

    public GetAllAssetsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAllAssetsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAllAssetsCall includeCounts(boolean includeCounts) {
        return param("includeCounts", includeCounts);
    }

    public GetAllAssetsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAllAssetsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
