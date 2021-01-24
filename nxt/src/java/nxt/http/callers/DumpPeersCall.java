// Auto generated code, do not modify
package nxt.http.callers;

import nxt.http.APICall;

public class DumpPeersCall extends APICall.Builder<DumpPeersCall> {
    private DumpPeersCall() {
        super(ApiSpec.dumpPeers);
    }

    public static DumpPeersCall create() {
        return new DumpPeersCall();
    }

    public DumpPeersCall weight(String weight) {
        return param("weight", weight);
    }

    public DumpPeersCall version(String version) {
        return param("version", version);
    }

    public DumpPeersCall connect(String connect) {
        return param("connect", connect);
    }

    public DumpPeersCall adminPassword(String adminPassword) {
        return param("adminPassword", adminPassword);
    }
}
