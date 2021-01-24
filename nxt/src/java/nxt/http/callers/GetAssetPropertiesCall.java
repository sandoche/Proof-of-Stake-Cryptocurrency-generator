// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAssetPropertiesCall extends APICall.Builder<GetAssetPropertiesCall> {
    private GetAssetPropertiesCall() {
        super(ApiSpec.getAssetProperties);
    }

    public static GetAssetPropertiesCall create() {
        return new GetAssetPropertiesCall();
    }

    public GetAssetPropertiesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAssetPropertiesCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAssetPropertiesCall property(String property) {
        return param("property", property);
    }

    public GetAssetPropertiesCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAssetPropertiesCall setter(String setter) {
        return param("setter", setter);
    }

    public GetAssetPropertiesCall setter(long setter) {
        return unsignedLongParam("setter", setter);
    }

    public GetAssetPropertiesCall asset(String asset) {
        return param("asset", asset);
    }

    public GetAssetPropertiesCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public GetAssetPropertiesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
