// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetPrunableMessagesCall extends APICall.Builder<GetPrunableMessagesCall> {
    private GetPrunableMessagesCall() {
        super(ApiSpec.getPrunableMessages);
    }

    public static GetPrunableMessagesCall create() {
        return new GetPrunableMessagesCall();
    }

    public GetPrunableMessagesCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetPrunableMessagesCall otherAccount(String otherAccount) {
        return param("otherAccount", otherAccount);
    }

    public GetPrunableMessagesCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetPrunableMessagesCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetPrunableMessagesCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public GetPrunableMessagesCall account(String account) {
        return param("account", account);
    }

    public GetPrunableMessagesCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetPrunableMessagesCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetPrunableMessagesCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }
}
