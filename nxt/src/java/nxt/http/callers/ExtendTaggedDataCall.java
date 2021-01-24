// Auto generated code, do not modify
package nxt.http.callers;

public class ExtendTaggedDataCall extends CreateTransactionCallBuilder<ExtendTaggedDataCall> {
    private ExtendTaggedDataCall() {
        super(ApiSpec.extendTaggedData);
    }

    public static ExtendTaggedDataCall create() {
        return new ExtendTaggedDataCall();
    }

    public ExtendTaggedDataCall filename(String filename) {
        return param("filename", filename);
    }

    public ExtendTaggedDataCall data(String data) {
        return param("data", data);
    }

    public ExtendTaggedDataCall channel(String channel) {
        return param("channel", channel);
    }

    public ExtendTaggedDataCall name(String name) {
        return param("name", name);
    }

    public ExtendTaggedDataCall description(String description) {
        return param("description", description);
    }

    public ExtendTaggedDataCall type(int type) {
        return param("type", type);
    }

    public ExtendTaggedDataCall isText(boolean isText) {
        return param("isText", isText);
    }

    public ExtendTaggedDataCall transaction(String transaction) {
        return param("transaction", transaction);
    }

    public ExtendTaggedDataCall transaction(long transaction) {
        return unsignedLongParam("transaction", transaction);
    }

    public ExtendTaggedDataCall tags(String tags) {
        return param("tags", tags);
    }

    public ExtendTaggedDataCall file(byte[] b) {
        return parts("file", b);
    }
}
