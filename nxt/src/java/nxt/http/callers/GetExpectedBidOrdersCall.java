// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetExpectedBidOrdersCall extends APICall.Builder<GetExpectedBidOrdersCall> {
    private GetExpectedBidOrdersCall() {
        super(ApiSpec.getExpectedBidOrders);
    }

    public static GetExpectedBidOrdersCall create() {
        return new GetExpectedBidOrdersCall();
    }

    public GetExpectedBidOrdersCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetExpectedBidOrdersCall sortByPrice(String sortByPrice) {
        return param("sortByPrice", sortByPrice);
    }

    public GetExpectedBidOrdersCall asset(String asset) {
        return param("asset", asset);
    }

    public GetExpectedBidOrdersCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public GetExpectedBidOrdersCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
