// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class ManagePeersNetworkingCall extends APICall.Builder<ManagePeersNetworkingCall> {
    private ManagePeersNetworkingCall() {
        super(ApiSpec.managePeersNetworking);
    }

    public static ManagePeersNetworkingCall create() {
        return new ManagePeersNetworkingCall();
    }

    public ManagePeersNetworkingCall operation(String operation) {
        return param("operation", operation);
    }
}
