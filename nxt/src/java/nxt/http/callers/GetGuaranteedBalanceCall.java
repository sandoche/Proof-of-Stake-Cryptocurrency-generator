// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetGuaranteedBalanceCall extends APICall.Builder<GetGuaranteedBalanceCall> {
    private GetGuaranteedBalanceCall() {
        super(ApiSpec.getGuaranteedBalance);
    }

    public static GetGuaranteedBalanceCall create() {
        return new GetGuaranteedBalanceCall();
    }

    public GetGuaranteedBalanceCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetGuaranteedBalanceCall numberOfConfirmations(String numberOfConfirmations) {
        return param("numberOfConfirmations", numberOfConfirmations);
    }

    public GetGuaranteedBalanceCall account(String account) {
        return param("account", account);
    }

    public GetGuaranteedBalanceCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetGuaranteedBalanceCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
