/*
 * Copyright © 2016-2020 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of this software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package com.jelurida.ardor.client.api;

import nxt.addons.JO;
import nxt.http.callers.GetPeersCall;

import java.net.URL;

/**
 * Read the list of connected API peers from a remote node
 */
public class GetPeersForMobileApp {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Specify remote node url, for example https://testnxt.jelurida.com/nxt");
            System.exit(0);
        }
        URL url = new URL(args[0]);
        getPeers(url);
    }

    private static void getPeers(URL url) {
        JO peers = GetPeersCall.create().active("true").state("CONNECTED").service("API", "CORS").includePeerInfo(true).
            remote(url).trustRemoteCertificate(true).call();
        System.out.println(peers.toJSONString());
    }
}
