// Auto generated code, do not modify
package nxt.http.callers;

public class LeaseBalanceCall extends CreateTransactionCallBuilder<LeaseBalanceCall> {
    private LeaseBalanceCall() {
        super(ApiSpec.leaseBalance);
    }

    public static LeaseBalanceCall create() {
        return new LeaseBalanceCall();
    }

    public LeaseBalanceCall period(String period) {
        return param("period", period);
    }

    public LeaseBalanceCall recipient(String recipient) {
        return param("recipient", recipient);
    }

    public LeaseBalanceCall recipient(long recipient) {
        return unsignedLongParam("recipient", recipient);
    }
}
