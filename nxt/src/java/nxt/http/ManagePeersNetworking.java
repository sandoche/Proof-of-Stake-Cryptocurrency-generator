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

package nxt.http;

import nxt.peer.Peers;
import nxt.util.Convert;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public class ManagePeersNetworking extends APIServlet.APIRequestHandler {
    static final ManagePeersNetworking instance = new ManagePeersNetworking();

    private ManagePeersNetworking() {
        super(new APITag[] {APITag.NETWORK}, "operation");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest request) {
        String operation = Convert.emptyToNull(request.getParameter("operation"));
        if (operation == null) {
            return JSONResponses.missing("operation");
        }
        switch (operation) {
            case "enable":
                Peers.enableNetworking();
                break;
            case "disable":
                Peers.disableNetworking();
                break;
            case "query":
                break;
            default:
                return JSONResponses.incorrect("operation", "Possible operations: 'enable', 'disable', 'query'");
        }
        JSONObject response = new JSONObject();
        response.put("isEnabled", Peers.isNetworkingEnabled());
        return response;
    }

    @Override
    protected final boolean requirePost() {
        return true;
    }

    @Override
    protected boolean requirePassword() {
        return true;
    }

    @Override
    protected boolean allowRequiredBlockParameters() {
        return false;
    }

    @Override
    protected boolean requireBlockchain() {
        return false;
    }
}
