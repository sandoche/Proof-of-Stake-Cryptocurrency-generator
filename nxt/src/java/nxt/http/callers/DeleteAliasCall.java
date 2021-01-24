// Auto generated code, do not modify
package nxt.http.callers;

public class DeleteAliasCall extends CreateTransactionCallBuilder<DeleteAliasCall> {
    private DeleteAliasCall() {
        super(ApiSpec.deleteAlias);
    }

    public static DeleteAliasCall create() {
        return new DeleteAliasCall();
    }

    public DeleteAliasCall aliasName(String aliasName) {
        return param("aliasName", aliasName);
    }

    public DeleteAliasCall alias(String alias) {
        return param("alias", alias);
    }
}
