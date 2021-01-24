// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetBidOrderIdsCall extends APICall.Builder<GetBidOrderIdsCall> {
    private GetBidOrderIdsCall() {
        super(ApiSpec.getBidOrderIds);
    }

    public static GetBidOrderIdsCall create() {
        return new GetBidOrderIdsCall();
    }

    public GetBidOrderIdsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetBidOrderIdsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetBidOrderIdsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetBidOrderIdsCall asset(String asset) {
        return param("asset", asset);
    }

    public GetBidOrderIdsCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public GetBidOrderIdsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
