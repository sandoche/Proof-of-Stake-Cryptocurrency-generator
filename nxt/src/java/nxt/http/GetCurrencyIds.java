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

public final class GetCurrencyIds extends APIServlet.APIRequestHandler {

    static final GetCurrencyIds instance = new GetCurrencyIds();

    private GetCurrencyIds() {
        super(new APITag[] {APITag.MS}, "firstIndex", "lastIndex");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) {

        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);

        JSONArray currencyIds = new JSONArray();
        try (DbIterator<Currency> currencies = Currency.getAllCurrencies(firstIndex, lastIndex)) {
            for (Currency currency : currencies) {
                currencyIds.add(Long.toUnsignedString(currency.getId()));
            }
        }
        JSONObject response = new JSONObject();
        response.put("currencyIds", currencyIds);
        return response;
    }

}
