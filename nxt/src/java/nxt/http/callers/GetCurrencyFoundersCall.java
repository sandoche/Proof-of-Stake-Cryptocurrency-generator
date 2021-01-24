// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetCurrencyFoundersCall extends APICall.Builder<GetCurrencyFoundersCall> {
    private GetCurrencyFoundersCall() {
        super(ApiSpec.getCurrencyFounders);
    }

    public static GetCurrencyFoundersCall create() {
        return new GetCurrencyFoundersCall();
    }

    public GetCurrencyFoundersCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetCurrencyFoundersCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetCurrencyFoundersCall currency(String currency) {
        return param("currency", currency);
    }

    public GetCurrencyFoundersCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public GetCurrencyFoundersCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetCurrencyFoundersCall account(String account) {
        return param("account", account);
    }

    public GetCurrencyFoundersCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetCurrencyFoundersCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
