// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GenerateTokenCall extends APICall.Builder<GenerateTokenCall> {
    private GenerateTokenCall() {
        super(ApiSpec.generateToken);
    }

    public static GenerateTokenCall create() {
        return new GenerateTokenCall();
    }

    public GenerateTokenCall website(String website) {
        return param("website", website);
    }

    public GenerateTokenCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }
}
