// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class BroadcastTransactionCall extends APICall.Builder<BroadcastTransactionCall> {
    private BroadcastTransactionCall() {
        super(ApiSpec.broadcastTransaction);
    }

    public static BroadcastTransactionCall create() {
        return new BroadcastTransactionCall();
    }

    public BroadcastTransactionCall transactionJSON(String transactionJSON) {
        return param("transactionJSON", transactionJSON);
    }

    public BroadcastTransactionCall transactionBytes(String transactionBytes) {
        return param("transactionBytes", transactionBytes);
    }

    public BroadcastTransactionCall transactionBytes(byte[] transactionBytes) {
        return param("transactionBytes", transactionBytes);
    }

    public BroadcastTransactionCall prunableAttachmentJSON(String prunableAttachmentJSON) {
        return param("prunableAttachmentJSON", prunableAttachmentJSON);
    }
}
