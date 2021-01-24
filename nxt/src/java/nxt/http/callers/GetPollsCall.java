// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetPollsCall extends APICall.Builder<GetPollsCall> {
    private GetPollsCall() {
        super(ApiSpec.getPolls);
    }

    public static GetPollsCall create() {
        return new GetPollsCall();
    }

    public GetPollsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetPollsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetPollsCall finishedOnly(String finishedOnly) {
        return param("finishedOnly", finishedOnly);
    }

    public GetPollsCall includeFinished(boolean includeFinished) {
        return param("includeFinished", includeFinished);
    }

    public GetPollsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetPollsCall account(String account) {
        return param("account", account);
    }

    public GetPollsCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetPollsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetPollsCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }
}
