// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetExpectedBuyOffersCall extends APICall.Builder<GetExpectedBuyOffersCall> {
    private GetExpectedBuyOffersCall() {
        super(ApiSpec.getExpectedBuyOffers);
    }

    public static GetExpectedBuyOffersCall create() {
        return new GetExpectedBuyOffersCall();
    }

    public GetExpectedBuyOffersCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetExpectedBuyOffersCall sortByRate(String sortByRate) {
        return param("sortByRate", sortByRate);
    }

    public GetExpectedBuyOffersCall currency(String currency) {
        return param("currency", currency);
    }

    public GetExpectedBuyOffersCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public GetExpectedBuyOffersCall account(String account) {
        return param("account", account);
    }

    public GetExpectedBuyOffersCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetExpectedBuyOffersCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
