// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GenerateFileTokenCall extends APICall.Builder<GenerateFileTokenCall> {
    private GenerateFileTokenCall() {
        super(ApiSpec.generateFileToken);
    }

    public static GenerateFileTokenCall create() {
        return new GenerateFileTokenCall();
    }

    public GenerateFileTokenCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public GenerateFileTokenCall file(byte[] b) {
        return parts("file", b);
    }
}
