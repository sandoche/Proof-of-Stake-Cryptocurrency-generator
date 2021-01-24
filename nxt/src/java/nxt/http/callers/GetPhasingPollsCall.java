// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetPhasingPollsCall extends APICall.Builder<GetPhasingPollsCall> {
    private GetPhasingPollsCall() {
        super(ApiSpec.getPhasingPolls);
    }

    public static GetPhasingPollsCall create() {
        return new GetPhasingPollsCall();
    }

    public GetPhasingPollsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetPhasingPollsCall countVotes(String countVotes) {
        return param("countVotes", countVotes);
    }

    public GetPhasingPollsCall transaction(String... transaction) {
        return param("transaction", transaction);
    }

    public GetPhasingPollsCall transaction(long... transaction) {
        return unsignedLongParam("transaction", transaction);
    }

    public GetPhasingPollsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
