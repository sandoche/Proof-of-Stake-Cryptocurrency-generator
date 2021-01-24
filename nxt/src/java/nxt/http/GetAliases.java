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

import nxt.Alias;
import nxt.NxtException;
import nxt.db.FilteringIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAliases extends APIServlet.APIRequestHandler {

    static final GetAliases instance = new GetAliases();

    private GetAliases() {
        super(new APITag[] {APITag.ALIASES}, "timestamp", "account", "firstIndex", "lastIndex");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        final int timestamp = ParameterParser.getTimestamp(req);
        final long accountId = ParameterParser.getAccountId(req, true);
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);

        JSONArray aliases = new JSONArray();
        try (FilteringIterator<Alias> aliasIterator = new FilteringIterator<>(Alias.getAliasesByOwner(accountId, 0, -1),
                alias -> alias.getTimestamp() >= timestamp, firstIndex, lastIndex)) {
            while(aliasIterator.hasNext()) {
                aliases.add(JSONData.alias(aliasIterator.next()));
            }
        }

        JSONObject response = new JSONObject();
        response.put("aliases", aliases);
        return response;
    }

}
