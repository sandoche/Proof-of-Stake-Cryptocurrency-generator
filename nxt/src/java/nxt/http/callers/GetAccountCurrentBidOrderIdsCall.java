// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAccountCurrentBidOrderIdsCall extends APICall.Builder<GetAccountCurrentBidOrderIdsCall> {
    private GetAccountCurrentBidOrderIdsCall() {
        super(ApiSpec.getAccountCurrentBidOrderIds);
    }

    public static GetAccountCurrentBidOrderIdsCall create() {
        return new GetAccountCurrentBidOrderIdsCall();
    }

    public GetAccountCurrentBidOrderIdsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAccountCurrentBidOrderIdsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAccountCurrentBidOrderIdsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAccountCurrentBidOrderIdsCall asset(String asset) {
        return param("asset", asset);
    }

    public GetAccountCurrentBidOrderIdsCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public GetAccountCurrentBidOrderIdsCall account(String account) {
        return param("account", account);
    }

    public GetAccountCurrentBidOrderIdsCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetAccountCurrentBidOrderIdsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
