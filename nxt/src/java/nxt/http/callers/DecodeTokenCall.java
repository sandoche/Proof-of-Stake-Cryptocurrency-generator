// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class DecodeTokenCall extends APICall.Builder<DecodeTokenCall> {
    private DecodeTokenCall() {
        super(ApiSpec.decodeToken);
    }

    public static DecodeTokenCall create() {
        return new DecodeTokenCall();
    }

    public DecodeTokenCall website(String website) {
        return param("website", website);
    }

    public DecodeTokenCall token(String token) {
        return param("token", token);
    }
}
