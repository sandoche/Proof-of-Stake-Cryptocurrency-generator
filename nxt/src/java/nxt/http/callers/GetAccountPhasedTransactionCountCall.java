// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAccountPhasedTransactionCountCall extends APICall.Builder<GetAccountPhasedTransactionCountCall> {
    private GetAccountPhasedTransactionCountCall() {
        super(ApiSpec.getAccountPhasedTransactionCount);
    }

    public static GetAccountPhasedTransactionCountCall create() {
        return new GetAccountPhasedTransactionCountCall();
    }

    public GetAccountPhasedTransactionCountCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAccountPhasedTransactionCountCall account(String account) {
        return param("account", account);
    }

    public GetAccountPhasedTransactionCountCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetAccountPhasedTransactionCountCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
