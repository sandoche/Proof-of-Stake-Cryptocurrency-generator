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

import nxt.util.Convert;
import nxt.util.JSON;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class HexConvert extends APIServlet.APIRequestHandler {

    static final HexConvert instance = new HexConvert();

    private HexConvert() {
        super(new APITag[] {APITag.UTILS}, "string");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) {
        String string = Convert.emptyToNull(req.getParameter("string"));
        if (string == null) {
            return JSON.emptyJSON;
        }
        JSONObject response = new JSONObject();
        try {
            byte[] asHex = Convert.parseHexString(string);
            if (asHex.length > 0) {
                response.put("text", Convert.toString(asHex));
            }
        } catch (RuntimeException ignore) {}
        try {
            byte[] asText = Convert.toBytes(string);
            response.put("binary", Convert.toHexString(asText));
        } catch (RuntimeException ignore) {}
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
