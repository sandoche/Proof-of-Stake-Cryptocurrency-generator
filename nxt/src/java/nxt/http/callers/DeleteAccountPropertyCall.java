// Auto generated code, do not modify
package nxt.http.callers;

public class DeleteAccountPropertyCall extends CreateTransactionCallBuilder<DeleteAccountPropertyCall> {
    private DeleteAccountPropertyCall() {
        super(ApiSpec.deleteAccountProperty);
    }

    public static DeleteAccountPropertyCall create() {
        return new DeleteAccountPropertyCall();
    }

    public DeleteAccountPropertyCall property(String property) {
        return param("property", property);
    }

    public DeleteAccountPropertyCall recipient(String recipient) {
        return param("recipient", recipient);
    }

    public DeleteAccountPropertyCall recipient(long recipient) {
        return unsignedLongParam("recipient", recipient);
    }

    public DeleteAccountPropertyCall setter(String setter) {
        return param("setter", setter);
    }

    public DeleteAccountPropertyCall setter(long setter) {
        return unsignedLongParam("setter", setter);
    }
}
