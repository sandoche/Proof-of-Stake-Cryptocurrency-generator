// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class CalculateFullHashCall extends APICall.Builder<CalculateFullHashCall> {
    private CalculateFullHashCall() {
        super(ApiSpec.calculateFullHash);
    }

    public static CalculateFullHashCall create() {
        return new CalculateFullHashCall();
    }

    public CalculateFullHashCall signatureHash(String signatureHash) {
        return param("signatureHash", signatureHash);
    }

    public CalculateFullHashCall unsignedTransactionBytes(String unsignedTransactionBytes) {
        return param("unsignedTransactionBytes", unsignedTransactionBytes);
    }

    public CalculateFullHashCall unsignedTransactionBytes(byte[] unsignedTransactionBytes) {
        return param("unsignedTransactionBytes", unsignedTransactionBytes);
    }

    public CalculateFullHashCall unsignedTransactionJSON(String unsignedTransactionJSON) {
        return param("unsignedTransactionJSON", unsignedTransactionJSON);
    }
}
