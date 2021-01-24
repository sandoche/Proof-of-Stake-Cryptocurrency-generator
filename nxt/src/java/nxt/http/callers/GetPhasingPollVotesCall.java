// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetPhasingPollVotesCall extends APICall.Builder<GetPhasingPollVotesCall> {
    private GetPhasingPollVotesCall() {
        super(ApiSpec.getPhasingPollVotes);
    }

    public static GetPhasingPollVotesCall create() {
        return new GetPhasingPollVotesCall();
    }

    public GetPhasingPollVotesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetPhasingPollVotesCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetPhasingPollVotesCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetPhasingPollVotesCall transaction(String transaction) {
        return param("transaction", transaction);
    }

    public GetPhasingPollVotesCall transaction(long transaction) {
        return unsignedLongParam("transaction", transaction);
    }

    public GetPhasingPollVotesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
