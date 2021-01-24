// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAllPhasingOnlyControlsCall extends APICall.Builder<GetAllPhasingOnlyControlsCall> {
    private GetAllPhasingOnlyControlsCall() {
        super(ApiSpec.getAllPhasingOnlyControls);
    }

    public static GetAllPhasingOnlyControlsCall create() {
        return new GetAllPhasingOnlyControlsCall();
    }

    public GetAllPhasingOnlyControlsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAllPhasingOnlyControlsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAllPhasingOnlyControlsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAllPhasingOnlyControlsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
