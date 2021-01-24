// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetCurrencyAccountsCall extends APICall.Builder<GetCurrencyAccountsCall> {
    private GetCurrencyAccountsCall() {
        super(ApiSpec.getCurrencyAccounts);
    }

    public static GetCurrencyAccountsCall create() {
        return new GetCurrencyAccountsCall();
    }

    public GetCurrencyAccountsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetCurrencyAccountsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetCurrencyAccountsCall currency(String currency) {
        return param("currency", currency);
    }

    public GetCurrencyAccountsCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public GetCurrencyAccountsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetCurrencyAccountsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetCurrencyAccountsCall height(int height) {
        return param("height", height);
    }
}
