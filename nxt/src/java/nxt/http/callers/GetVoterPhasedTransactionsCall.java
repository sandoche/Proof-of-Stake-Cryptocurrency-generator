// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetVoterPhasedTransactionsCall extends APICall.Builder<GetVoterPhasedTransactionsCall> {
    private GetVoterPhasedTransactionsCall() {
        super(ApiSpec.getVoterPhasedTransactions);
    }

    public static GetVoterPhasedTransactionsCall create() {
        return new GetVoterPhasedTransactionsCall();
    }

    public GetVoterPhasedTransactionsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetVoterPhasedTransactionsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetVoterPhasedTransactionsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetVoterPhasedTransactionsCall account(String account) {
        return param("account", account);
    }

    public GetVoterPhasedTransactionsCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetVoterPhasedTransactionsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
