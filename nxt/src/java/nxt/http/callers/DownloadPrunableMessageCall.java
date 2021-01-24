// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class DownloadPrunableMessageCall extends APICall.Builder<DownloadPrunableMessageCall> {
    private DownloadPrunableMessageCall() {
        super(ApiSpec.downloadPrunableMessage);
    }

    public static DownloadPrunableMessageCall create() {
        return new DownloadPrunableMessageCall();
    }

    public DownloadPrunableMessageCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public DownloadPrunableMessageCall sharedKey(String sharedKey) {
        return param("sharedKey", sharedKey);
    }

    public DownloadPrunableMessageCall save(String save) {
        return param("save", save);
    }

    public DownloadPrunableMessageCall retrieve(boolean retrieve) {
        return param("retrieve", retrieve);
    }

    public DownloadPrunableMessageCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public DownloadPrunableMessageCall transaction(String transaction) {
        return param("transaction", transaction);
    }

    public DownloadPrunableMessageCall transaction(long transaction) {
        return unsignedLongParam("transaction", transaction);
    }

    public DownloadPrunableMessageCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
