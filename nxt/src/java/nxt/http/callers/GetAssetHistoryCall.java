// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAssetHistoryCall extends APICall.Builder<GetAssetHistoryCall> {
    private GetAssetHistoryCall() {
        super(ApiSpec.getAssetHistory);
    }

    public static GetAssetHistoryCall create() {
        return new GetAssetHistoryCall();
    }

    public GetAssetHistoryCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAssetHistoryCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAssetHistoryCall increasesOnly(String increasesOnly) {
        return param("increasesOnly", increasesOnly);
    }

    public GetAssetHistoryCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAssetHistoryCall includeAssetInfo(boolean includeAssetInfo) {
        return param("includeAssetInfo", includeAssetInfo);
    }

    public GetAssetHistoryCall asset(String asset) {
        return param("asset", asset);
    }

    public GetAssetHistoryCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public GetAssetHistoryCall account(String account) {
        return param("account", account);
    }

    public GetAssetHistoryCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetAssetHistoryCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAssetHistoryCall deletesOnly(String deletesOnly) {
        return param("deletesOnly", deletesOnly);
    }

    public GetAssetHistoryCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }
}
