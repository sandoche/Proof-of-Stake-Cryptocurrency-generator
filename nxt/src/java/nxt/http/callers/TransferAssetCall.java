// Auto generated code, do not modify
package nxt.http.callers;

public class TransferAssetCall extends CreateTransactionCallBuilder<TransferAssetCall> {
    private TransferAssetCall() {
        super(ApiSpec.transferAsset);
    }

    public static TransferAssetCall create() {
        return new TransferAssetCall();
    }

    public TransferAssetCall quantityQNT(long quantityQNT) {
        return param("quantityQNT", quantityQNT);
    }

    public TransferAssetCall recipient(String recipient) {
        return param("recipient", recipient);
    }

    public TransferAssetCall recipient(long recipient) {
        return unsignedLongParam("recipient", recipient);
    }

    public TransferAssetCall asset(String asset) {
        return param("asset", asset);
    }

    public TransferAssetCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }
}
