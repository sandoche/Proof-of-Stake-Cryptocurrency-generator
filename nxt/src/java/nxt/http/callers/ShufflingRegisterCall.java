// Auto generated code, do not modify
package nxt.http.callers;

public class ShufflingRegisterCall extends CreateTransactionCallBuilder<ShufflingRegisterCall> {
    private ShufflingRegisterCall() {
        super(ApiSpec.shufflingRegister);
    }

    public static ShufflingRegisterCall create() {
        return new ShufflingRegisterCall();
    }

    public ShufflingRegisterCall shufflingFullHash(String shufflingFullHash) {
        return param("shufflingFullHash", shufflingFullHash);
    }

    public ShufflingRegisterCall shufflingFullHash(byte[] shufflingFullHash) {
        return param("shufflingFullHash", shufflingFullHash);
    }
}
