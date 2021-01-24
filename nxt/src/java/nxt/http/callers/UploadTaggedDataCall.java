// Auto generated code, do not modify
package nxt.http.callers;

public class UploadTaggedDataCall extends CreateTransactionCallBuilder<UploadTaggedDataCall> {
    private UploadTaggedDataCall() {
        super(ApiSpec.uploadTaggedData);
    }

    public static UploadTaggedDataCall create() {
        return new UploadTaggedDataCall();
    }

    public UploadTaggedDataCall filename(String filename) {
        return param("filename", filename);
    }

    public UploadTaggedDataCall data(String data) {
        return param("data", data);
    }

    public UploadTaggedDataCall channel(String channel) {
        return param("channel", channel);
    }

    public UploadTaggedDataCall name(String name) {
        return param("name", name);
    }

    public UploadTaggedDataCall description(String description) {
        return param("description", description);
    }

    public UploadTaggedDataCall type(int type) {
        return param("type", type);
    }

    public UploadTaggedDataCall isText(boolean isText) {
        return param("isText", isText);
    }

    public UploadTaggedDataCall tags(String tags) {
        return param("tags", tags);
    }

    public UploadTaggedDataCall file(byte[] b) {
        return parts("file", b);
    }
}
