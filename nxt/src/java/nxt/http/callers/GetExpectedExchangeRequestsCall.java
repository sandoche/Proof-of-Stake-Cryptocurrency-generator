// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetExpectedExchangeRequestsCall extends APICall.Builder<GetExpectedExchangeRequestsCall> {
    private GetExpectedExchangeRequestsCall() {
        super(ApiSpec.getExpectedExchangeRequests);
    }

    public static GetExpectedExchangeRequestsCall create() {
        return new GetExpectedExchangeRequestsCall();
    }

    public GetExpectedExchangeRequestsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetExpectedExchangeRequestsCall currency(String currency) {
        return param("currency", currency);
    }

    public GetExpectedExchangeRequestsCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public GetExpectedExchangeRequestsCall includeCurrencyInfo(boolean includeCurrencyInfo) {
        return param("includeCurrencyInfo", includeCurrencyInfo);
    }

    public GetExpectedExchangeRequestsCall account(String account) {
        return param("account", account);
    }

    public GetExpectedExchangeRequestsCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetExpectedExchangeRequestsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
