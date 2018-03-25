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

import nxt.Account;
import nxt.Attachment;
import nxt.Attachment.MessagingPollCreation.PollBuilder;
import nxt.Constants;
import nxt.Nxt;
import nxt.NxtException;
import nxt.util.Convert;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static nxt.http.JSONResponses.INCORRECT_POLL_DESCRIPTION_LENGTH;
import static nxt.http.JSONResponses.INCORRECT_POLL_NAME_LENGTH;
import static nxt.http.JSONResponses.INCORRECT_POLL_OPTION_LENGTH;
import static nxt.http.JSONResponses.INCORRECT_ZEROOPTIONS;
import static nxt.http.JSONResponses.MISSING_DESCRIPTION;
import static nxt.http.JSONResponses.MISSING_NAME;

public final class CreatePoll extends CreateTransaction {

    static final CreatePoll instance = new CreatePoll();

    private CreatePoll() {
        super(new APITag[]{APITag.VS, APITag.CREATE_TRANSACTION},
                "name", "description", "finishHeight", "votingModel",
                "minNumberOfOptions", "maxNumberOfOptions",
                "minRangeValue", "maxRangeValue",
                "minBalance", "minBalanceModel", "holding",
                "option00", "option01", "option02");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {

        String nameValue = Convert.emptyToNull(req.getParameter("name"));
        String descriptionValue = req.getParameter("description");

        if (nameValue == null || nameValue.trim().isEmpty()) {
            return MISSING_NAME;
        } else if (descriptionValue == null) {
            return MISSING_DESCRIPTION;
        }

        if (nameValue.length() > Constants.MAX_POLL_NAME_LENGTH) {
            return INCORRECT_POLL_NAME_LENGTH;
        }

        if (descriptionValue.length() > Constants.MAX_POLL_DESCRIPTION_LENGTH) {
            return INCORRECT_POLL_DESCRIPTION_LENGTH;
        }

        List<String> options = new ArrayList<>();
        while (options.size() < Constants.MAX_POLL_OPTION_COUNT) {
            int i = options.size();
            String optionValue = Convert.emptyToNull(req.getParameter("option" + (i < 10 ? "0" + i : i)));
            if (optionValue == null) {
                break;
            }
            if (optionValue.length() > Constants.MAX_POLL_OPTION_LENGTH || (optionValue = optionValue.trim()).isEmpty()) {
                return INCORRECT_POLL_OPTION_LENGTH;
            }
            options.add(optionValue);
        }

        byte optionsSize = (byte) options.size();
        if (options.size() == 0) {
            return INCORRECT_ZEROOPTIONS;
        }

        int currentHeight = Nxt.getBlockchain().getHeight();
        int finishHeight = ParameterParser.getInt(req, "finishHeight",
                currentHeight + 2,
                currentHeight + Constants.MAX_POLL_DURATION + 1, true);

        byte votingModel = ParameterParser.getByte(req, "votingModel", (byte)0, (byte)3, true);

        byte minNumberOfOptions = ParameterParser.getByte(req, "minNumberOfOptions", (byte) 1, optionsSize, true);
        byte maxNumberOfOptions = ParameterParser.getByte(req, "maxNumberOfOptions", minNumberOfOptions, optionsSize, true);

        byte minRangeValue = ParameterParser.getByte(req, "minRangeValue", Constants.MIN_VOTE_VALUE, Constants.MAX_VOTE_VALUE, true);
        byte maxRangeValue = ParameterParser.getByte(req, "maxRangeValue", minRangeValue, Constants.MAX_VOTE_VALUE, true);

        PollBuilder builder = new PollBuilder(nameValue.trim(), descriptionValue.trim(),
                options.toArray(new String[options.size()]), finishHeight, votingModel,
                minNumberOfOptions, maxNumberOfOptions, minRangeValue, maxRangeValue);

        long minBalance = ParameterParser.getLong(req, "minBalance", 0, Long.MAX_VALUE, false);

        if (minBalance != 0) {
            byte minBalanceModel = ParameterParser.getByte(req, "minBalanceModel", (byte)0, (byte)3, true);
            builder.minBalance(minBalanceModel, minBalance);
        }

        long holdingId = ParameterParser.getUnsignedLong(req, "holding", false);
        if (holdingId != 0) {
            builder.holdingId(holdingId);
        }

        Account account = ParameterParser.getSenderAccount(req);
        Attachment attachment = builder.build();
        return createTransaction(req, account, attachment);
    }
}
