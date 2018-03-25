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

import nxt.Nxt;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>RetrievePrunedData will schedule a background task to retrieve data which
 * has been pruned.  The nxt.maxPrunableLifetime property determines the
 * data that will be retrieved.  Data is retrieved from a random peer with
 * the PRUNABLE service.
 * </p>
 */
public class RetrievePrunedData extends APIServlet.APIRequestHandler {

    static final RetrievePrunedData instance = new RetrievePrunedData();

    private RetrievePrunedData() {
        super(new APITag[] {APITag.DEBUG});
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) {
        JSONObject response = new JSONObject();
        try {
            int count = Nxt.getBlockchainProcessor().restorePrunedData();
            response.put("done", true);
            response.put("numberOfPrunedData", count);
        } catch (RuntimeException e) {
            JSONData.putException(response, e);
        }
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
    protected final boolean allowRequiredBlockParameters() {
        return false;
    }

}
