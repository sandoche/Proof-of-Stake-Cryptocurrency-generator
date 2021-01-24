// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAliasCall extends APICall.Builder<GetAliasCall> {
    private GetAliasCall() {
        super(ApiSpec.getAlias);
    }

    public static GetAliasCall create() {
        return new GetAliasCall();
    }

    public GetAliasCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAliasCall aliasName(String aliasName) {
        return param("aliasName", aliasName);
    }

    public GetAliasCall alias(String alias) {
        return param("alias", alias);
    }

    public GetAliasCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
