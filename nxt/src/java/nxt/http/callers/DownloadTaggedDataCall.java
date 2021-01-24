// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class DownloadTaggedDataCall extends APICall.Builder<DownloadTaggedDataCall> {
    private DownloadTaggedDataCall() {
        super(ApiSpec.downloadTaggedData);
    }

    public static DownloadTaggedDataCall create() {
        return new DownloadTaggedDataCall();
    }

    public DownloadTaggedDataCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public DownloadTaggedDataCall retrieve(boolean retrieve) {
        return param("retrieve", retrieve);
    }

    public DownloadTaggedDataCall transaction(String transaction) {
        return param("transaction", transaction);
    }

    public DownloadTaggedDataCall transaction(long transaction) {
        return unsignedLongParam("transaction", transaction);
    }

    public DownloadTaggedDataCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
