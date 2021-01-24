// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAccountBlocksCall extends APICall.Builder<GetAccountBlocksCall> {
    private GetAccountBlocksCall() {
        super(ApiSpec.getAccountBlocks);
    }

    public static GetAccountBlocksCall create() {
        return new GetAccountBlocksCall();
    }

    public GetAccountBlocksCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAccountBlocksCall includeTransactions(boolean includeTransactions) {
        return param("includeTransactions", includeTransactions);
    }

    public GetAccountBlocksCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAccountBlocksCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAccountBlocksCall account(String account) {
        return param("account", account);
    }

    public GetAccountBlocksCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetAccountBlocksCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAccountBlocksCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }
}
