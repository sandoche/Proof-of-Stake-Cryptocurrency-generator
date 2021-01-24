// Auto generated code, do not modify
package nxt.http.callers;

public class CurrencySellCall extends CreateTransactionCallBuilder<CurrencySellCall> {
    private CurrencySellCall() {
        super(ApiSpec.currencySell);
    }

    public static CurrencySellCall create() {
        return new CurrencySellCall();
    }

    public CurrencySellCall currency(String currency) {
        return param("currency", currency);
    }

    public CurrencySellCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public CurrencySellCall units(long units) {
        return param("units", units);
    }

    public CurrencySellCall rateNQT(long rateNQT) {
        return param("rateNQT", rateNQT);
    }
}
