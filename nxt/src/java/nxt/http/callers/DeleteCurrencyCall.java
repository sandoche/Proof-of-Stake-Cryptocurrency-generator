// Auto generated code, do not modify
package nxt.http.callers;

public class DeleteCurrencyCall extends CreateTransactionCallBuilder<DeleteCurrencyCall> {
    private DeleteCurrencyCall() {
        super(ApiSpec.deleteCurrency);
    }

    public static DeleteCurrencyCall create() {
        return new DeleteCurrencyCall();
    }

    public DeleteCurrencyCall currency(String currency) {
        return param("currency", currency);
    }

    public DeleteCurrencyCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }
}
