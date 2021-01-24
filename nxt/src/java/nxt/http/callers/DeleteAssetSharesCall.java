// Auto generated code, do not modify
package nxt.http.callers;

public class DeleteAssetSharesCall extends CreateTransactionCallBuilder<DeleteAssetSharesCall> {
    private DeleteAssetSharesCall() {
        super(ApiSpec.deleteAssetShares);
    }

    public static DeleteAssetSharesCall create() {
        return new DeleteAssetSharesCall();
    }

    public DeleteAssetSharesCall quantityQNT(long quantityQNT) {
        return param("quantityQNT", quantityQNT);
    }

    public DeleteAssetSharesCall asset(String asset) {
        return param("asset", asset);
    }

    public DeleteAssetSharesCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }
}
