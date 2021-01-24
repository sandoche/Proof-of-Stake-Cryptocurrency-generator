// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetCurrencyTransfersCall extends APICall.Builder<GetCurrencyTransfersCall> {
    private GetCurrencyTransfersCall() {
        super(ApiSpec.getCurrencyTransfers);
    }

    public static GetCurrencyTransfersCall create() {
        return new GetCurrencyTransfersCall();
    }

    public GetCurrencyTransfersCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetCurrencyTransfersCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetCurrencyTransfersCall currency(String currency) {
        return param("currency", currency);
    }

    public GetCurrencyTransfersCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public GetCurrencyTransfersCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetCurrencyTransfersCall includeCurrencyInfo(boolean includeCurrencyInfo) {
        return param("includeCurrencyInfo", includeCurrencyInfo);
    }

    public GetCurrencyTransfersCall account(String account) {
        return param("account", account);
    }

    public GetCurrencyTransfersCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetCurrencyTransfersCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetCurrencyTransfersCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }
}
