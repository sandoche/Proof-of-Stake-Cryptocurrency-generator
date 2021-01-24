// Auto generated code, do not modify
package nxt.http.callers;

public class ShufflingProcessCall extends CreateTransactionCallBuilder<ShufflingProcessCall> {
    private ShufflingProcessCall() {
        super(ApiSpec.shufflingProcess);
    }

    public static ShufflingProcessCall create() {
        return new ShufflingProcessCall();
    }

    public ShufflingProcessCall recipientSecretPhrase(String recipientSecretPhrase) {
        return param("recipientSecretPhrase", recipientSecretPhrase);
    }

    public ShufflingProcessCall shuffling(String shuffling) {
        return param("shuffling", shuffling);
    }
}
