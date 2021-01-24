// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class GetInboundPeersCall extends APICall.Builder<GetInboundPeersCall> {
    private GetInboundPeersCall() {
        super(ApiSpec.getInboundPeers);
    }

    public static GetInboundPeersCall create() {
        return new GetInboundPeersCall();
    }

    public GetInboundPeersCall includePeerInfo(boolean includePeerInfo) {
        return param("includePeerInfo", includePeerInfo);
    }
}
