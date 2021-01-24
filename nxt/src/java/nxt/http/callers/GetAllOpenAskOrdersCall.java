// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAllOpenAskOrdersCall extends APICall.Builder<GetAllOpenAskOrdersCall> {
    private GetAllOpenAskOrdersCall() {
        super(ApiSpec.getAllOpenAskOrders);
    }

    public static GetAllOpenAskOrdersCall create() {
        return new GetAllOpenAskOrdersCall();
    }

    public GetAllOpenAskOrdersCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAllOpenAskOrdersCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAllOpenAskOrdersCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAllOpenAskOrdersCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
