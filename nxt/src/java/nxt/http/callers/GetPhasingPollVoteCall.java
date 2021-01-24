// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetPhasingPollVoteCall extends APICall.Builder<GetPhasingPollVoteCall> {
    private GetPhasingPollVoteCall() {
        super(ApiSpec.getPhasingPollVote);
    }

    public static GetPhasingPollVoteCall create() {
        return new GetPhasingPollVoteCall();
    }

    public GetPhasingPollVoteCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetPhasingPollVoteCall account(String account) {
        return param("account", account);
    }

    public GetPhasingPollVoteCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetPhasingPollVoteCall transaction(String transaction) {
        return param("transaction", transaction);
    }

    public GetPhasingPollVoteCall transaction(long transaction) {
        return unsignedLongParam("transaction", transaction);
    }

    public GetPhasingPollVoteCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
