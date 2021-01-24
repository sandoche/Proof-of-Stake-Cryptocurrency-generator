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

import nxt.Account;
import nxt.Attachment;
import nxt.Constants;
import nxt.NxtException;
import nxt.util.Convert;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static nxt.http.JSONResponses.INCORRECT_ACCOUNT_PROPERTY_NAME_LENGTH;
import static nxt.http.JSONResponses.INCORRECT_ACCOUNT_PROPERTY_VALUE_LENGTH;

public final class SetAccountProperty extends CreateTransaction {

    static final SetAccountProperty instance = new SetAccountProperty();

    private SetAccountProperty() {
        super(new APITag[] {APITag.ACCOUNTS, APITag.CREATE_TRANSACTION}, "recipient", "property", "value");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {

        Account senderAccount = ParameterParser.getSenderAccount(req);
        long recipientId = ParameterParser.getAccountId(req, "recipient", false);
        if (recipientId == 0) {
            recipientId = senderAccount.getId();
        }
        String property = Convert.nullToEmpty(req.getParameter("property")).trim();
        String value = Convert.nullToEmpty(req.getParameter("value")).trim();

        if (property.length() > Constants.MAX_ACCOUNT_PROPERTY_NAME_LENGTH || property.length() == 0) {
            return INCORRECT_ACCOUNT_PROPERTY_NAME_LENGTH;
        }

        if (value.length() > Constants.MAX_ACCOUNT_PROPERTY_VALUE_LENGTH) {
            return INCORRECT_ACCOUNT_PROPERTY_VALUE_LENGTH;
        }

        Attachment attachment = new Attachment.MessagingAccountProperty(property, value);
        return createTransaction(req, senderAccount, recipientId, 0, attachment);

    }

}
