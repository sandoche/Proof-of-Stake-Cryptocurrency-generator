// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class EncodeQRCodeCall extends APICall.Builder<EncodeQRCodeCall> {
    private EncodeQRCodeCall() {
        super(ApiSpec.encodeQRCode);
    }

    public static EncodeQRCodeCall create() {
        return new EncodeQRCodeCall();
    }

    public EncodeQRCodeCall qrCodeData(String qrCodeData) {
        return param("qrCodeData", qrCodeData);
    }

    public EncodeQRCodeCall width(String width) {
        return param("width", width);
    }

    public EncodeQRCodeCall height(int height) {
        return param("height", height);
    }
}
