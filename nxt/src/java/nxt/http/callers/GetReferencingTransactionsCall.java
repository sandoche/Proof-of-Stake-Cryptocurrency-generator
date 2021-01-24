// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetReferencingTransactionsCall extends APICall.Builder<GetReferencingTransactionsCall> {
    private GetReferencingTransactionsCall() {
        super(ApiSpec.getReferencingTransactions);
    }

    public static GetReferencingTransactionsCall create() {
        return new GetReferencingTransactionsCall();
    }

    public GetReferencingTransactionsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetReferencingTransactionsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetReferencingTransactionsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetReferencingTransactionsCall transaction(String transaction) {
        return param("transaction", transaction);
    }

    public GetReferencingTransactionsCall transaction(long transaction) {
        return unsignedLongParam("transaction", transaction);
    }

    public GetReferencingTransactionsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
