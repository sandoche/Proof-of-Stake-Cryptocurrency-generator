// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAllTradesCall extends APICall.Builder<GetAllTradesCall> {
    private GetAllTradesCall() {
        super(ApiSpec.getAllTrades);
    }

    public static GetAllTradesCall create() {
        return new GetAllTradesCall();
    }

    public GetAllTradesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAllTradesCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAllTradesCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAllTradesCall includeAssetInfo(boolean includeAssetInfo) {
        return param("includeAssetInfo", includeAssetInfo);
    }

    public GetAllTradesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAllTradesCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }
}
