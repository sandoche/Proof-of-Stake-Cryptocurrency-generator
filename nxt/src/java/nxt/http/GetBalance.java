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
import nxt.Nxt;
import nxt.NxtException;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class GetBalance extends APIServlet.APIRequestHandler {

    static final GetBalance instance = new GetBalance();

    private GetBalance() {
        super(new APITag[] {APITag.ACCOUNTS}, "account", "includeEffectiveBalance", "height");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        boolean includeEffectiveBalance = "true".equalsIgnoreCase(req.getParameter("includeEffectiveBalance"));
        long accountId = ParameterParser.getAccountId(req, true);
        int height = ParameterParser.getHeight(req);
        if (height < 0) {
            height = Nxt.getBlockchain().getHeight();
        }
        Account account = Account.getAccount(accountId, height);
        return JSONData.accountBalance(account, includeEffectiveBalance, height);
    }

}
