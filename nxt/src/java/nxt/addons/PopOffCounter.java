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

package nxt.addons;

import nxt.BlockchainProcessor;
import nxt.Nxt;
import nxt.http.APIServlet;
import nxt.http.APITag;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

public final class PopOffCounter implements AddOn {

    private volatile int numberOfPopOffs = 0;

    @Override
    public void init() {
        Nxt.getBlockchainProcessor().addListener(block -> numberOfPopOffs += 1, BlockchainProcessor.Event.BLOCK_POPPED);
    }

    @Override
    public APIServlet.APIRequestHandler getAPIRequestHandler() {
        return new APIServlet.APIRequestHandler(new APITag[]{APITag.ADDONS, APITag.BLOCKS}) {
            @Override
            protected JSONStreamAware processRequest(HttpServletRequest request) {
                JSONObject response = new JSONObject();
                response.put("numberOfPopOffs", numberOfPopOffs);
                return response;
            }
            @Override
            protected boolean allowRequiredBlockParameters() {
                return false;
            }
        };
    }

    @Override
    public String getAPIRequestType() {
        return "getNumberOfPopOffs";
    }

}
