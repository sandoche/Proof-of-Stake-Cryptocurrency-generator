// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class CanDeleteCurrencyCall extends APICall.Builder<CanDeleteCurrencyCall> {
    private CanDeleteCurrencyCall() {
        super(ApiSpec.canDeleteCurrency);
    }

    public static CanDeleteCurrencyCall create() {
        return new CanDeleteCurrencyCall();
    }

    public CanDeleteCurrencyCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public CanDeleteCurrencyCall currency(String currency) {
        return param("currency", currency);
    }

    public CanDeleteCurrencyCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public CanDeleteCurrencyCall account(String account) {
        return param("account", account);
    }

    public CanDeleteCurrencyCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public CanDeleteCurrencyCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
