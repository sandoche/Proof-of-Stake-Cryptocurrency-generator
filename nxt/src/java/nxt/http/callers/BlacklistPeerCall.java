// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class BlacklistPeerCall extends APICall.Builder<BlacklistPeerCall> {
    private BlacklistPeerCall() {
        super(ApiSpec.blacklistPeer);
    }

    public static BlacklistPeerCall create() {
        return new BlacklistPeerCall();
    }

    public BlacklistPeerCall peer(String peer) {
        return param("peer", peer);
    }
}
