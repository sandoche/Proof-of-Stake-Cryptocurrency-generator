// Auto generated code, do not modify
package nxt.http.callers;

public class ShufflingCancelCall extends CreateTransactionCallBuilder<ShufflingCancelCall> {
    private ShufflingCancelCall() {
        super(ApiSpec.shufflingCancel);
    }

    public static ShufflingCancelCall create() {
        return new ShufflingCancelCall();
    }

    public ShufflingCancelCall cancellingAccount(String cancellingAccount) {
        return param("cancellingAccount", cancellingAccount);
    }

    public ShufflingCancelCall shufflingStateHash(String shufflingStateHash) {
        return param("shufflingStateHash", shufflingStateHash);
    }

    public ShufflingCancelCall shuffling(String shuffling) {
        return param("shuffling", shuffling);
    }
}
