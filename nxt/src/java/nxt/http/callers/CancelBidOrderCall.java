// Auto generated code, do not modify
package nxt.http.callers;

public class CancelBidOrderCall extends CreateTransactionCallBuilder<CancelBidOrderCall> {
    private CancelBidOrderCall() {
        super(ApiSpec.cancelBidOrder);
    }

    public static CancelBidOrderCall create() {
        return new CancelBidOrderCall();
    }

    public CancelBidOrderCall order(String order) {
        return param("order", order);
    }

    public CancelBidOrderCall order(long order) {
        return unsignedLongParam("order", order);
    }
}
