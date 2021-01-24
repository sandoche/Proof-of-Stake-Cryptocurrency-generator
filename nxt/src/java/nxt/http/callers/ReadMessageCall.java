// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class ReadMessageCall extends APICall.Builder<ReadMessageCall> {
    private ReadMessageCall() {
        super(ApiSpec.readMessage);
    }

    public static ReadMessageCall create() {
        return new ReadMessageCall();
    }

    public ReadMessageCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public ReadMessageCall sharedKey(String sharedKey) {
        return param("sharedKey", sharedKey);
    }

    public ReadMessageCall retrieve(boolean retrieve) {
        return param("retrieve", retrieve);
    }

    public ReadMessageCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public ReadMessageCall transaction(String transaction) {
        return param("transaction", transaction);
    }

    public ReadMessageCall transaction(long transaction) {
        return unsignedLongParam("transaction", transaction);
    }

    public ReadMessageCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
