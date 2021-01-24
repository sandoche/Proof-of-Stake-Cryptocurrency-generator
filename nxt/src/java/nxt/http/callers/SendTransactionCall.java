// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class SendTransactionCall extends APICall.Builder<SendTransactionCall> {
    private SendTransactionCall() {
        super(ApiSpec.sendTransaction);
    }

    public static SendTransactionCall create() {
        return new SendTransactionCall();
    }

    public SendTransactionCall transactionJSON(String transactionJSON) {
        return param("transactionJSON", transactionJSON);
    }

    public SendTransactionCall transactionBytes(String transactionBytes) {
        return param("transactionBytes", transactionBytes);
    }

    public SendTransactionCall transactionBytes(byte[] transactionBytes) {
        return param("transactionBytes", transactionBytes);
    }

    public SendTransactionCall prunableAttachmentJSON(String prunableAttachmentJSON) {
        return param("prunableAttachmentJSON", prunableAttachmentJSON);
    }
}
