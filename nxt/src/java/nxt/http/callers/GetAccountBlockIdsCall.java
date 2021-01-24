// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAccountBlockIdsCall extends APICall.Builder<GetAccountBlockIdsCall> {
    private GetAccountBlockIdsCall() {
        super(ApiSpec.getAccountBlockIds);
    }

    public static GetAccountBlockIdsCall create() {
        return new GetAccountBlockIdsCall();
    }

    public GetAccountBlockIdsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAccountBlockIdsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAccountBlockIdsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAccountBlockIdsCall account(String account) {
        return param("account", account);
    }

    public GetAccountBlockIdsCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetAccountBlockIdsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAccountBlockIdsCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }
}
