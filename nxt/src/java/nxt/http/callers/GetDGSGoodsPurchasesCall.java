// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetDGSGoodsPurchasesCall extends APICall.Builder<GetDGSGoodsPurchasesCall> {
    private GetDGSGoodsPurchasesCall() {
        super(ApiSpec.getDGSGoodsPurchases);
    }

    public static GetDGSGoodsPurchasesCall create() {
        return new GetDGSGoodsPurchasesCall();
    }

    public GetDGSGoodsPurchasesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetDGSGoodsPurchasesCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetDGSGoodsPurchasesCall goods(String goods) {
        return param("goods", goods);
    }

    public GetDGSGoodsPurchasesCall goods(long goods) {
        return unsignedLongParam("goods", goods);
    }

    public GetDGSGoodsPurchasesCall completed(String completed) {
        return param("completed", completed);
    }

    public GetDGSGoodsPurchasesCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetDGSGoodsPurchasesCall withPublicFeedbacksOnly(String withPublicFeedbacksOnly) {
        return param("withPublicFeedbacksOnly", withPublicFeedbacksOnly);
    }

    public GetDGSGoodsPurchasesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetDGSGoodsPurchasesCall buyer(String buyer) {
        return param("buyer", buyer);
    }

    public GetDGSGoodsPurchasesCall buyer(long buyer) {
        return unsignedLongParam("buyer", buyer);
    }
}
