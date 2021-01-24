// Auto generated code, do not modify
package nxt.http.callers;

public class SetAssetPropertyCall extends CreateTransactionCallBuilder<SetAssetPropertyCall> {
    private SetAssetPropertyCall() {
        super(ApiSpec.setAssetProperty);
    }

    public static SetAssetPropertyCall create() {
        return new SetAssetPropertyCall();
    }

    public SetAssetPropertyCall property(String property) {
        return param("property", property);
    }

    public SetAssetPropertyCall asset(String asset) {
        return param("asset", asset);
    }

    public SetAssetPropertyCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public SetAssetPropertyCall value(String value) {
        return param("value", value);
    }
}
