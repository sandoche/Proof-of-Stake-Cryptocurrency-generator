// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetAccountLedgerCall extends APICall.Builder<GetAccountLedgerCall> {
    private GetAccountLedgerCall() {
        super(ApiSpec.getAccountLedger);
    }

    public static GetAccountLedgerCall create() {
        return new GetAccountLedgerCall();
    }

    public GetAccountLedgerCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetAccountLedgerCall includeTransactions(boolean includeTransactions) {
        return param("includeTransactions", includeTransactions);
    }

    public GetAccountLedgerCall holding(String holding) {
        return param("holding", holding);
    }

    public GetAccountLedgerCall holding(long holding) {
        return unsignedLongParam("holding", holding);
    }

    public GetAccountLedgerCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetAccountLedgerCall includeHoldingInfo(boolean includeHoldingInfo) {
        return param("includeHoldingInfo", includeHoldingInfo);
    }

    public GetAccountLedgerCall holdingType(byte holdingType) {
        return param("holdingType", holdingType);
    }

    public GetAccountLedgerCall eventType(String eventType) {
        return param("eventType", eventType);
    }

    public GetAccountLedgerCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetAccountLedgerCall event(String event) {
        return param("event", event);
    }

    public GetAccountLedgerCall event(long event) {
        return unsignedLongParam("event", event);
    }

    public GetAccountLedgerCall account(String account) {
        return param("account", account);
    }

    public GetAccountLedgerCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetAccountLedgerCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }
}
