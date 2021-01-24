// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetChannelTaggedDataCall extends APICall.Builder<GetChannelTaggedDataCall> {
    private GetChannelTaggedDataCall() {
        super(ApiSpec.getChannelTaggedData);
    }

    public static GetChannelTaggedDataCall create() {
        return new GetChannelTaggedDataCall();
    }

    public GetChannelTaggedDataCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetChannelTaggedDataCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetChannelTaggedDataCall channel(String channel) {
        return param("channel", channel);
    }

    public GetChannelTaggedDataCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetChannelTaggedDataCall includeData(boolean includeData) {
        return param("includeData", includeData);
    }

    public GetChannelTaggedDataCall account(String account) {
        return param("account", account);
    }

    public GetChannelTaggedDataCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetChannelTaggedDataCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
