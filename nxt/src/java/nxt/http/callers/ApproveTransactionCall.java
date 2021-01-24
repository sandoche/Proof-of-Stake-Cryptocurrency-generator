// Auto generated code, do not modify
package nxt.http.callers;

public class ApproveTransactionCall extends CreateTransactionCallBuilder<ApproveTransactionCall> {
    private ApproveTransactionCall() {
        super(ApiSpec.approveTransaction);
    }

    public static ApproveTransactionCall create() {
        return new ApproveTransactionCall();
    }

    public ApproveTransactionCall revealedSecret(String revealedSecret) {
        return param("revealedSecret", revealedSecret);
    }

    public ApproveTransactionCall transactionFullHash(String... transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }

    public ApproveTransactionCall transactionFullHash(byte[]... transactionFullHash) {
        return param("transactionFullHash", transactionFullHash);
    }

    public ApproveTransactionCall revealedSecretIsText(boolean revealedSecretIsText) {
        return param("revealedSecretIsText", revealedSecretIsText);
    }
}
