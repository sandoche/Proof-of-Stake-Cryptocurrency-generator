// Auto generated code, do not modify
package nxt.http.callers;

public class CurrencyReserveIncreaseCall extends CreateTransactionCallBuilder<CurrencyReserveIncreaseCall> {
    private CurrencyReserveIncreaseCall() {
        super(ApiSpec.currencyReserveIncrease);
    }

    public static CurrencyReserveIncreaseCall create() {
        return new CurrencyReserveIncreaseCall();
    }

    public CurrencyReserveIncreaseCall amountPerUnitNQT(long amountPerUnitNQT) {
        return param("amountPerUnitNQT", amountPerUnitNQT);
    }

    public CurrencyReserveIncreaseCall currency(String currency) {
        return param("currency", currency);
    }

    public CurrencyReserveIncreaseCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }
}
