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

import nxt.Currency;
import nxt.db.DbIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetCurrenciesByIssuer extends APIServlet.APIRequestHandler {

    static final GetCurrenciesByIssuer instance = new GetCurrenciesByIssuer();

    private GetCurrenciesByIssuer() {
        super(new APITag[] {APITag.MS, APITag.ACCOUNTS}, "account", "account", "account", "firstIndex", "lastIndex", "includeCounts");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {
        long[] accountIds = ParameterParser.getAccountIds(req, true);
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        boolean includeCounts = "true".equalsIgnoreCase(req.getParameter("includeCounts"));

        JSONObject response = new JSONObject();
        JSONArray accountsJSONArray = new JSONArray();
        response.put("currencies", accountsJSONArray);
        for (long accountId : accountIds) {
            JSONArray currenciesJSONArray = new JSONArray();
            try (DbIterator<Currency> currencies = Currency.getCurrencyIssuedBy(accountId, firstIndex, lastIndex)) {
                for (Currency currency : currencies) {
                    currenciesJSONArray.add(JSONData.currency(currency, includeCounts));
                }
            }
            accountsJSONArray.add(currenciesJSONArray);
        }
        return response;
    }

}
