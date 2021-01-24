// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetBidOrdersCall extends APICall.Builder<GetBidOrdersCall> {
    private GetBidOrdersCall() {
        super(ApiSpec.getBidOrders);
    }

    public static GetBidOrdersCall create() {
        return new GetBidOrdersCall();
    }

    public GetBidOrdersCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetBidOrdersCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetBidOrdersCall showExpectedCancellations(String showExpectedCancellations) {
        return param("showExpectedCancellations", showExpectedCancellations);
    }

    public GetBidOrdersCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetBidOrdersCall asset(String asset) {
        return param("asset", asset);
    }

    public GetBidOrdersCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public GetBidOrdersCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
