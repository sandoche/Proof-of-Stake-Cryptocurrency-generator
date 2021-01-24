// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAccountBlockCountCall extends APICall.Builder<GetAccountBlockCountCall> {
    private GetAccountBlockCountCall() {
        super(ApiSpec.getAccountBlockCount);
    }

    public static GetAccountBlockCountCall create() {
        return new GetAccountBlockCountCall();
    }

    public GetAccountBlockCountCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAccountBlockCountCall account(String account) {
        return param("account", account);
    }

    public GetAccountBlockCountCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetAccountBlockCountCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
