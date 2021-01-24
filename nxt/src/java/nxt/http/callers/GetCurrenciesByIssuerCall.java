// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetCurrenciesByIssuerCall extends APICall.Builder<GetCurrenciesByIssuerCall> {
    private GetCurrenciesByIssuerCall() {
        super(ApiSpec.getCurrenciesByIssuer);
    }

    public static GetCurrenciesByIssuerCall create() {
        return new GetCurrenciesByIssuerCall();
    }

    public GetCurrenciesByIssuerCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetCurrenciesByIssuerCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetCurrenciesByIssuerCall includeCounts(boolean includeCounts) {
        return param("includeCounts", includeCounts);
    }

    public GetCurrenciesByIssuerCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetCurrenciesByIssuerCall account(String... account) {
        return param("account", account);
    }

    public GetCurrenciesByIssuerCall account(long... account) {
        return unsignedLongParam("account", account);
    }

    public GetCurrenciesByIssuerCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
