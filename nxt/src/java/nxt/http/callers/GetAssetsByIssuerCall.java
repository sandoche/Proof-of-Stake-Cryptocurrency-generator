// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAssetsByIssuerCall extends APICall.Builder<GetAssetsByIssuerCall> {
    private GetAssetsByIssuerCall() {
        super(ApiSpec.getAssetsByIssuer);
    }

    public static GetAssetsByIssuerCall create() {
        return new GetAssetsByIssuerCall();
    }

    public GetAssetsByIssuerCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAssetsByIssuerCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAssetsByIssuerCall includeCounts(boolean includeCounts) {
        return param("includeCounts", includeCounts);
    }

    public GetAssetsByIssuerCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAssetsByIssuerCall account(String... account) {
        return param("account", account);
    }

    public GetAssetsByIssuerCall account(long... account) {
        return unsignedLongParam("account", account);
    }

    public GetAssetsByIssuerCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
