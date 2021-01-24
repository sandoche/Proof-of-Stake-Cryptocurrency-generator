// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAskOrderIdsCall extends APICall.Builder<GetAskOrderIdsCall> {
    private GetAskOrderIdsCall() {
        super(ApiSpec.getAskOrderIds);
    }

    public static GetAskOrderIdsCall create() {
        return new GetAskOrderIdsCall();
    }

    public GetAskOrderIdsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAskOrderIdsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAskOrderIdsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAskOrderIdsCall asset(String asset) {
        return param("asset", asset);
    }

    public GetAskOrderIdsCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public GetAskOrderIdsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
