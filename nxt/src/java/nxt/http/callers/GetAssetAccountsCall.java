// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAssetAccountsCall extends APICall.Builder<GetAssetAccountsCall> {
    private GetAssetAccountsCall() {
        super(ApiSpec.getAssetAccounts);
    }

    public static GetAssetAccountsCall create() {
        return new GetAssetAccountsCall();
    }

    public GetAssetAccountsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAssetAccountsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAssetAccountsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAssetAccountsCall asset(String asset) {
        return param("asset", asset);
    }

    public GetAssetAccountsCall asset(long asset) {
        return unsignedLongParam("asset", asset);
    }

    public GetAssetAccountsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetAssetAccountsCall height(int height) {
        return param("height", height);
    }
}
