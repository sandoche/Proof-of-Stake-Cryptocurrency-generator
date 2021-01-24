// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetTaggedDataExtendTransactionsCall extends APICall.Builder<GetTaggedDataExtendTransactionsCall> {
    private GetTaggedDataExtendTransactionsCall() {
        super(ApiSpec.getTaggedDataExtendTransactions);
    }

    public static GetTaggedDataExtendTransactionsCall create() {
        return new GetTaggedDataExtendTransactionsCall();
    }

    public GetTaggedDataExtendTransactionsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetTaggedDataExtendTransactionsCall transaction(String transaction) {
        return param("transaction", transaction);
    }

    public GetTaggedDataExtendTransactionsCall transaction(long transaction) {
        return unsignedLongParam("transaction", transaction);
    }

    public GetTaggedDataExtendTransactionsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
