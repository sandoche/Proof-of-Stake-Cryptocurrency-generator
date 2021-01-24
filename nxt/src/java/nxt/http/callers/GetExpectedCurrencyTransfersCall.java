// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetExpectedCurrencyTransfersCall extends APICall.Builder<GetExpectedCurrencyTransfersCall> {
    private GetExpectedCurrencyTransfersCall() {
        super(ApiSpec.getExpectedCurrencyTransfers);
    }

    public static GetExpectedCurrencyTransfersCall create() {
        return new GetExpectedCurrencyTransfersCall();
    }

    public GetExpectedCurrencyTransfersCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetExpectedCurrencyTransfersCall currency(String currency) {
        return param("currency", currency);
    }

    public GetExpectedCurrencyTransfersCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public GetExpectedCurrencyTransfersCall includeCurrencyInfo(boolean includeCurrencyInfo) {
        return param("includeCurrencyInfo", includeCurrencyInfo);
    }

    public GetExpectedCurrencyTransfersCall account(String account) {
        return param("account", account);
    }

    public GetExpectedCurrencyTransfersCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetExpectedCurrencyTransfersCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
