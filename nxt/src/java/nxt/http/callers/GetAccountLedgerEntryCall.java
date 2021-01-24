// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAccountLedgerEntryCall extends APICall.Builder<GetAccountLedgerEntryCall> {
    private GetAccountLedgerEntryCall() {
        super(ApiSpec.getAccountLedgerEntry);
    }

    public static GetAccountLedgerEntryCall create() {
        return new GetAccountLedgerEntryCall();
    }

    public GetAccountLedgerEntryCall ledgerId(String ledgerId) {
        return param("ledgerId", ledgerId);
    }

    public GetAccountLedgerEntryCall ledgerId(long ledgerId) {
        return unsignedLongParam("ledgerId", ledgerId);
    }

    public GetAccountLedgerEntryCall includeHoldingInfo(boolean includeHoldingInfo) {
        return param("includeHoldingInfo", includeHoldingInfo);
    }

    public GetAccountLedgerEntryCall includeTransaction(boolean includeTransaction) {
        return param("includeTransaction", includeTransaction);
    }
}
