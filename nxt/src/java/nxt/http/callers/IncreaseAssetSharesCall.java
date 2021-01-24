// Auto generated code, do not modify
package nxt.http.callers;

public class IncreaseAssetSharesCall extends CreateTransactionCallBuilder<IncreaseAssetSharesCall> {
    private IncreaseAssetSharesCall() {
        super(ApiSpec.increaseAssetShares);
    }

    public static IncreaseAssetSharesCall create() {
        return new IncreaseAssetSharesCall();
    }

    public IncreaseAssetSharesCall quantityQNT(long quantityQNT) {
        return param("quantityQNT", quantityQNT);
    }

    public IncreaseAssetSharesCall asset(String asset) {
        return param("asset", asset);
    }

    public IncreaseAssetSharesCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }
}
