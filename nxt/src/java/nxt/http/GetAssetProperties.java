/*
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

import nxt.Asset;
import nxt.NxtException;
import nxt.db.DbIterator;
import nxt.util.Convert;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public class GetAssetProperties extends APIServlet.APIRequestHandler {

    static final GetAssetProperties instance = new GetAssetProperties();

    private GetAssetProperties() {
        super(new APITag[] {APITag.AE}, "asset", "setter", "property", "firstIndex", "lastIndex");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        long assetId = ParameterParser.getUnsignedLong(req, "asset", false);
        long setterId = ParameterParser.getAccountId(req, "setter", false);
        if (assetId == 0 && setterId == 0) {
            return JSONResponses.missing("asset", "setter");
        }

        String property = Convert.emptyToNull(req.getParameter("property"));
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);

        JSONObject response = new JSONObject();
        if (assetId != 0) {
            response.put("asset", Long.toUnsignedString(assetId));
        }
        if (setterId != 0) {
            JSONData.putAccount(response, "setter", setterId);
        }
        JSONArray propertiesJSON = new JSONArray();
        response.put("properties", propertiesJSON);
        try (DbIterator<Asset.AssetProperty> iterator = Asset.getProperties(assetId, setterId, property, firstIndex, lastIndex)) {
            while (iterator.hasNext()) {
                propertiesJSON.add(JSONData.assetProperty(iterator.next(), assetId == 0, setterId == 0));
            }
        }
        return response;
    }
}
