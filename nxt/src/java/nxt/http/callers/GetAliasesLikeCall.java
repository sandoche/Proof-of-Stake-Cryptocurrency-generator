// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAliasesLikeCall extends APICall.Builder<GetAliasesLikeCall> {
    private GetAliasesLikeCall() {
        super(ApiSpec.getAliasesLike);
    }

    public static GetAliasesLikeCall create() {
        return new GetAliasesLikeCall();
    }

    public GetAliasesLikeCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAliasesLikeCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAliasesLikeCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAliasesLikeCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAliasesLikeCall aliasPrefix(String aliasPrefix) {
        return param("aliasPrefix", aliasPrefix);
    }
}
