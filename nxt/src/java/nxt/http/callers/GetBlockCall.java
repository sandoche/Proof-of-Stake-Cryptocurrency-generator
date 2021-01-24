// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetBlockCall extends APICall.Builder<GetBlockCall> {
    private GetBlockCall() {
        super(ApiSpec.getBlock);
    }

    public static GetBlockCall create() {
        return new GetBlockCall();
    }

    public GetBlockCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetBlockCall includeExecutedPhased(boolean includeExecutedPhased) {
        return param("includeExecutedPhased", includeExecutedPhased);
    }

    public GetBlockCall includeTransactions(boolean includeTransactions) {
        return param("includeTransactions", includeTransactions);
    }

    public GetBlockCall block(String block) {
        return param("block", block);
    }

    public GetBlockCall block(long block) {
        return unsignedLongParam("block", block);
    }

    public GetBlockCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetBlockCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }

    public GetBlockCall height(int height) {
        return param("height", height);
    }
}
