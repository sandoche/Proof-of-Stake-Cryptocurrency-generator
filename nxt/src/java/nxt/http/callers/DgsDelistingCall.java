// Auto generated code, do not modify
package nxt.http.callers;

public class DgsDelistingCall extends CreateTransactionCallBuilder<DgsDelistingCall> {
    private DgsDelistingCall() {
        super(ApiSpec.dgsDelisting);
    }

    public static DgsDelistingCall create() {
        return new DgsDelistingCall();
    }

    public DgsDelistingCall goods(String goods) {
        return param("goods", goods);
    }

    public DgsDelistingCall goods(long goods) {
        return unsignedLongParam("goods", goods);
    }
}
