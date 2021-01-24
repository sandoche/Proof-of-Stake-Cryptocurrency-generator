/*
 * Copyright © 2013-2016 The Nxt Core Developers.
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

import nxt.util.Convert;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetEpochTime extends APIServlet.APIRequestHandler {

    static final GetEpochTime instance = new GetEpochTime();

    private GetEpochTime() {
        super(new APITag[] {APITag.UTILS}, "unixtime");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {
        long unixtime = ParameterParser.getLong(req, "unixtime", 0, Long.MAX_VALUE, System.currentTimeMillis()/1000);
        JSONObject response = new JSONObject();
        response.put("time", Convert.toEpochTime(unixtime * 1000));
        return response;
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
