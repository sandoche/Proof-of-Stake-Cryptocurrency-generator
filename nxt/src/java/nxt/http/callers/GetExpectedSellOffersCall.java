// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetExpectedSellOffersCall extends APICall.Builder<GetExpectedSellOffersCall> {
    private GetExpectedSellOffersCall() {
        super(ApiSpec.getExpectedSellOffers);
    }

    public static GetExpectedSellOffersCall create() {
        return new GetExpectedSellOffersCall();
    }

    public GetExpectedSellOffersCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetExpectedSellOffersCall sortByRate(String sortByRate) {
        return param("sortByRate", sortByRate);
    }

    public GetExpectedSellOffersCall currency(String currency) {
        return param("currency", currency);
    }

    public GetExpectedSellOffersCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public GetExpectedSellOffersCall account(String account) {
        return param("account", account);
    }

    public GetExpectedSellOffersCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetExpectedSellOffersCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
