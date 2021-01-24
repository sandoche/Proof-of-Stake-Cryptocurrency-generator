// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetExchangesCall extends APICall.Builder<GetExchangesCall> {
    private GetExchangesCall() {
        super(ApiSpec.getExchanges);
    }

    public static GetExchangesCall create() {
        return new GetExchangesCall();
    }

    public GetExchangesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetExchangesCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetExchangesCall currency(String currency) {
        return param("currency", currency);
    }

    public GetExchangesCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public GetExchangesCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetExchangesCall includeCurrencyInfo(boolean includeCurrencyInfo) {
        return param("includeCurrencyInfo", includeCurrencyInfo);
    }

    public GetExchangesCall account(String account) {
        return param("account", account);
    }

    public GetExchangesCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetExchangesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetExchangesCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }
}
