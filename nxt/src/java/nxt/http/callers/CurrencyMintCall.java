// Auto generated code, do not modify
package nxt.http.callers;

public class CurrencyMintCall extends CreateTransactionCallBuilder<CurrencyMintCall> {
    private CurrencyMintCall() {
        super(ApiSpec.currencyMint);
    }

    public static CurrencyMintCall create() {
        return new CurrencyMintCall();
    }

    public CurrencyMintCall currency(String currency) {
        return param("currency", currency);
    }

    public CurrencyMintCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public CurrencyMintCall counter(long counter) {
        return param("counter", counter);
    }

    public CurrencyMintCall units(long units) {
        return param("units", units);
    }

    public CurrencyMintCall nonce(String nonce) {
        return param("nonce", nonce);
    }
}
