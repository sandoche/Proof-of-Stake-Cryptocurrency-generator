// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetStateCall extends APICall.Builder<GetStateCall> {
    private GetStateCall() {
        super(ApiSpec.getState);
    }

    public static GetStateCall create() {
        return new GetStateCall();
    }

    public GetStateCall includeCounts(boolean includeCounts) {
        return param("includeCounts", includeCounts);
    }

    public GetStateCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
