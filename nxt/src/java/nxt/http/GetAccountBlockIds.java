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

import nxt.Block;
import nxt.Nxt;
import nxt.NxtException;
import nxt.db.DbIterator;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetAccountBlockIds extends APIServlet.APIRequestHandler {

    static final GetAccountBlockIds instance = new GetAccountBlockIds();

    private GetAccountBlockIds() {
        super(new APITag[] {APITag.ACCOUNTS}, "account", "timestamp", "firstIndex", "lastIndex");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {

        long accountId = ParameterParser.getAccountId(req, true);
        int timestamp = ParameterParser.getTimestamp(req);
        int firstIndex = ParameterParser.getFirstIndex(req);
        int lastIndex = ParameterParser.getLastIndex(req);

        JSONArray blockIds = new JSONArray();
        try (DbIterator<? extends Block> iterator = Nxt.getBlockchain().getBlocks(accountId, timestamp, firstIndex, lastIndex)) {
            while (iterator.hasNext()) {
                Block block = iterator.next();
                blockIds.add(block.getStringId());
            }
        }

        JSONObject response = new JSONObject();
        response.put("blockIds", blockIds);

        return response;
    }

}
