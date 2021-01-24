// Auto generated code, do not modify
package nxt.http.callers;

public class CreatePollCall extends CreateTransactionCallBuilder<CreatePollCall> {
    private CreatePollCall() {
        super(ApiSpec.createPoll);
    }

    public static CreatePollCall create() {
        return new CreatePollCall();
    }

    public CreatePollCall minRangeValue(long minRangeValue) {
        return param("minRangeValue", minRangeValue);
    }

    public CreatePollCall votingModel(byte votingModel) {
        return param("votingModel", votingModel);
    }

    public CreatePollCall description(String description) {
        return param("description", description);
    }

    public CreatePollCall holding(String holding) {
        return param("holding", holding);
    }

    public CreatePollCall holding(long holding) {
        return unsignedLongParam("holding", holding);
    }

    public CreatePollCall minNumberOfOptions(long minNumberOfOptions) {
        return param("minNumberOfOptions", minNumberOfOptions);
    }

    public CreatePollCall minBalance(long minBalance) {
        return param("minBalance", minBalance);
    }

    public CreatePollCall finishHeight(long finishHeight) {
        return param("finishHeight", finishHeight);
    }

    public CreatePollCall name(String name) {
        return param("name", name);
    }

    public CreatePollCall maxNumberOfOptions(long maxNumberOfOptions) {
        return param("maxNumberOfOptions", maxNumberOfOptions);
    }

    public CreatePollCall option01(String option01) {
        return param("option01", option01);
    }

    public CreatePollCall minBalanceModel(byte minBalanceModel) {
        return param("minBalanceModel", minBalanceModel);
    }

    public CreatePollCall option02(String option02) {
        return param("option02", option02);
    }

    public CreatePollCall option00(String option00) {
        return param("option00", option00);
    }

    public CreatePollCall maxRangeValue(long maxRangeValue) {
        return param("maxRangeValue", maxRangeValue);
    }
}
