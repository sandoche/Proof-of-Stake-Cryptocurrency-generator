// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetPollVoteCall extends APICall.Builder<GetPollVoteCall> {
    private GetPollVoteCall() {
        super(ApiSpec.getPollVote);
    }

    public static GetPollVoteCall create() {
        return new GetPollVoteCall();
    }

    public GetPollVoteCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetPollVoteCall includeWeights(boolean includeWeights) {
        return param("includeWeights", includeWeights);
    }

    public GetPollVoteCall poll(String poll) {
        return param("poll", poll);
    }

    public GetPollVoteCall poll(long poll) {
        return unsignedLongParam("poll", poll);
    }

    public GetPollVoteCall account(String account) {
        return param("account", account);
    }

    public GetPollVoteCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetPollVoteCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
