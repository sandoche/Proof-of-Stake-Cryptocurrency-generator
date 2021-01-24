// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetPollVotesCall extends APICall.Builder<GetPollVotesCall> {
    private GetPollVotesCall() {
        super(ApiSpec.getPollVotes);
    }

    public static GetPollVotesCall create() {
        return new GetPollVotesCall();
    }

    public GetPollVotesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetPollVotesCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetPollVotesCall includeWeights(boolean includeWeights) {
        return param("includeWeights", includeWeights);
    }

    public GetPollVotesCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetPollVotesCall poll(String poll) {
        return param("poll", poll);
    }

    public GetPollVotesCall poll(long poll) {
        return unsignedLongParam("poll", poll);
    }

    public GetPollVotesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
