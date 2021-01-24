// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class DecodeQRCodeCall extends APICall.Builder<DecodeQRCodeCall> {
    private DecodeQRCodeCall() {
        super(ApiSpec.decodeQRCode);
    }

    public static DecodeQRCodeCall create() {
        return new DecodeQRCodeCall();
    }

    public DecodeQRCodeCall qrCodeBase64(String qrCodeBase64) {
        return param("qrCodeBase64", qrCodeBase64);
    }
}
