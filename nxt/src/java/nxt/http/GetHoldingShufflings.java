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

import nxt.Shuffling;
import nxt.db.DbIterator;
import nxt.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static nxt.http.JSONResponses.incorrect;

public final class GetHoldingShufflings extends APIServlet.APIRequestHandler {

    static final GetHoldingShufflings instance = new GetHoldingShufflings();

    private GetHoldingShufflings() {
        super(new APITag[] {APITag.SHUFFLING}, "holding", "stage", "includeFinished", "firstIndex", "lastIndex");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) {

        long holdingId = 0;
        String holdingValue = Convert.emptyToNull(req.getParameter("holding"));
        if (holdingValue != null) {
            try {
                holdingId = Convert.parseUnsignedLong(holdingValue);
            } catch (RuntimeException e) {
                return incorrect("holding");
            }
        }
        String stageValue = Convert.emptyToNull(req.getParameter("stage"));
        Shuffling.Stage stage = null;
        if (stageValue != null) {
            try {
                stage = Shuffling.Stage.get(Byte.parseByte(stageValue));
            } catch (RuntimeException e) {
                return incorrect("stage");
            }
        }
        boolean includeFinished = "true".equalsIgnoreCase(req.getParameter("includeFinished"));
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);

        JSONObject response = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        response.put("shufflings", jsonArray);
        try (DbIterator<Shuffling> shufflings = Shuffling.getHoldingShufflings(holdingId, stage, includeFinished, firstIndex, lastIndex)) {
            for (Shuffling shuffling : shufflings) {
                jsonArray.add(JSONData.shuffling(shuffling, false));
            }
        }
        return response;
    }

}
