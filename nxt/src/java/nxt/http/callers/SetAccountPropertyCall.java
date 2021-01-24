// Auto generated code, do not modify
package nxt.http.callers;

public class SetAccountPropertyCall extends CreateTransactionCallBuilder<SetAccountPropertyCall> {
    private SetAccountPropertyCall() {
        super(ApiSpec.setAccountProperty);
    }

    public static SetAccountPropertyCall create() {
        return new SetAccountPropertyCall();
    }

    public SetAccountPropertyCall property(String property) {
        return param("property", property);
    }

    public SetAccountPropertyCall recipient(String recipient) {
        return param("recipient", recipient);
    }

    public SetAccountPropertyCall recipient(long recipient) {
        return unsignedLongParam("recipient", recipient);
    }

    public SetAccountPropertyCall value(String value) {
        return param("value", value);
    }
}
