// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetDataTagCountCall extends APICall.Builder<GetDataTagCountCall> {
    private GetDataTagCountCall() {
        super(ApiSpec.getDataTagCount);
    }

    public static GetDataTagCountCall create() {
        return new GetDataTagCountCall();
    }

    public GetDataTagCountCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetDataTagCountCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
