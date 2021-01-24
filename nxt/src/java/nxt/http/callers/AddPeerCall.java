// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class AddPeerCall extends APICall.Builder<AddPeerCall> {
    private AddPeerCall() {
        super(ApiSpec.addPeer);
    }

    public static AddPeerCall create() {
        return new AddPeerCall();
    }

    public AddPeerCall peer(String peer) {
        return param("peer", peer);
    }
}
