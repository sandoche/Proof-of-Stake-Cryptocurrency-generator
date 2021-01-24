// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAssetPhasedTransactionsCall extends APICall.Builder<GetAssetPhasedTransactionsCall> {
    private GetAssetPhasedTransactionsCall() {
        super(ApiSpec.getAssetPhasedTransactions);
    }

    public static GetAssetPhasedTransactionsCall create() {
        return new GetAssetPhasedTransactionsCall();
    }

    public GetAssetPhasedTransactionsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAssetPhasedTransactionsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAssetPhasedTransactionsCall withoutWhitelist(String withoutWhitelist) {
        return param("withoutWhitelist", withoutWhitelist);
    }

    public GetAssetPhasedTransactionsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAssetPhasedTransactionsCall asset(String asset) {
        return param("asset", asset);
    }

    public GetAssetPhasedTransactionsCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public GetAssetPhasedTransactionsCall account(String account) {
        return param("account", account);
    }

    public GetAssetPhasedTransactionsCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetAssetPhasedTransactionsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
