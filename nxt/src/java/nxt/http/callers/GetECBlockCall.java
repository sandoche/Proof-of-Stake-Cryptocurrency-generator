// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetECBlockCall extends APICall.Builder<GetECBlockCall> {
    private GetECBlockCall() {
        super(ApiSpec.getECBlock);
    }

    public static GetECBlockCall create() {
        return new GetECBlockCall();
    }

    public GetECBlockCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetECBlockCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetECBlockCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }
}
