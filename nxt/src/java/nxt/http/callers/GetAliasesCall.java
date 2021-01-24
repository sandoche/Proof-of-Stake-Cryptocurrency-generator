// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAliasesCall extends APICall.Builder<GetAliasesCall> {
    private GetAliasesCall() {
        super(ApiSpec.getAliases);
    }

    public static GetAliasesCall create() {
        return new GetAliasesCall();
    }

    public GetAliasesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAliasesCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAliasesCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAliasesCall account(String account) {
        return param("account", account);
    }

    public GetAliasesCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetAliasesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAliasesCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }
}
