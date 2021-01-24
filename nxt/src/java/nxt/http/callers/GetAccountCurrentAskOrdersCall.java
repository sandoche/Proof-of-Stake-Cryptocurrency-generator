// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAccountCurrentAskOrdersCall extends APICall.Builder<GetAccountCurrentAskOrdersCall> {
    private GetAccountCurrentAskOrdersCall() {
        super(ApiSpec.getAccountCurrentAskOrders);
    }

    public static GetAccountCurrentAskOrdersCall create() {
        return new GetAccountCurrentAskOrdersCall();
    }

    public GetAccountCurrentAskOrdersCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAccountCurrentAskOrdersCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAccountCurrentAskOrdersCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAccountCurrentAskOrdersCall asset(String asset) {
        return param("asset", asset);
    }

    public GetAccountCurrentAskOrdersCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public GetAccountCurrentAskOrdersCall account(String account) {
        return param("account", account);
    }

    public GetAccountCurrentAskOrdersCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetAccountCurrentAskOrdersCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
