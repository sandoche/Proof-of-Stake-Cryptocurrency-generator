// Auto generated code, do not modify
package nxt.http.callers;

public class DgsQuantityChangeCall extends CreateTransactionCallBuilder<DgsQuantityChangeCall> {
    private DgsQuantityChangeCall() {
        super(ApiSpec.dgsQuantityChange);
    }

    public static DgsQuantityChangeCall create() {
        return new DgsQuantityChangeCall();
    }

    public DgsQuantityChangeCall goods(String goods) {
        return param("goods", goods);
    }

    public DgsQuantityChangeCall goods(long goods) {
        return unsignedLongParam("goods", goods);
    }

    public DgsQuantityChangeCall deltaQuantity(String deltaQuantity) {
        return param("deltaQuantity", deltaQuantity);
    }
}
