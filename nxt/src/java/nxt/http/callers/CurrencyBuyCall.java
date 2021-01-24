// Auto generated code, do not modify
package nxt.http.callers;

public class CurrencyBuyCall extends CreateTransactionCallBuilder<CurrencyBuyCall> {
    private CurrencyBuyCall() {
        super(ApiSpec.currencyBuy);
    }

    public static CurrencyBuyCall create() {
        return new CurrencyBuyCall();
    }

    public CurrencyBuyCall currency(String currency) {
        return param("currency", currency);
    }

    public CurrencyBuyCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public CurrencyBuyCall units(long units) {
        return param("units", units);
    }

    public CurrencyBuyCall rateNQT(long rateNQT) {
        return param("rateNQT", rateNQT);
    }
}
