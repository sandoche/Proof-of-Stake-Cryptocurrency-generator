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

import nxt.DigitalGoodsStore;
import nxt.db.DbIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetDGSTags extends APIServlet.APIRequestHandler {

    static final GetDGSTags instance = new GetDGSTags();

    private GetDGSTags() {
        super(new APITag[] {APITag.DGS}, "inStockOnly", "firstIndex", "lastIndex");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) {
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        final boolean inStockOnly = !"false".equalsIgnoreCase(req.getParameter("inStockOnly"));

        JSONObject response = new JSONObject();
        JSONArray tagsJSON = new JSONArray();
        response.put("tags", tagsJSON);

        try (DbIterator<DigitalGoodsStore.Tag> tags = inStockOnly
                ? DigitalGoodsStore.Tag.getInStockTags(firstIndex, lastIndex) : DigitalGoodsStore.Tag.getAllTags(firstIndex, lastIndex)) {
            while (tags.hasNext()) {
                tagsJSON.add(JSONData.tag(tags.next()));
            }
        }
        return response;
    }

}
