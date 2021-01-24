// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetShufflingParticipantsCall extends APICall.Builder<GetShufflingParticipantsCall> {
    private GetShufflingParticipantsCall() {
        super(ApiSpec.getShufflingParticipants);
    }

    public static GetShufflingParticipantsCall create() {
        return new GetShufflingParticipantsCall();
    }

    public GetShufflingParticipantsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetShufflingParticipantsCall shuffling(String shuffling) {
        return param("shuffling", shuffling);
    }

    public GetShufflingParticipantsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
