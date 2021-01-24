// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetExchangesByOfferCall extends APICall.Builder<GetExchangesByOfferCall> {
    private GetExchangesByOfferCall() {
        super(ApiSpec.getExchangesByOffer);
    }

    public static GetExchangesByOfferCall create() {
        return new GetExchangesByOfferCall();
    }

    public GetExchangesByOfferCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetExchangesByOfferCall offer(String offer) {
        return param("offer", offer);
    }

    public GetExchangesByOfferCall offer(long offer) {
        return unsignedLongParam("offer", offer);
    }

    public GetExchangesByOfferCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetExchangesByOfferCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetExchangesByOfferCall includeCurrencyInfo(boolean includeCurrencyInfo) {
        return param("includeCurrencyInfo", includeCurrencyInfo);
    }

    public GetExchangesByOfferCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
