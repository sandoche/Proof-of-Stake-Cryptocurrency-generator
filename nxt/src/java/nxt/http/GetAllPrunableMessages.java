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

import nxt.NxtException;
import nxt.PrunableMessage;
import nxt.db.DbIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAllPrunableMessages extends APIServlet.APIRequestHandler {

    static final GetAllPrunableMessages instance = new GetAllPrunableMessages();

    private GetAllPrunableMessages() {
        super(new APITag[] {APITag.MESSAGES}, "firstIndex", "lastIndex", "timestamp");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        final int timestamp = ParameterParser.getTimestamp(req);

        JSONObject response = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        response.put("prunableMessages", jsonArray);

        try (DbIterator<PrunableMessage> messages = PrunableMessage.getAll(firstIndex, lastIndex)) {
            while (messages.hasNext()) {
                PrunableMessage prunableMessage = messages.next();
                if (prunableMessage.getBlockTimestamp() < timestamp) {
                    break;
                }
                jsonArray.add(JSONData.prunableMessage(prunableMessage, null, null));
            }
        }
        return response;
    }

}
