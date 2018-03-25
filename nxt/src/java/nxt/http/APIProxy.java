/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2018 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of the Nxt software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package nxt.http;

import nxt.Constants;
import nxt.Nxt;
import nxt.peer.Peer;
import nxt.peer.Peers;
import nxt.util.Logger;
import nxt.util.ThreadPool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class APIProxy {
    public static final Set<String> NOT_FORWARDED_REQUESTS;

    private static final APIProxy instance = new APIProxy();

    static final boolean enableAPIProxy = Constants.isLightClient ||
            (Nxt.getBooleanProperty("nxt.enableAPIProxy") && ! API.isOpenAPI);
    private static final int blacklistingPeriod = Nxt.getIntProperty("nxt.apiProxyBlacklistingPeriod") / 1000;
    static final String forcedServerURL = Nxt.getStringProperty("nxt.forceAPIProxyServerURL", "");

    private volatile String forcedPeerHost;
    private volatile List<String> peersHosts = Collections.emptyList();
    private volatile String mainPeerAnnouncedAddress;

    private final Map<String, Integer> blacklistedPeers = new ConcurrentHashMap<>();

    static {
        Set<String> requests = new HashSet<>();
        requests.add("getBlockchainStatus");
        requests.add("getState");

        final EnumSet<APITag> notForwardedTags = EnumSet.of(APITag.DEBUG, APITag.NETWORK);

        for (APIEnum api : APIEnum.values()) {
            APIServlet.APIRequestHandler handler = api.getHandler();
            if (handler.requireBlockchain() && !Collections.disjoint(handler.getAPITags(), notForwardedTags)) {
                requests.add(api.getName());
            }
        }

        NOT_FORWARDED_REQUESTS = Collections.unmodifiableSet(requests);
    }

    private static final Runnable peersUpdateThread = () -> {
        int curTime = Nxt.getEpochTime();
        instance.blacklistedPeers.entrySet().removeIf((entry) -> {
            if (entry.getValue() < curTime) {
                Logger.logDebugMessage("Unblacklisting API peer " + entry.getKey());
                return true;
            }
            return false;
        });
        List<String> currentPeersHosts = instance.peersHosts;
        if (currentPeersHosts != null) {
            for (String host : currentPeersHosts) {
                Peer peer = Peers.getPeer(host);
                if (peer != null) {
                    Peers.connectPeer(peer);
                }
            }
        }
    };

    static {
        if (!Constants.isOffline && enableAPIProxy) {
            ThreadPool.scheduleThread("APIProxyPeersUpdate", peersUpdateThread, 60);
        }
    }

    private APIProxy() {

    }

    public static void init() {}

    public static APIProxy getInstance() {
        return instance;
    }

    Peer getServingPeer(String requestType) {
        if (forcedPeerHost != null) {
            return Peers.getPeer(forcedPeerHost);
        }

        APIEnum requestAPI = APIEnum.fromName(requestType);
        if (!peersHosts.isEmpty()) {
            for (String host : peersHosts) {
                Peer peer = Peers.getPeer(host);
                if (peer != null && peer.isApiConnectable() && !peer.getDisabledAPIs().contains(requestAPI)) {
                    return peer;
                }
            }
        }

        List<Peer> connectablePeers = Peers.getPeers(p -> p.isApiConnectable() && !blacklistedPeers.containsKey(p.getHost()));
        if (connectablePeers.isEmpty()) {
            return null;
        }
        // subset of connectable peers that have at least one new API enabled, which was disabled for the
        // The first peer (element 0 of peersHosts) is chosen at random. Next peers are chosen randomly from a
        // previously chosen peers. In worst case the size of peersHosts will be the number of APIs
        Peer peer = getRandomAPIPeer(connectablePeers);
        if (peer == null) {
            return null;
        }

        Peer resultPeer = null;
        List<String> currentPeersHosts = new ArrayList<>();
        EnumSet<APIEnum> disabledAPIs = EnumSet.noneOf(APIEnum.class);
        currentPeersHosts.add(peer.getHost());
        mainPeerAnnouncedAddress = peer.getAnnouncedAddress();
        if (!peer.getDisabledAPIs().contains(requestAPI)) {
            resultPeer = peer;
        }
        while (!disabledAPIs.isEmpty() && !connectablePeers.isEmpty()) {
            // remove all peers that do not introduce new enabled APIs
            connectablePeers.removeIf(p -> p.getDisabledAPIs().containsAll(disabledAPIs));
            peer = getRandomAPIPeer(connectablePeers);
            if (peer != null) {
                currentPeersHosts.add(peer.getHost());
                if (!peer.getDisabledAPIs().contains(requestAPI)) {
                    resultPeer = peer;
                }
                disabledAPIs.retainAll(peer.getDisabledAPIs());
            }
        }
        peersHosts = Collections.unmodifiableList(currentPeersHosts);
        Logger.logInfoMessage("Selected API peer " + resultPeer + " peer hosts selected " + currentPeersHosts);
        return resultPeer;
    }

    Peer setForcedPeer(Peer peer) {
        if (peer != null) {
            forcedPeerHost = peer.getHost();
            mainPeerAnnouncedAddress = peer.getAnnouncedAddress();
            return peer;
        } else {
            forcedPeerHost = null;
            mainPeerAnnouncedAddress = null;
            return getServingPeer(null);
        }
    }

    String getMainPeerAnnouncedAddress() {
        // The first client request GetBlockchainState is handled by the server
        // Not by the proxy. In order to report a peer to the client we have
        // To select some initial peer.
        if (mainPeerAnnouncedAddress == null) {
            Peer peer = getServingPeer(null);
            if (peer != null) {
                mainPeerAnnouncedAddress = peer.getAnnouncedAddress();
            }
        }
        return mainPeerAnnouncedAddress;
    }

    static boolean isActivated() {
        return Constants.isLightClient || (enableAPIProxy && Nxt.getBlockchainProcessor().isDownloading());
    }

    boolean blacklistHost(String host) {
        if (blacklistedPeers.size() > 1000) {
            Logger.logInfoMessage("Too many blacklisted peers");
            return false;
        }
        blacklistedPeers.put(host, Nxt.getEpochTime() + blacklistingPeriod);
        if (peersHosts.contains(host)) {
            peersHosts = Collections.emptyList();
            getServingPeer(null);
        }
        return true;
    }

    private Peer getRandomAPIPeer(List<Peer> peers) {
        if (peers.isEmpty()) {
            return null;
        }
        int index = ThreadLocalRandom.current().nextInt(peers.size());
        return peers.remove(index);
    }
}
