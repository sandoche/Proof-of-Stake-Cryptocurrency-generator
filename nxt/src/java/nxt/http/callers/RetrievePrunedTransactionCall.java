// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class RetrievePrunedTransactionCall extends APICall.Builder<RetrievePrunedTransactionCall> {
    private RetrievePrunedTransactionCall() {
        super(ApiSpec.retrievePrunedTransaction);
    }

    public static RetrievePrunedTransactionCall create() {
        return new RetrievePrunedTransactionCall();
    }

    public RetrievePrunedTransactionCall transaction(String transaction) {
        return param("transaction", transaction);
    }

    public RetrievePrunedTransactionCall transaction(long transaction) {
        return unsignedLongParam("transaction", transaction);
    }
}
