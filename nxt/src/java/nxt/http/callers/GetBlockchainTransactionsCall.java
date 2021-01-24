// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetBlockchainTransactionsCall extends APICall.Builder<GetBlockchainTransactionsCall> {
    private GetBlockchainTransactionsCall() {
        super(ApiSpec.getBlockchainTransactions);
    }

    public static GetBlockchainTransactionsCall create() {
        return new GetBlockchainTransactionsCall();
    }

    public GetBlockchainTransactionsCall requireLastBlock(String requireLastBlock) {
        return param("requireLastBlock", requireLastBlock);
    }

    public GetBlockchainTransactionsCall includeExpiredPrunable(boolean includeExpiredPrunable) {
        return param("includeExpiredPrunable", includeExpiredPrunable);
    }

    public GetBlockchainTransactionsCall numberOfConfirmations(String numberOfConfirmations) {
        return param("numberOfConfirmations", numberOfConfirmations);
    }

    public GetBlockchainTransactionsCall executedOnly(boolean executedOnly) {
        return param("executedOnly", executedOnly);
    }

    public GetBlockchainTransactionsCall type(int type) {
        return param("type", type);
    }

    public GetBlockchainTransactionsCall withMessage(String withMessage) {
        return param("withMessage", withMessage);
    }

    public GetBlockchainTransactionsCall firstIndex(int firstIndex) {
        return param("firstIndex", firstIndex);
    }

    public GetBlockchainTransactionsCall nonPhasedOnly(String nonPhasedOnly) {
        return param("nonPhasedOnly", nonPhasedOnly);
    }

    public GetBlockchainTransactionsCall subtype(int subtype) {
        return param("subtype", subtype);
    }

    public GetBlockchainTransactionsCall includePhasingResult(boolean includePhasingResult) {
        return param("includePhasingResult", includePhasingResult);
    }

    public GetBlockchainTransactionsCall phasedOnly(String phasedOnly) {
        return param("phasedOnly", phasedOnly);
    }

    public GetBlockchainTransactionsCall lastIndex(int lastIndex) {
        return param("lastIndex", lastIndex);
    }

    public GetBlockchainTransactionsCall account(String account) {
        return param("account", account);
    }

    public GetBlockchainTransactionsCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetBlockchainTransactionsCall requireBlock(String requireBlock) {
        return param("requireBlock", requireBlock);
    }

    public GetBlockchainTransactionsCall timestamp(int timestamp) {
        return param("timestamp", timestamp);
    }
}
