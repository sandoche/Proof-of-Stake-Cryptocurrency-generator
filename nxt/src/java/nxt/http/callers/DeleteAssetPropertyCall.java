// Auto generated code, do not modify
package nxt.http.callers;

public class DeleteAssetPropertyCall extends CreateTransactionCallBuilder<DeleteAssetPropertyCall> {
    private DeleteAssetPropertyCall() {
        super(ApiSpec.deleteAssetProperty);
    }

    public static DeleteAssetPropertyCall create() {
        return new DeleteAssetPropertyCall();
    }

    public DeleteAssetPropertyCall property(String property) {
        return param("property", property);
    }

    public DeleteAssetPropertyCall setter(String setter) {
        return param("setter", setter);
    }

    public DeleteAssetPropertyCall setter(long setter) {
        return unsignedLongParam("setter", setter);
    }

    public DeleteAssetPropertyCall asset(String asset) {
        return param("asset", asset);
    }

    public DeleteAssetPropertyCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }
}
