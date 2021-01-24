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

import nxt.PhasingPoll;
import nxt.Transaction;
import nxt.VoteWeighting;
import nxt.db.DbIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public class GetAssetPhasedTransactions extends APIServlet.APIRequestHandler {
    static final GetAssetPhasedTransactions instance = new GetAssetPhasedTransactions();

    private GetAssetPhasedTransactions() {
        super(new APITag[]{APITag.AE, APITag.PHASING}, "asset", "account", "withoutWhitelist", "firstIndex", "lastIndex");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {
        long assetId = ParameterParser.getUnsignedLong(req, "asset", true);
        long accountId = ParameterParser.getAccountId(req, false);
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);
        boolean withoutWhitelist = "true".equalsIgnoreCase(req.getParameter("withoutWhitelist"));

        JSONArray transactions = new JSONArray();
        try (DbIterator<? extends Transaction> iterator = PhasingPoll.getHoldingPhasedTransactions(assetId, VoteWeighting.VotingModel.ASSET,
                accountId, withoutWhitelist, firstIndex, lastIndex)) {
            while (iterator.hasNext()) {
                Transaction transaction = iterator.next();
                transactions.add(JSONData.transaction(transaction));
            }
        }
        JSONObject response = new JSONObject();
        response.put("transactions", transactions);
        return response;
    }

}
