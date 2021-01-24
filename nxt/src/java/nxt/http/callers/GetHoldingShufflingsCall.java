// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetHoldingShufflingsCall extends APICall.Builder<GetHoldingShufflingsCall> {
    private GetHoldingShufflingsCall() {
        super(ApiSpec.getHoldingShufflings);
    }

    public static GetHoldingShufflingsCall create() {
        return new GetHoldingShufflingsCall();
    }

    public GetHoldingShufflingsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetHoldingShufflingsCall holding(String holding) {
        return param("holding", holding);
    }

    public GetHoldingShufflingsCall holding(long holding) {
        return unsignedLongParam("holding", holding);
    }

    public GetHoldingShufflingsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetHoldingShufflingsCall stage(String stage) {
        return param("stage", stage);
    }

    public GetHoldingShufflingsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetHoldingShufflingsCall includeFinished(boolean includeFinished) {
        return param("includeFinished", includeFinished);
    }

    public GetHoldingShufflingsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
