// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetPhasingPollCall extends APICall.Builder<GetPhasingPollCall> {
    private GetPhasingPollCall() {
        super(ApiSpec.getPhasingPoll);
    }

    public static GetPhasingPollCall create() {
        return new GetPhasingPollCall();
    }

    public GetPhasingPollCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetPhasingPollCall countVotes(String countVotes) {
        return param("countVotes", countVotes);
    }

    public GetPhasingPollCall transaction(String transaction) {
        return param("transaction", transaction);
    }

    public GetPhasingPollCall transaction(long transaction) {
        return unsignedLongParam("transaction", transaction);
    }

    public GetPhasingPollCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
