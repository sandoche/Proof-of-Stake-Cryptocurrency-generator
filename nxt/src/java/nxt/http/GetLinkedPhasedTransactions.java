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

import nxt.NxtException;
import nxt.PhasingPoll;
import nxt.Transaction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class GetLinkedPhasedTransactions extends APIServlet.APIRequestHandler {
    static final GetLinkedPhasedTransactions instance = new GetLinkedPhasedTransactions();

    private GetLinkedPhasedTransactions() {
        super(new APITag[]{APITag.PHASING}, "linkedFullHash");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        byte[] linkedFullHash = ParameterParser.getBytes(req, "linkedFullHash", true);

        JSONArray json = new JSONArray();
        List<? extends Transaction> transactions = PhasingPoll.getLinkedPhasedTransactions(linkedFullHash);
        transactions.forEach(transaction -> json.add(JSONData.transaction(transaction)));
        JSONObject response = new JSONObject();
        response.put("transactions", json);

        return response;
    }
}