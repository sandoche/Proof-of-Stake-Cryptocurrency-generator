// Auto generated code, do not modify
package nxt.http.callers;

public class SendMoneyCall extends CreateTransactionCallBuilder<SendMoneyCall> {
    private SendMoneyCall() {
        super(ApiSpec.sendMoney);
    }

    public static SendMoneyCall create() {
        return new SendMoneyCall();
    }

    public SendMoneyCall amountNQT(long amountNQT) {
        return param("amountNQT", amountNQT);
    }

    public SendMoneyCall recipient(String recipient) {
        return param("recipient", recipient);
    }

    public SendMoneyCall recipient(long recipient) {
        return unsignedLongParam("recipient", recipient);
    }
}
