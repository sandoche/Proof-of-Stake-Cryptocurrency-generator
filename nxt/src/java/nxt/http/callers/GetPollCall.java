// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetPollCall extends APICall.Builder<GetPollCall> {
    private GetPollCall() {
        super(ApiSpec.getPoll);
    }

    public static GetPollCall create() {
        return new GetPollCall();
    }

    public GetPollCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetPollCall poll(String poll) {
        return param("poll", poll);
    }

    public GetPollCall poll(long poll) {
        return unsignedLongParam("poll", poll);
    }

    public GetPollCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
