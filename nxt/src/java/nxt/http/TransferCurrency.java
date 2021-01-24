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
import nxt.Currency;
import nxt.NxtException;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static nxt.http.JSONResponses.NOT_ENOUGH_CURRENCY;

public final class TransferCurrency extends CreateTransaction {

    static final TransferCurrency instance = new TransferCurrency();

    private TransferCurrency() {
        super(new APITag[] {APITag.MS, APITag.CREATE_TRANSACTION}, "recipient", "currency", "units");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {

        long recipient = ParameterParser.getAccountId(req, "recipient", true);

        Currency currency = ParameterParser.getCurrency(req);
        long units = ParameterParser.getLong(req, "units", 0, Long.MAX_VALUE, true);
        Account account = ParameterParser.getSenderAccount(req);

        Attachment attachment = new Attachment.MonetarySystemCurrencyTransfer(currency.getId(), units);
        try {
            return createTransaction(req, account, recipient, 0, attachment);
        } catch (NxtException.InsufficientBalanceException e) {
            return NOT_ENOUGH_CURRENCY;
        }
    }

}
