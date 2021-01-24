// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetPhasingOnlyControlCall extends APICall.Builder<GetPhasingOnlyControlCall> {
    private GetPhasingOnlyControlCall() {
        super(ApiSpec.getPhasingOnlyControl);
    }

    public static GetPhasingOnlyControlCall create() {
        return new GetPhasingOnlyControlCall();
    }

    public GetPhasingOnlyControlCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetPhasingOnlyControlCall account(String account) {
        return param("account", account);
    }

    public GetPhasingOnlyControlCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetPhasingOnlyControlCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
