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
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public class GetPhasingPoll extends APIServlet.APIRequestHandler {

    static final GetPhasingPoll instance = new GetPhasingPoll();

    private GetPhasingPoll() {
        super(new APITag[]{APITag.PHASING}, "transaction", "countVotes");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        long transactionId = ParameterParser.getUnsignedLong(req, "transaction", true);
        boolean countVotes = "true".equalsIgnoreCase(req.getParameter("countVotes"));
        PhasingPoll phasingPoll = PhasingPoll.getPoll(transactionId);
        if (phasingPoll != null) {
            return JSONData.phasingPoll(phasingPoll, countVotes);
        }
        PhasingPoll.PhasingPollResult pollResult = PhasingPoll.getResult(transactionId);
        if (pollResult != null) {
            return JSONData.phasingPollResult(pollResult);
        }
        return JSONResponses.UNKNOWN_TRANSACTION;
    }
}