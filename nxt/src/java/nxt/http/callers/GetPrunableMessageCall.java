// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetPrunableMessageCall extends APICall.Builder<GetPrunableMessageCall> {
    private GetPrunableMessageCall() {
        super(ApiSpec.getPrunableMessage);
    }

    public static GetPrunableMessageCall create() {
        return new GetPrunableMessageCall();
    }

    public GetPrunableMessageCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetPrunableMessageCall sharedKey(String sharedKey) {
        return param("sharedKey", sharedKey);
    }

    public GetPrunableMessageCall retrieve(boolean retrieve) {
        return param("retrieve", retrieve);
    }

    public GetPrunableMessageCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public GetPrunableMessageCall transaction(String transaction) {
        return param("transaction", transaction);
    }

    public GetPrunableMessageCall transaction(long transaction) {
        return unsignedLongParam("transaction", transaction);
    }

    public GetPrunableMessageCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
