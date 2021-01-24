// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class VerifyTaggedDataCall extends APICall.Builder<VerifyTaggedDataCall> {
    private VerifyTaggedDataCall() {
        super(ApiSpec.verifyTaggedData);
    }

    public static VerifyTaggedDataCall create() {
        return new VerifyTaggedDataCall();
    }

    public VerifyTaggedDataCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public VerifyTaggedDataCall filename(String filename) {
        return param("filename", filename);
    }

    public VerifyTaggedDataCall data(String data) {
        return param("data", data);
    }

    public VerifyTaggedDataCall channel(String channel) {
        return param("channel", channel);
    }

    public VerifyTaggedDataCall name(String name) {
        return param("name", name);
    }

    public VerifyTaggedDataCall description(String description) {
        return param("description", description);
    }

    public VerifyTaggedDataCall type(int type) {
        return param("type", type);
    }

    public VerifyTaggedDataCall isText(boolean isText) {
        return param("isText", isText);
    }

    public VerifyTaggedDataCall transaction(String transaction) {
        return param("transaction", transaction);
    }

    public VerifyTaggedDataCall transaction(long transaction) {
        return unsignedLongParam("transaction", transaction);
    }

    public VerifyTaggedDataCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public VerifyTaggedDataCall tags(String tags) {
        return param("tags", tags);
    }

    public VerifyTaggedDataCall file(byte[] b) {
        return parts("file", b);
    }
}
