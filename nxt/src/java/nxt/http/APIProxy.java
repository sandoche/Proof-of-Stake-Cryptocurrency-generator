/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2020 Jelurida IP B.V.
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
import nxt.Db;
import nxt.Nxt;
import nxt.peer.Peer;
import nxt.peer.Peers;
import nxt.util.Logger;
import nxt.util.ThreadPool;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class APIProxy {
    public static final Set<String> NOT_FORWARDED_REQUESTS;

    static final boolean enableAPIProxy = Constants.isLightClient ||
            (Nxt.getBooleanProperty("nxt.enableAPIProxy") && ! API.isOpenAPI);
    private static final int blacklistingPeriod = Nxt.getIntProperty("nxt.apiProxyBlacklistingPeriod");
    static final String forcedServerURL = Nxt.getStringProperty("nxt.forceAPIProxyServerURL", "");
    public static final int PEER_CONNECTIONS_RETRIES = 3;
    private final List<String> bootstrapNodes = Nxt.getStringListProperty(
            Constants.isTestnet ? "nxt.testnetProxyBootstrapNodes" : "nxt.proxyBootstrapNodes");

    private volatile String forcedPeerHost;
    private volatile List<String> peersHosts = Collections.emptyList();
    private volatile String mainPeerAnnouncedAddress;

    private final Map<String, Integer> blacklistedPeers = new ConcurrentHashMap<>();
    private static volatile APIProxy instance;

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
        boolean hasUnblacklisted = getInstance().blacklistedPeers.entrySet().removeIf((entry) -> {
            if (entry.getValue() < curTime) {
                Logger.logDebugMessage("Unblacklisting API peer " + entry.getKey());
                return true;
            }
            return false;
        });
        if (hasUnblacklisted) {
            deleteBlacklistedPeers(curTime);
        }
        List<String> currentPeersHosts = getInstance().peersHosts;
        if (currentPeersHosts != null && Peers.isNetworkingEnabled()) {
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

    private HttpClient bootstrapHttpClient;

    private APIProxy() {
        if (enableAPIProxy) {
            int curTime = Nxt.getEpochTime();
            loadBlacklistedPeers((host, unblacklistTime) -> {
                if (curTime < unblacklistTime) {
                    blacklistedPeers.put(host, unblacklistTime);
                }
            });
            ThreadPool.runBeforeStart(() -> {
                StringBuilder message = new StringBuilder();
                bootstrap(message);
                Logger.logDebugMessage(message.toString());
            }, true);
        }
    }

    public static void init() {
        instance = new APIProxy();
    }

    public static APIProxy getInstance() {
        return instance;
    }

    public boolean bootstrap(StringBuilder message) {
        if (Constants.isOffline) {
            message.append("Current instance is configured for offline work");
            return false;
        }

        if (forcedPeerHost != null) {
            message.append("Forced peer is set to ").append(forcedPeerHost);
            return true;
        }

        bootstrapHttpClient = HttpClientFactory.newHttpClient();
        try {
            bootstrapHttpClient.start();
        } catch (Exception e) {
            Logger.logErrorMessage("", e);
            message.append(e.toString());
            return false;
        }
        try {
            boolean hasConnectedPeers = true;
            List<Peer> connectablePeers = null;
            while (true) {
                Peer peer = null;
                if (hasConnectedPeers) {
                    peer = getServingPeer("getBlockchainStatus");
                }
                if (peer == null) {
                    hasConnectedPeers = false;
                    if (connectablePeers == null) {
                        connectablePeers = Peers.getPeers(p -> p.isOpenAPI() && !blacklistedPeers.containsKey(p.getHost()));
                    }
                    peer = tryToConnectPeer(connectablePeers);
                }
                if (peer == null) {
                    break;
                } else {
                    if (testPeerAPI(peer)) {
                        peersHosts = Collections.singletonList(peer.getHost());
                        message.append("Proxy bootstrap complete, known peer ").append(peer.getHost()).append(" is connected");
                        return true;
                    } else {
                        peer.blacklist("Advertises open API but doesn't respond to getBlockchainStatus");
                        blacklistHost(peer.getHost());
                    }
                }
            }
            return trustedBootstrap(message);
        } finally {
            try {
                bootstrapHttpClient.stop();
            } catch (Exception e) {
                Logger.logErrorMessage("", e);
            }
        }
    }

    private boolean trustedBootstrap(StringBuilder message) {
        Logger.logDebugMessage("Start trusted proxy bootstrap");
        Collections.shuffle(bootstrapNodes);
        for (String bootstrapNode : bootstrapNodes) {
            Peer bootstrapPeer = addAndConnectPeer(bootstrapNode);
            if (bootstrapPeer != null) {
                JSONObject response = executeRequest(bootstrapPeer, "getPeers",
                        "state=CONNECTED&service=API");
                if (response != null) {
                    JSONArray openApiPeers = (JSONArray) response.get("peers");
                    for (Object address : openApiPeers) {
                        Peer peer = addAndConnectPeer((String) address);
                        if (peer != null) {
                            if (testPeerAPI(peer)) {
                                peersHosts = Collections.singletonList(peer.getHost());
                                message.append("Could not connect known peers." + " Bootstrapped from ")
                                        .append(bootstrapNode).append(". Initial peer is ").append(peer.getHost());
                                return true;
                            }
                        }
                    }
                }
            }
        }
        message.append("Trusted proxy bootstrap failed");
        return false;
    }

    private Peer tryToConnectPeer(List<Peer> connectablePeers) {
        int retries = PEER_CONNECTIONS_RETRIES;
        while (!connectablePeers.isEmpty() && retries > 0) {
            Peer result = getRandomAPIPeer(connectablePeers);
            if (result != null) {
                Peers.connectPeer(result);
                if (result.getState() == Peer.State.CONNECTED) {
                    return result;
                }
            }
            retries--;
        }
        return null;
    }

    private Peer addAndConnectPeer(String address) {
        Peer bootstrapPeer = Peers.findOrCreatePeer(address, true);
        if (bootstrapPeer == null) {
            return null;
        }
        Peers.addPeer(bootstrapPeer);
        Peers.connectPeer(bootstrapPeer);
        if (bootstrapPeer.getState() == Peer.State.CONNECTED) {
            return bootstrapPeer;
        } else {
            return null;
        }
    }

    private boolean testPeerAPI(Peer peer) {
        JSONObject statusObj = executeRequest(peer, "getBlockchainStatus", null);
        if (statusObj == null) {
            blacklistHost(peer.getHost());
            return false;
        }
        Peer.BlockchainState blockchainState = Peer.BlockchainState.get((String) statusObj.get("blockchainState"));
        return blockchainState == Peer.BlockchainState.UP_TO_DATE || blockchainState == Peer.BlockchainState.FORK;
    }

    private JSONObject executeRequest(Peer peer, String requestType, String query) {

        bootstrapHttpClient.setAddressResolutionTimeout(500);
        bootstrapHttpClient.setConnectTimeout(1000);
        StringBuilder uri = peer.getPeerApiUri();
        try {
            uri.append("/nxt?requestType=").append(requestType);
            if (query != null) {
                uri.append("&").append(query);
            }
            Request request = bootstrapHttpClient.newRequest(uri.toString())
                    .timeout(2, TimeUnit.SECONDS);
            ContentResponse response = request.send();
            return (JSONObject) JSONValue.parse(response.getContentAsString());
        } catch (Exception e) {
            Logger.logDebugMessage("Proxy bootstrap request failed: " + uri.toString(), e);
            return null;
        }
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
        if (blacklistedPeers.size() > 2000) {
            Logger.logInfoMessage("Too many blacklisted peers");
            return false;
        }
        int unblacklistTime = Nxt.getEpochTime() + blacklistingPeriod;
        blacklistedPeers.put(host, unblacklistTime);
        storeBlacklistedPeer(host, unblacklistTime);
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

    private static void storeBlacklistedPeer(String host, int unblacklistTime) {
        try (Connection con = Db.db.getConnection();
             PreparedStatement pstmt = con.prepareStatement("MERGE INTO blacklisted_open_api_nodes "
                     + "(host, unblacklist_time) KEY(host) VALUES(?, ?)")) {
            pstmt.setString(1, host);
            pstmt.setInt(2, unblacklistTime);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    private static void deleteBlacklistedPeers(int currentTime) {
        try (Connection con = Db.db.getConnection();
             PreparedStatement pstmt = con.prepareStatement("DELETE FROM blacklisted_open_api_nodes "
                     + "WHERE unblacklist_time < ?")) {
            pstmt.setInt(1, currentTime);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    private static void loadBlacklistedPeers(BiConsumer<String, Integer> consumer) {
        try (Connection con = Db.db.getConnection();
             PreparedStatement pstmt = con.prepareStatement("SELECT * FROM blacklisted_open_api_nodes");
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                consumer.accept(rs.getString("host"), rs.getInt("unblacklist_time"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }
}
