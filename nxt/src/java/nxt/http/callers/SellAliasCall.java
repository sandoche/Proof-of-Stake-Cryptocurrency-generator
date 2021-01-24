// Auto generated code, do not modify
package nxt.http.callers;

public class SellAliasCall extends CreateTransactionCallBuilder<SellAliasCall> {
    private SellAliasCall() {
        super(ApiSpec.sellAlias);
    }

    public static SellAliasCall create() {
        return new SellAliasCall();
    }

    public SellAliasCall priceNQT(long priceNQT) {
        return param("priceNQT", priceNQT);
    }

    public SellAliasCall aliasName(String aliasName) {
        return param("aliasName", aliasName);
    }

    public SellAliasCall recipient(String recipient) {
        return param("recipient", recipient);
    }

    public SellAliasCall recipient(long recipient) {
        return unsignedLongParam("recipient", recipient);
    }

    public SellAliasCall alias(String alias) {
        return param("alias", alias);
    }
}
