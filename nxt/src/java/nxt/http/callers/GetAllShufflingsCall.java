// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAllShufflingsCall extends APICall.Builder<GetAllShufflingsCall> {
    private GetAllShufflingsCall() {
        super(ApiSpec.getAllShufflings);
    }

    public static GetAllShufflingsCall create() {
        return new GetAllShufflingsCall();
    }

    public GetAllShufflingsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAllShufflingsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAllShufflingsCall includeHoldingInfo(boolean includeHoldingInfo) {
        return param("includeHoldingInfo", includeHoldingInfo);
    }

    public GetAllShufflingsCall finishedOnly(String finishedOnly) {
        return param("finishedOnly", finishedOnly);
    }

    public GetAllShufflingsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAllShufflingsCall includeFinished(boolean includeFinished) {
        return param("includeFinished", includeFinished);
    }

    public GetAllShufflingsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
