// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetDGSGoodsCountCall extends APICall.Builder<GetDGSGoodsCountCall> {
    private GetDGSGoodsCountCall() {
        super(ApiSpec.getDGSGoodsCount);
    }

    public static GetDGSGoodsCountCall create() {
        return new GetDGSGoodsCountCall();
    }

    public GetDGSGoodsCountCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetDGSGoodsCountCall seller(String seller) {
        return param("seller", seller);
    }

    public GetDGSGoodsCountCall inStockOnly(String inStockOnly) {
        return param("inStockOnly", inStockOnly);
    }

    public GetDGSGoodsCountCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
