// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetPeersCall extends APICall.Builder<GetPeersCall> {
    private GetPeersCall() {
        super(ApiSpec.getPeers);
    }

    public static GetPeersCall create() {
        return new GetPeersCall();
    }

    public GetPeersCall service(String... service) {
        return param("service", service);
    }

    public GetPeersCall active(String active) {
        return param("active", active);
    }

    public GetPeersCall state(String state) {
        return param("state", state);
    }

    public GetPeersCall includePeerInfo(boolean includePeerInfo) {
        return param("includePeerInfo", includePeerInfo);
    }
}
