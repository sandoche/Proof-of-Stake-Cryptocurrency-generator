// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetMintingTargetCall extends APICall.Builder<GetMintingTargetCall> {
    private GetMintingTargetCall() {
        super(ApiSpec.getMintingTarget);
    }

    public static GetMintingTargetCall create() {
        return new GetMintingTargetCall();
    }

    public GetMintingTargetCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetMintingTargetCall currency(String currency) {
        return param("currency", currency);
    }

    public GetMintingTargetCall currency(long currency) {
        return unsignedLongParam("currency", currency);
    }

    public GetMintingTargetCall units(long units) {
        return param("units", units);
    }

    public GetMintingTargetCall account(String account) {
        return param("account", account);
    }

    public GetMintingTargetCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetMintingTargetCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
