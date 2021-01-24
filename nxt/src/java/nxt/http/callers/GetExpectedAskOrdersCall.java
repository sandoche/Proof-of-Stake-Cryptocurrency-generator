// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetExpectedAskOrdersCall extends APICall.Builder<GetExpectedAskOrdersCall> {
    private GetExpectedAskOrdersCall() {
        super(ApiSpec.getExpectedAskOrders);
    }

    public static GetExpectedAskOrdersCall create() {
        return new GetExpectedAskOrdersCall();
    }

    public GetExpectedAskOrdersCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetExpectedAskOrdersCall sortByPrice(String sortByPrice) {
        return param("sortByPrice", sortByPrice);
    }

    public GetExpectedAskOrdersCall asset(String asset) {
        return param("asset", asset);
    }

    public GetExpectedAskOrdersCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public GetExpectedAskOrdersCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
