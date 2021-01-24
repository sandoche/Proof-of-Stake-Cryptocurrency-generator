// Auto generated code, do not modify
package nxt.http.callers;

public class ScheduleCurrencyBuyCall extends CreateTransactionCallBuilder<ScheduleCurrencyBuyCall> {
    private ScheduleCurrencyBuyCall() {
        super(ApiSpec.scheduleCurrencyBuy);
    }

    public static ScheduleCurrencyBuyCall create() {
        return new ScheduleCurrencyBuyCall();
    }

    public ScheduleCurrencyBuyCall transactionJSON(String transactionJSON) {
        return param("transactionJSON", transactionJSON);
    }

    public ScheduleCurrencyBuyCall currency(String currency) {
        return param("currency", currency);
    }

    public ScheduleCurrencyBuyCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public ScheduleCurrencyBuyCall units(long units) {
        return param("units", units);
    }

    public ScheduleCurrencyBuyCall transactionBytes(String transactionBytes) {
        return param("transactionBytes", transactionBytes);
    }

    public ScheduleCurrencyBuyCall transactionBytes(byte[] transactionBytes) {
        return param("transactionBytes", transactionBytes);
    }

    public ScheduleCurrencyBuyCall prunableAttachmentJSON(String prunableAttachmentJSON) {
        return param("prunableAttachmentJSON", prunableAttachmentJSON);
    }

    public ScheduleCurrencyBuyCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }

    public ScheduleCurrencyBuyCall offerIssuer(boolean offerIssuer) {
        return param("offerIssuer", offerIssuer);
    }

    public ScheduleCurrencyBuyCall rateNQT(long rateNQT) {
        return param("rateNQT", rateNQT);
    }
}
