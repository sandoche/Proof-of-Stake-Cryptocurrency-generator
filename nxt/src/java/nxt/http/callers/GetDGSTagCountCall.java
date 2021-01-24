// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetDGSTagCountCall extends APICall.Builder<GetDGSTagCountCall> {
    private GetDGSTagCountCall() {
        super(ApiSpec.getDGSTagCount);
    }

    public static GetDGSTagCountCall create() {
        return new GetDGSTagCountCall();
    }

    public GetDGSTagCountCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetDGSTagCountCall inStockOnly(String inStockOnly) {
        return param("inStockOnly", inStockOnly);
    }

    public GetDGSTagCountCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
