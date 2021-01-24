// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAccountCurrentAskOrderIdsCall extends APICall.Builder<GetAccountCurrentAskOrderIdsCall> {
    private GetAccountCurrentAskOrderIdsCall() {
        super(ApiSpec.getAccountCurrentAskOrderIds);
    }

    public static GetAccountCurrentAskOrderIdsCall create() {
        return new GetAccountCurrentAskOrderIdsCall();
    }

    public GetAccountCurrentAskOrderIdsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAccountCurrentAskOrderIdsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAccountCurrentAskOrderIdsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAccountCurrentAskOrderIdsCall asset(String asset) {
        return param("asset", asset);
    }

    public GetAccountCurrentAskOrderIdsCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public GetAccountCurrentAskOrderIdsCall account(String account) {
        return param("account", account);
    }

    public GetAccountCurrentAskOrderIdsCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetAccountCurrentAskOrderIdsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
