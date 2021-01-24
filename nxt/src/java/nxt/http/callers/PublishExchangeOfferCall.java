// Auto generated code, do not modify
package nxt.http.callers;

public class PublishExchangeOfferCall extends CreateTransactionCallBuilder<PublishExchangeOfferCall> {
    private PublishExchangeOfferCall() {
        super(ApiSpec.publishExchangeOffer);
    }

    public static PublishExchangeOfferCall create() {
        return new PublishExchangeOfferCall();
    }

    public PublishExchangeOfferCall totalSellLimit(long totalSellLimit) {
        return param("totalSellLimit", totalSellLimit);
    }

    public PublishExchangeOfferCall initialSellSupply(long initialSellSupply) {
        return param("initialSellSupply", initialSellSupply);
    }

    public PublishExchangeOfferCall buyRateNQT(long buyRateNQT) {
        return param("buyRateNQT", buyRateNQT);
    }

    public PublishExchangeOfferCall expirationHeight(long expirationHeight) {
        return param("expirationHeight", expirationHeight);
    }

    public PublishExchangeOfferCall totalBuyLimit(long totalBuyLimit) {
        return param("totalBuyLimit", totalBuyLimit);
    }

    public PublishExchangeOfferCall sellRateNQT(long sellRateNQT) {
        return param("sellRateNQT", sellRateNQT);
    }

    public PublishExchangeOfferCall currency(String currency) {
        return param("currency", currency);
    }

    public PublishExchangeOfferCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public PublishExchangeOfferCall initialBuySupply(long initialBuySupply) {
        return param("initialBuySupply", initialBuySupply);
    }
}
