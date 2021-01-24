// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAllOpenBidOrdersCall extends APICall.Builder<GetAllOpenBidOrdersCall> {
    private GetAllOpenBidOrdersCall() {
        super(ApiSpec.getAllOpenBidOrders);
    }

    public static GetAllOpenBidOrdersCall create() {
        return new GetAllOpenBidOrdersCall();
    }

    public GetAllOpenBidOrdersCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAllOpenBidOrdersCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAllOpenBidOrdersCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAllOpenBidOrdersCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
