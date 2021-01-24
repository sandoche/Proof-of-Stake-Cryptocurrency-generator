// Auto generated code, do not modify
package nxt.http.callers;

public class PlaceAskOrderCall extends CreateTransactionCallBuilder<PlaceAskOrderCall> {
    private PlaceAskOrderCall() {
        super(ApiSpec.placeAskOrder);
    }

    public static PlaceAskOrderCall create() {
        return new PlaceAskOrderCall();
    }

    public PlaceAskOrderCall priceNQT(long priceNQT) {
        return param("priceNQT", priceNQT);
    }

    public PlaceAskOrderCall quantityQNT(long quantityQNT) {
        return param("quantityQNT", quantityQNT);
    }

    public PlaceAskOrderCall asset(String asset) {
        return param("asset", asset);
    }

    public PlaceAskOrderCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }
}
