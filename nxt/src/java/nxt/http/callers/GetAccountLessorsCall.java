// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAccountLessorsCall extends APICall.Builder<GetAccountLessorsCall> {
    private GetAccountLessorsCall() {
        super(ApiSpec.getAccountLessors);
    }

    public static GetAccountLessorsCall create() {
        return new GetAccountLessorsCall();
    }

    public GetAccountLessorsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAccountLessorsCall account(String account) {
        return param("account", account);
    }

    public GetAccountLessorsCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetAccountLessorsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAccountLessorsCall height(int height) {
        return param("height", height);
    }
}
