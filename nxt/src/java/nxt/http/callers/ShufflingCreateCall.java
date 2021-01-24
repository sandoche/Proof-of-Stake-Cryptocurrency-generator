// Auto generated code, do not modify
package nxt.http.callers;

public class ShufflingCreateCall extends CreateTransactionCallBuilder<ShufflingCreateCall> {
    private ShufflingCreateCall() {
        super(ApiSpec.shufflingCreate);
    }

    public static ShufflingCreateCall create() {
        return new ShufflingCreateCall();
    }

    public ShufflingCreateCall holding(String holding) {
        return param("holding", holding);
    }

    public ShufflingCreateCall holding(long holding) {
        return unsignedLongParam("holding", holding);
    }

    public ShufflingCreateCall amount(String amount) {
        return param("amount", amount);
    }

    public ShufflingCreateCall registrationPeriod(String registrationPeriod) {
        return param("registrationPeriod", registrationPeriod);
    }

    public ShufflingCreateCall participantCount(String participantCount) {
        return param("participantCount", participantCount);
    }

    public ShufflingCreateCall holdingType(byte holdingType) {
        return param("holdingType", holdingType);
    }
}
