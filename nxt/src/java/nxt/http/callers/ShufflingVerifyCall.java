// Auto generated code, do not modify
package nxt.http.callers;

public class ShufflingVerifyCall extends CreateTransactionCallBuilder<ShufflingVerifyCall> {
    private ShufflingVerifyCall() {
        super(ApiSpec.shufflingVerify);
    }

    public static ShufflingVerifyCall create() {
        return new ShufflingVerifyCall();
    }

    public ShufflingVerifyCall shufflingStateHash(String shufflingStateHash) {
        return param("shufflingStateHash", shufflingStateHash);
    }

    public ShufflingVerifyCall shuffling(String shuffling) {
        return param("shuffling", shuffling);
    }
}
