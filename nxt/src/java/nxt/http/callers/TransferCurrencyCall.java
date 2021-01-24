// Auto generated code, do not modify
package nxt.http.callers;

public class TransferCurrencyCall extends CreateTransactionCallBuilder<TransferCurrencyCall> {
    private TransferCurrencyCall() {
        super(ApiSpec.transferCurrency);
    }

    public static TransferCurrencyCall create() {
        return new TransferCurrencyCall();
    }

    public TransferCurrencyCall recipient(String recipient) {
        return param("recipient", recipient);
    }

    public TransferCurrencyCall recipient(long recipient) {
        return unsignedLongParam("recipient", recipient);
    }

    public TransferCurrencyCall currency(String currency) {
        return param("currency", currency);
    }

    public TransferCurrencyCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public TransferCurrencyCall units(long units) {
        return param("units", units);
    }
}
