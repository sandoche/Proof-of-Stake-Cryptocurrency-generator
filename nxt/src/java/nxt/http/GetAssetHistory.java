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

import nxt.AssetHistory;
import nxt.NxtException;
import nxt.db.DbIterator;
import nxt.db.DbUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAssetHistory extends APIServlet.APIRequestHandler {

    static final GetAssetHistory instance = new GetAssetHistory();

    enum Query {
        DELETE_ONLY {
            @Override
            DbIterator<AssetHistory> getAssetHistories(long assetId, long accountId, int firstIndex, int lastIndex) {
                if (accountId == 0) {
                    return AssetHistory.getAssetDeletes(assetId, firstIndex, lastIndex);
                } else if (assetId == 0) {
                    return AssetHistory.getAccountAssetDeletes(accountId, firstIndex, lastIndex);
                } else {
                    return AssetHistory.getAccountAssetDeletes(accountId, assetId, firstIndex, lastIndex);
                }
            }
        },
        INCREASE_ONLY {
            @Override
            DbIterator<AssetHistory> getAssetHistories(long assetId, long accountId, int firstIndex, int lastIndex) {
                if (accountId == 0) {
                    return AssetHistory.getAssetIncreases(assetId, firstIndex, lastIndex);
                } else if (assetId == 0) {
                    return AssetHistory.getAccountAssetIncreases(accountId, firstIndex, lastIndex);
                } else {
                    return AssetHistory.getAccountAssetIncreases(accountId, assetId, firstIndex, lastIndex);
                }
            }
        },
        ALL {
            @Override
            DbIterator<AssetHistory> getAssetHistories(long assetId, long accountId, int firstIndex, int lastIndex) {
                if (accountId == 0) {
                    return AssetHistory.getAssetHistory(assetId, firstIndex, lastIndex);
                } else if (assetId == 0) {
                    return AssetHistory.getAccountAssetHistory(accountId, firstIndex, lastIndex);
                } else {
                    return AssetHistory.getAccountAssetHistory(accountId, assetId, firstIndex, lastIndex);
                }
            }
        };

        static Query get(boolean deletesOnly, boolean increasesOnly) {
            if (deletesOnly) return DELETE_ONLY;
            if (increasesOnly) return INCREASE_ONLY;
            return ALL;
        }

        abstract DbIterator<AssetHistory> getAssetHistories(long assetId, long accountId, int firstIndex, int lastIndex);
    }

    private GetAssetHistory() {
        super(new APITag[] {APITag.AE}, "asset", "account", "firstIndex", "lastIndex", "timestamp", "includeAssetInfo", "deletesOnly", "increasesOnly");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {

        long assetId = ParameterParser.getUnsignedLong(req, "asset", false);
        long accountId = ParameterParser.getAccountId(req, false);
        if (assetId == 0 && accountId == 0) {
            return JSONResponses.MISSING_ASSET_ACCOUNT;
        }
        boolean deletesOnly = "true".equalsIgnoreCase(req.getParameter( "deletesOnly"));
        boolean increasesOnly = "true".equalsIgnoreCase(req.getParameter( "increasesOnly"));
        if (deletesOnly && increasesOnly) {
            return JSONResponses.either("deletesOnly", "increasesOnly");
        }
        Query query = Query.get(deletesOnly, increasesOnly);
        int timestamp = ParameterParser.getTimestamp(req);
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        boolean includeAssetInfo = "true".equalsIgnoreCase(req.getParameter( "includeAssetInfo"));

        JSONObject response = new JSONObject();
        JSONArray assetHistoryData = new JSONArray();
        DbIterator<AssetHistory> assetHistory = null;
        try {
            assetHistory = query.getAssetHistories(assetId, accountId, firstIndex, lastIndex);
            while (assetHistory.hasNext()) {
                AssetHistory history = assetHistory.next();
                if (history.getTimestamp() < timestamp) {
                    break;
                }
                assetHistoryData.add(JSONData.assetHistory(history, includeAssetInfo));
            }
        } finally {
            DbUtils.close(assetHistory);
        }
        response.put("assetHistory", assetHistoryData);

        return response;
    }

    @Override
    protected boolean startDbTransaction() {
        return true;
    }
}
