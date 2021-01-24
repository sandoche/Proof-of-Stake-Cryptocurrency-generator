// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetFundingMonitorCall extends APICall.Builder<GetFundingMonitorCall> {
    private GetFundingMonitorCall() {
        super(ApiSpec.getFundingMonitor);
    }

    public static GetFundingMonitorCall create() {
        return new GetFundingMonitorCall();
    }

    public GetFundingMonitorCall includeMonitoredAccounts(boolean includeMonitoredAccounts) {
        return param("includeMonitoredAccounts", includeMonitoredAccounts);
    }

    public GetFundingMonitorCall holding(String holding) {
        return param("holding", holding);
    }

    public GetFundingMonitorCall holding(long holding) {
        return unsignedLongParam("holding", holding);
    }

    public GetFundingMonitorCall holdingType(byte holdingType) {
        return param("holdingType", holdingType);
    }

    public GetFundingMonitorCall property(String property) {
        return param("property", property);
    }

    public GetFundingMonitorCall secretPhrase(String secretPhrase) {
        return param("secretPhrase", secretPhrase);
    }

    public GetFundingMonitorCall account(String account) {
        return param("account", account);
    }

    public GetFundingMonitorCall account(long account) {
        return unsignedLongParam("account", account);
    }

    public GetFundingMonitorCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
