// Auto generated code, do not modify
package nxt.http.callers;

public class PlaceBidOrderCall extends CreateTransactionCallBuilder<PlaceBidOrderCall> {
    private PlaceBidOrderCall() {
        super(ApiSpec.placeBidOrder);
    }

    public static PlaceBidOrderCall create() {
        return new PlaceBidOrderCall();
    }

    public PlaceBidOrderCall priceNQT(long priceNQT) {
        return param("priceNQT", priceNQT);
    }

    public PlaceBidOrderCall quantityQNT(long quantityQNT) {
        return param("quantityQNT", quantityQNT);
    }

    public PlaceBidOrderCall asset(String asset) {
        return param("asset", asset);
    }

    public PlaceBidOrderCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }
}
