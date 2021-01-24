// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAskOrdersCall extends APICall.Builder<GetAskOrdersCall> {
    private GetAskOrdersCall() {
        super(ApiSpec.getAskOrders);
    }

    public static GetAskOrdersCall create() {
        return new GetAskOrdersCall();
    }

    public GetAskOrdersCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAskOrdersCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAskOrdersCall showExpectedCancellations(String showExpectedCancellations) {
        return param("showExpectedCancellations", showExpectedCancellations);
    }

    public GetAskOrdersCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAskOrdersCall asset(String asset) {
        return param("asset", asset);
    }

    public GetAskOrdersCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public GetAskOrdersCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
