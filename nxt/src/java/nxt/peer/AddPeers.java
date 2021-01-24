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

package nxt.peer;

import nxt.util.JSON;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

final class AddPeers extends PeerServlet.PeerRequestHandler {

    static final AddPeers instance = new AddPeers();

    private AddPeers() {}

    @Override
    JSONStreamAware processRequest(JSONObject request, Peer peer) {
        final JSONArray peers = (JSONArray)request.get("peers");
        if (peers != null && Peers.getMorePeers && !Peers.hasTooManyKnownPeers()) {
            final JSONArray services = (JSONArray)request.get("services");
            final boolean setServices = (services != null && services.size() == peers.size());
            Peers.peersService.submit(() -> {
                for (int i=0; i<peers.size(); i++) {
                    String announcedAddress = (String)peers.get(i);
                    PeerImpl newPeer = Peers.findOrCreatePeer(announcedAddress, true);
                    if (newPeer != null) {
                        if (Peers.addPeer(newPeer) && setServices) {
                            newPeer.setServices(Long.parseUnsignedLong((String)services.get(i)));
                        }
                        if (Peers.hasTooManyKnownPeers()) {
                            break;
                        }
                    }
                }
            });
        }
        return JSON.emptyJSON;
    }

    @Override
    boolean rejectWhileDownloading() {
        return false;
    }

}
