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

import nxt.AssetTransfer;
import nxt.NxtException;
import nxt.db.DbIterator;
import nxt.db.DbUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAssetTransfers extends APIServlet.APIRequestHandler {

    static final GetAssetTransfers instance = new GetAssetTransfers();

    private GetAssetTransfers() {
        super(new APITag[] {APITag.AE}, "asset", "account", "firstIndex", "lastIndex", "timestamp", "includeAssetInfo");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {

        long assetId = ParameterParser.getUnsignedLong(req, "asset", false);
        long accountId = ParameterParser.getAccountId(req, false);
        if (assetId == 0 && accountId == 0) {
            return JSONResponses.MISSING_ASSET_ACCOUNT;
        }
        int timestamp = ParameterParser.getTimestamp(req);
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        boolean includeAssetInfo = "true".equalsIgnoreCase(req.getParameter("includeAssetInfo"));

        JSONObject response = new JSONObject();
        JSONArray transfersData = new JSONArray();
        DbIterator<AssetTransfer> transfers = null;
        try {
            if (accountId == 0) {
                transfers = AssetTransfer.getAssetTransfers(assetId, firstIndex, lastIndex);
            } else if (assetId == 0) {
                transfers = AssetTransfer.getAccountAssetTransfers(accountId, firstIndex, lastIndex);
            } else {
                transfers = AssetTransfer.getAccountAssetTransfers(accountId, assetId, firstIndex, lastIndex);
            }
            while (transfers.hasNext()) {
                AssetTransfer assetTransfer = transfers.next();
                if (assetTransfer.getTimestamp() < timestamp) {
                    break;
                }
                transfersData.add(JSONData.assetTransfer(assetTransfer, includeAssetInfo));
            }
        } finally {
            DbUtils.close(transfers);
        }
        response.put("transfers", transfersData);

        return response;
    }

    @Override
    protected boolean startDbTransaction() {
        return true;
    }
}
