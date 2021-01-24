// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class MarkHostCall extends APICall.Builder<MarkHostCall> {
    private MarkHostCall() {
        super(ApiSpec.markHost);
    }

    public static MarkHostCall create() {
        return new MarkHostCall();
    }

    public MarkHostCall date(String date) {
        return param("date", date);
    }

    public MarkHostCall host(String host) {
        return param("host", host);
    }

    public MarkHostCall weight(String weight) {
        return param("weight", weight);
    }

    public MarkHostCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }
}
