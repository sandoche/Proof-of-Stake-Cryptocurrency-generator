// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class SignTransactionCall extends APICall.Builder<SignTransactionCall> {
    private SignTransactionCall() {
        super(ApiSpec.signTransaction);
    }

    public static SignTransactionCall create() {
        return new SignTransactionCall();
    }

    public SignTransactionCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public SignTransactionCall unsignedTransactionBytes(String unsignedTransactionBytes) {
        return param("unsignedTransactionBytes", unsignedTransactionBytes);
    }

    public SignTransactionCall unsignedTransactionBytes(byte[] unsignedTransactionBytes) {
        return param("unsignedTransactionBytes", unsignedTransactionBytes);
    }

    public SignTransactionCall unsignedTransactionJSON(String unsignedTransactionJSON) {
        return param("unsignedTransactionJSON", unsignedTransactionJSON);
    }

    public SignTransactionCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public SignTransactionCall prunableAttachmentJSON(String prunableAttachmentJSON) {
        return param("prunableAttachmentJSON", prunableAttachmentJSON);
    }

    public SignTransactionCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public SignTransactionCall validate(boolean validate) {
        return param("validate", validate);
    }
}
