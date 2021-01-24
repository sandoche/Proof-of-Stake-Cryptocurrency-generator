// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAccountShufflingsCall extends APICall.Builder<GetAccountShufflingsCall> {
    private GetAccountShufflingsCall() {
        super(ApiSpec.getAccountShufflings);
    }

    public static GetAccountShufflingsCall create() {
        return new GetAccountShufflingsCall();
    }

    public GetAccountShufflingsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAccountShufflingsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAccountShufflingsCall includeHoldingInfo(boolean includeHoldingInfo) {
        return param("includeHoldingInfo", includeHoldingInfo);
    }

    public GetAccountShufflingsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAccountShufflingsCall includeFinished(boolean includeFinished) {
        return param("includeFinished", includeFinished);
    }

    public GetAccountShufflingsCall account(String account) {
        return param("account", account);
    }

    public GetAccountShufflingsCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetAccountShufflingsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
