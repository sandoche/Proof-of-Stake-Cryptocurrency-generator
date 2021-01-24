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

import nxt.Account;
import nxt.NxtException;
import nxt.db.DbIterator;
import nxt.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAccountProperties extends APIServlet.APIRequestHandler {

    static final GetAccountProperties instance = new GetAccountProperties();

    private GetAccountProperties() {
        super(new APITag[] {APITag.ACCOUNTS}, "recipient", "property", "setter", "firstIndex", "lastIndex");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {

        long recipientId = ParameterParser.getAccountId(req, "recipient", false);
        long setterId = ParameterParser.getAccountId(req, "setter", false);
        if (recipientId == 0 && setterId == 0) {
            return JSONResponses.missing("recipient", "setter");
        }
        String property = Convert.emptyToNull(req.getParameter("property"));
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);

        JSONObject response = new JSONObject();
        JSONArray propertiesJSON = new JSONArray();
        response.put("properties", propertiesJSON);
        if (recipientId != 0) {
            JSONData.putAccount(response, "recipient", recipientId);
        }
        if (setterId != 0) {
            JSONData.putAccount(response, "setter", setterId);
        }
        try (DbIterator<Account.AccountProperty> iterator = Account.getProperties(recipientId, setterId, property, firstIndex, lastIndex)) {
            while (iterator.hasNext()) {
                propertiesJSON.add(JSONData.accountProperty(iterator.next(), recipientId == 0, setterId == 0));
            }
        }
        return response;

    }

}
