// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetUnconfirmedTransactionIdsCall extends APICall.Builder<GetUnconfirmedTransactionIdsCall> {
    private GetUnconfirmedTransactionIdsCall() {
        super(ApiSpec.getUnconfirmedTransactionIds);
    }

    public static GetUnconfirmedTransactionIdsCall create() {
        return new GetUnconfirmedTransactionIdsCall();
    }

    public GetUnconfirmedTransactionIdsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetUnconfirmedTransactionIdsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetUnconfirmedTransactionIdsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetUnconfirmedTransactionIdsCall account(String... account) {
        return param("account", account);
    }

    public GetUnconfirmedTransactionIdsCall account(long... account) {
        return unsignedLongParam("account", account);
    }

    public GetUnconfirmedTransactionIdsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
