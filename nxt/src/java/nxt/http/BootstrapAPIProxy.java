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

import nxt.NxtException;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public class BootstrapAPIProxy extends APIServlet.APIRequestHandler {

    static final BootstrapAPIProxy instance = new BootstrapAPIProxy();

    private BootstrapAPIProxy() {
        super(new APITag[] {APITag.NETWORK});
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest request) throws NxtException {
        JSONObject response = new JSONObject();
        StringBuilder message = new StringBuilder();
        boolean result = APIProxy.getInstance().bootstrap(message);
        response.put("success", result);
        response.put("message", message.toString());
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
