// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class FullHashToIdCall extends APICall.Builder<FullHashToIdCall> {
    private FullHashToIdCall() {
        super(ApiSpec.fullHashToId);
    }

    public static FullHashToIdCall create() {
        return new FullHashToIdCall();
    }

    public FullHashToIdCall fullHash(String fullHash) {
        return param("fullHash", fullHash);
    }

    public FullHashToIdCall fullHash(byte[] fullHash) {
        return param("fullHash", fullHash);
    }
}
