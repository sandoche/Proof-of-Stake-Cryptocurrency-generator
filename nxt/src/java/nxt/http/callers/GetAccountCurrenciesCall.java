// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAccountCurrenciesCall extends APICall.Builder<GetAccountCurrenciesCall> {
    private GetAccountCurrenciesCall() {
        super(ApiSpec.getAccountCurrencies);
    }

    public static GetAccountCurrenciesCall create() {
        return new GetAccountCurrenciesCall();
    }

    public GetAccountCurrenciesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAccountCurrenciesCall currency(String currency) {
        return param("currency", currency);
    }

    public GetAccountCurrenciesCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public GetAccountCurrenciesCall includeCurrencyInfo(boolean includeCurrencyInfo) {
        return param("includeCurrencyInfo", includeCurrencyInfo);
    }

    public GetAccountCurrenciesCall account(String account) {
        return param("account", account);
    }

    public GetAccountCurrenciesCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetAccountCurrenciesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAccountCurrenciesCall height(int height) {
        return param("height", height);
    }
}
