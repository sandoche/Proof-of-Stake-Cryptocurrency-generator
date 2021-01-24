// Auto generated code, do not modify
package nxt.http.callers;

public class SendMessageCall extends CreateTransactionCallBuilder<SendMessageCall> {
    private SendMessageCall() {
        super(ApiSpec.sendMessage);
    }

    public static SendMessageCall create() {
        return new SendMessageCall();
    }

    public SendMessageCall recipient(String recipient) {
        return param("recipient", recipient);
    }

    public SendMessageCall recipient(long recipient) {
        return unsignedLongParam("recipient", recipient);
    }
}
