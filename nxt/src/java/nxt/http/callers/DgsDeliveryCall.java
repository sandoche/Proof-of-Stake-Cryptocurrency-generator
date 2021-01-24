// Auto generated code, do not modify
package nxt.http.callers;

public class DgsDeliveryCall extends CreateTransactionCallBuilder<DgsDeliveryCall> {
    private DgsDeliveryCall() {
        super(ApiSpec.dgsDelivery);
    }

    public static DgsDeliveryCall create() {
        return new DgsDeliveryCall();
    }

    public DgsDeliveryCall goodsIsText(boolean goodsIsText) {
        return param("goodsIsText", goodsIsText);
    }

    public DgsDeliveryCall discountNQT(long discountNQT) {
        return param("discountNQT", discountNQT);
    }

    public DgsDeliveryCall goodsData(String goodsData) {
        return param("goodsData", goodsData);
    }

    public DgsDeliveryCall purchase(String purchase) {
        return param("purchase", purchase);
    }

    public DgsDeliveryCall purchase(long purchase) {
        return unsignedLongParam("purchase", purchase);
    }

    public DgsDeliveryCall goodsToEncrypt(String goodsToEncrypt) {
        return param("goodsToEncrypt", goodsToEncrypt);
    }

    public DgsDeliveryCall goodsNonce(String goodsNonce) {
        return param("goodsNonce", goodsNonce);
    }

    public DgsDeliveryCall goodsNonce(byte[] goodsNonce) {
        return param("goodsNonce", goodsNonce);
    }
}
