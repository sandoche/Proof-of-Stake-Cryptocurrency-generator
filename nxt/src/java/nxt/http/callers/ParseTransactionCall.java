// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class ParseTransactionCall extends APICall.Builder<ParseTransactionCall> {
    private ParseTransactionCall() {
        super(ApiSpec.parseTransaction);
    }

    public static ParseTransactionCall create() {
        return new ParseTransactionCall();
    }

    public ParseTransactionCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public ParseTransactionCall transactionJSON(String transactionJSON) {
        return param("transactionJSON", transactionJSON);
    }

    public ParseTransactionCall transactionBytes(String transactionBytes) {
        return param("transactionBytes", transactionBytes);
    }

    public ParseTransactionCall transactionBytes(byte[] transactionBytes) {
        return param("transactionBytes", transactionBytes);
    }

    public ParseTransactionCall prunableAttachmentJSON(String prunableAttachmentJSON) {
        return param("prunableAttachmentJSON", prunableAttachmentJSON);
    }

    public ParseTransactionCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
