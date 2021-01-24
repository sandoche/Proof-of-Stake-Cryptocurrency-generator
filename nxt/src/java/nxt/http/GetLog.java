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

import nxt.util.MemoryHandler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.logging.Handler;
import java.util.logging.Logger;

/**
 * <p>The GetLog API will return log messages from the ring buffer
 * maintained by the MemoryHandler log handler.  The most recent
 * 'count' messages will be returned.  All log messages in the
 * ring buffer will be returned if 'count' is omitted.</p>
 *
 * <p>Request parameters:</p>
 * <ul>
 * <li>count - The number of log messages to return</li>
 * </ul>
 *
 * <p>Response parameters:</p>
 * <ul>
 * <li>messages - An array of log messages</li>
 * </ul>
 */
public final class GetLog extends APIServlet.APIRequestHandler {

    /** GetLog instance */
    static final GetLog instance = new GetLog();

    /**
     * Create the GetLog instance
     */
    private GetLog() {
        super(new APITag[] {APITag.DEBUG}, "count");
    }

    /**
     * Process the GetLog API request
     *
     * @param   req                 API request
     * @return                      API response
     */
    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) {
        //
        // Get the number of log messages to return
        //
        int count;
        String value = req.getParameter("count");
        if (value != null)
            count = Math.max(Integer.valueOf(value), 0);
        else
            count = Integer.MAX_VALUE;
        //
        // Get the log messages
        //
        JSONArray logJSON = new JSONArray();
        Logger logger = Logger.getLogger("");
        Handler[] handlers = logger.getHandlers();
        for (Handler handler : handlers) {
            if (handler instanceof MemoryHandler) {
                logJSON.addAll(((MemoryHandler)handler).getMessages(count));
                break;
            }
        }
        //
        // Return the response
        //
        JSONObject response = new JSONObject();
        response.put("messages", logJSON);
        return response;
    }

    /**
     * Require the administrator password
     *
     * @return                      TRUE if the admin password is required
     */
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
