// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAssignedShufflingsCall extends APICall.Builder<GetAssignedShufflingsCall> {
    private GetAssignedShufflingsCall() {
        super(ApiSpec.getAssignedShufflings);
    }

    public static GetAssignedShufflingsCall create() {
        return new GetAssignedShufflingsCall();
    }

    public GetAssignedShufflingsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAssignedShufflingsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAssignedShufflingsCall includeHoldingInfo(boolean includeHoldingInfo) {
        return param("includeHoldingInfo", includeHoldingInfo);
    }

    public GetAssignedShufflingsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAssignedShufflingsCall account(String account) {
        return param("account", account);
    }

    public GetAssignedShufflingsCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetAssignedShufflingsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
