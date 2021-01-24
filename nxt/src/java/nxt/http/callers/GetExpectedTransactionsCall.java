// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetExpectedTransactionsCall extends APICall.Builder<GetExpectedTransactionsCall> {
    private GetExpectedTransactionsCall() {
        super(ApiSpec.getExpectedTransactions);
    }

    public static GetExpectedTransactionsCall create() {
        return new GetExpectedTransactionsCall();
    }

    public GetExpectedTransactionsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetExpectedTransactionsCall account(String... account) {
        return param("account", account);
    }

    public GetExpectedTransactionsCall account(long... account) {
        return unsignedLongParam("account", account);
    }

    public GetExpectedTransactionsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
