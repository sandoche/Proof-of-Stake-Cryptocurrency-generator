// Auto generated code, do not modify
package nxt.http.callers;

public class SetPhasingOnlyControlCall extends CreateTransactionCallBuilder<SetPhasingOnlyControlCall> {
    private SetPhasingOnlyControlCall() {
        super(ApiSpec.setPhasingOnlyControl);
    }

    public static SetPhasingOnlyControlCall create() {
        return new SetPhasingOnlyControlCall();
    }

    public SetPhasingOnlyControlCall controlHolding(String controlHolding) {
        return param("controlHolding", controlHolding);
    }

    public SetPhasingOnlyControlCall controlHolding(long controlHolding) {
        return unsignedLongParam("controlHolding", controlHolding);
    }

    public SetPhasingOnlyControlCall controlQuorum(long controlQuorum) {
        return param("controlQuorum", controlQuorum);
    }

    public SetPhasingOnlyControlCall controlMinBalanceModel(byte controlMinBalanceModel) {
        return param("controlMinBalanceModel", controlMinBalanceModel);
    }

    public SetPhasingOnlyControlCall controlMaxFees(String controlMaxFees) {
        return param("controlMaxFees", controlMaxFees);
    }

    public SetPhasingOnlyControlCall controlMinDuration(String controlMinDuration) {
        return param("controlMinDuration", controlMinDuration);
    }

    public SetPhasingOnlyControlCall controlVotingModel(byte controlVotingModel) {
        return param("controlVotingModel", controlVotingModel);
    }

    public SetPhasingOnlyControlCall controlMaxDuration(String controlMaxDuration) {
        return param("controlMaxDuration", controlMaxDuration);
    }

    public SetPhasingOnlyControlCall controlMinBalance(long controlMinBalance) {
        return param("controlMinBalance", controlMinBalance);
    }

    public SetPhasingOnlyControlCall controlWhitelisted(String... controlWhitelisted) {
        return param("controlWhitelisted", controlWhitelisted);
    }
}
