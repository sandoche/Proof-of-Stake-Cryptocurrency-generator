// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetBuyOffersCall extends APICall.Builder<GetBuyOffersCall> {
    private GetBuyOffersCall() {
        super(ApiSpec.getBuyOffers);
    }

    public static GetBuyOffersCall create() {
        return new GetBuyOffersCall();
    }

    public GetBuyOffersCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetBuyOffersCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetBuyOffersCall availableOnly(String availableOnly) {
        return param("availableOnly", availableOnly);
    }

    public GetBuyOffersCall currency(String currency) {
        return param("currency", currency);
    }

    public GetBuyOffersCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public GetBuyOffersCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetBuyOffersCall account(String account) {
        return param("account", account);
    }

    public GetBuyOffersCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetBuyOffersCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
