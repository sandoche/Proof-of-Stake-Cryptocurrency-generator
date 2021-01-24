// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetTransactionBytesCall extends APICall.Builder<GetTransactionBytesCall> {
    private GetTransactionBytesCall() {
        super(ApiSpec.getTransactionBytes);
    }

    public static GetTransactionBytesCall create() {
        return new GetTransactionBytesCall();
    }

    public GetTransactionBytesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetTransactionBytesCall transaction(String transaction) {
        return param("transaction", transaction);
    }

    public GetTransactionBytesCall transaction(long transaction) {
        return unsignedLongParam("transaction", transaction);
    }

    public GetTransactionBytesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
