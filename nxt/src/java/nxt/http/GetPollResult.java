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


import nxt.NxtException;
import nxt.Poll;
import nxt.VoteWeighting;
import nxt.util.Convert;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static nxt.http.JSONResponses.POLL_RESULTS_NOT_AVAILABLE;

public class GetPollResult extends APIServlet.APIRequestHandler {

    static final GetPollResult instance = new GetPollResult();

    private GetPollResult() {
        super(new APITag[]{APITag.VS}, "poll", "votingModel", "holding", "minBalance", "minBalanceModel");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        Poll poll = ParameterParser.getPoll(req);
        List<Poll.OptionResult> pollResults;
        VoteWeighting voteWeighting;
        if (Convert.emptyToNull(req.getParameter("votingModel")) == null) {
            pollResults = poll.getResults();
            voteWeighting = poll.getVoteWeighting();
        } else {
            byte votingModel = ParameterParser.getByte(req, "votingModel", (byte)0, (byte)3, true);
            long holdingId = ParameterParser.getLong(req, "holding", Long.MIN_VALUE, Long.MAX_VALUE, false);
            long minBalance = ParameterParser.getLong(req, "minBalance", 0, Long.MAX_VALUE, false);
            byte minBalanceModel = ParameterParser.getByte(req, "minBalanceModel", (byte)0, (byte)3, false);
            voteWeighting = new VoteWeighting(votingModel, holdingId, minBalance, minBalanceModel);
            voteWeighting.validate();
            pollResults = poll.getResults(voteWeighting);
        }
        if (pollResults == null) {
            return POLL_RESULTS_NOT_AVAILABLE;
        }
        return JSONData.pollResults(poll, pollResults, voteWeighting);
    }
}
