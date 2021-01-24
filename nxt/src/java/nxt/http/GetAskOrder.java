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
import nxt.Order;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static nxt.http.JSONResponses.UNKNOWN_ORDER;

public final class GetAskOrder extends APIServlet.APIRequestHandler {

    static final GetAskOrder instance = new GetAskOrder();

    private GetAskOrder() {
        super(new APITag[] {APITag.AE}, "order");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws NxtException {
        long orderId = ParameterParser.getUnsignedLong(req, "order", true);
        Order.Ask askOrder = Order.Ask.getAskOrder(orderId);
        if (askOrder == null) {
            return UNKNOWN_ORDER;
        }
        return JSONData.askOrder(askOrder);
    }

}
