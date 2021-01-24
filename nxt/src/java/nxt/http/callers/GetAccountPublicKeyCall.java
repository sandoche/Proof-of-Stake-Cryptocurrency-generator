// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAccountPublicKeyCall extends APICall.Builder<GetAccountPublicKeyCall> {
    private GetAccountPublicKeyCall() {
        super(ApiSpec.getAccountPublicKey);
    }

    public static GetAccountPublicKeyCall create() {
        return new GetAccountPublicKeyCall();
    }

    public GetAccountPublicKeyCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAccountPublicKeyCall account(String account) {
        return param("account", account);
    }

    public GetAccountPublicKeyCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetAccountPublicKeyCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
