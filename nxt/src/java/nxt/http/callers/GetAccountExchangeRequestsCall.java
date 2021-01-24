// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAccountExchangeRequestsCall extends APICall.Builder<GetAccountExchangeRequestsCall> {
    private GetAccountExchangeRequestsCall() {
        super(ApiSpec.getAccountExchangeRequests);
    }

    public static GetAccountExchangeRequestsCall create() {
        return new GetAccountExchangeRequestsCall();
    }

    public GetAccountExchangeRequestsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAccountExchangeRequestsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAccountExchangeRequestsCall currency(String currency) {
        return param("currency", currency);
    }

    public GetAccountExchangeRequestsCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public GetAccountExchangeRequestsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAccountExchangeRequestsCall includeCurrencyInfo(boolean includeCurrencyInfo) {
        return param("includeCurrencyInfo", includeCurrencyInfo);
    }

    public GetAccountExchangeRequestsCall account(String account) {
        return param("account", account);
    }

    public GetAccountExchangeRequestsCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetAccountExchangeRequestsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
