// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAccountPhasedTransactionsCall extends APICall.Builder<GetAccountPhasedTransactionsCall> {
    private GetAccountPhasedTransactionsCall() {
        super(ApiSpec.getAccountPhasedTransactions);
    }

    public static GetAccountPhasedTransactionsCall create() {
        return new GetAccountPhasedTransactionsCall();
    }

    public GetAccountPhasedTransactionsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAccountPhasedTransactionsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAccountPhasedTransactionsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAccountPhasedTransactionsCall account(String account) {
        return param("account", account);
    }

    public GetAccountPhasedTransactionsCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetAccountPhasedTransactionsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
