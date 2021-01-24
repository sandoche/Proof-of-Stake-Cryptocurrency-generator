// Auto generated code, do not modify
package nxt.http.callers;

public class DgsFeedbackCall extends CreateTransactionCallBuilder<DgsFeedbackCall> {
    private DgsFeedbackCall() {
        super(ApiSpec.dgsFeedback);
    }

    public static DgsFeedbackCall create() {
        return new DgsFeedbackCall();
    }

    public DgsFeedbackCall purchase(String purchase) {
        return param("purchase", purchase);
    }

    public DgsFeedbackCall purchase(long purchase) {
        return unsignedLongParam("purchase", purchase);
    }
}
