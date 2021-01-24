// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAssetIdsCall extends APICall.Builder<GetAssetIdsCall> {
    private GetAssetIdsCall() {
        super(ApiSpec.getAssetIds);
    }

    public static GetAssetIdsCall create() {
        return new GetAssetIdsCall();
    }

    public GetAssetIdsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAssetIdsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAssetIdsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAssetIdsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
