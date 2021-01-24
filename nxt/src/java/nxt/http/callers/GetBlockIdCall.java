// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetBlockIdCall extends APICall.Builder<GetBlockIdCall> {
    private GetBlockIdCall() {
        super(ApiSpec.getBlockId);
    }

    public static GetBlockIdCall create() {
        return new GetBlockIdCall();
    }

    public GetBlockIdCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetBlockIdCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetBlockIdCall height(int height) {
        return param("height", height);
    }
}
