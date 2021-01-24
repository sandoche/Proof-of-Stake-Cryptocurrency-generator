// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetLastTradesCall extends APICall.Builder<GetLastTradesCall> {
    private GetLastTradesCall() {
        super(ApiSpec.getLastTrades);
    }

    public static GetLastTradesCall create() {
        return new GetLastTradesCall();
    }

    public GetLastTradesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetLastTradesCall assets(String... assets) {
        return param("assets", assets);
    }

    public GetLastTradesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
