// Auto generated code, do not modify
package nxt.http.callers;

public class SetAccountInfoCall extends CreateTransactionCallBuilder<SetAccountInfoCall> {
    private SetAccountInfoCall() {
        super(ApiSpec.setAccountInfo);
    }

    public static SetAccountInfoCall create() {
        return new SetAccountInfoCall();
    }

    public SetAccountInfoCall name(String name) {
        return param("name", name);
    }

    public SetAccountInfoCall description(String description) {
        return param("description", description);
    }
}
