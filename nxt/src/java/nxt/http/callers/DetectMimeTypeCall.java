// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class DetectMimeTypeCall extends APICall.Builder<DetectMimeTypeCall> {
    private DetectMimeTypeCall() {
        super(ApiSpec.detectMimeType);
    }

    public static DetectMimeTypeCall create() {
        return new DetectMimeTypeCall();
    }

    public DetectMimeTypeCall filename(String filename) {
        return param("filename", filename);
    }

    public DetectMimeTypeCall data(String data) {
        return param("data", data);
    }

    public DetectMimeTypeCall isText(boolean isText) {
        return param("isText", isText);
    }

    public DetectMimeTypeCall file(byte[] b) {
        return parts("file", b);
    }
}
