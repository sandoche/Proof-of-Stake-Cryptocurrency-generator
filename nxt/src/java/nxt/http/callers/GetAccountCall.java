// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAccountCall extends APICall.Builder<GetAccountCall> {
    private GetAccountCall() {
        super(ApiSpec.getAccount);
    }

    public static GetAccountCall create() {
        return new GetAccountCall();
    }

    public GetAccountCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAccountCall includeCurrencies(boolean includeCurrencies) {
        return param("includeCurrencies", includeCurrencies);
    }

    public GetAccountCall includeEffectiveBalance(boolean includeEffectiveBalance) {
        return param("includeEffectiveBalance", includeEffectiveBalance);
    }

    public GetAccountCall includeLessors(boolean includeLessors) {
        return param("includeLessors", includeLessors);
    }

    public GetAccountCall includeAssets(boolean includeAssets) {
        return param("includeAssets", includeAssets);
    }

    public GetAccountCall account(String account) {
        return param("account", account);
    }

    public GetAccountCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetAccountCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
