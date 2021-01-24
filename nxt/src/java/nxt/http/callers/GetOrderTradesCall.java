// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetOrderTradesCall extends APICall.Builder<GetOrderTradesCall> {
    private GetOrderTradesCall() {
        super(ApiSpec.getOrderTrades);
    }

    public static GetOrderTradesCall create() {
        return new GetOrderTradesCall();
    }

    public GetOrderTradesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetOrderTradesCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetOrderTradesCall bidOrder(String bidOrder) {
        return param("bidOrder", bidOrder);
    }

    public GetOrderTradesCall askOrder(String askOrder) {
        return param("askOrder", askOrder);
    }

    public GetOrderTradesCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetOrderTradesCall includeAssetInfo(boolean includeAssetInfo) {
        return param("includeAssetInfo", includeAssetInfo);
    }

    public GetOrderTradesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
