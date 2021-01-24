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
import nxt.Generator;
import nxt.Nxt;
import nxt.crypto.Crypto;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;

import static nxt.http.JSONResponses.NOT_FORGING;
import static nxt.http.JSONResponses.UNKNOWN_ACCOUNT;


public final class GetForging extends APIServlet.APIRequestHandler {

    static final GetForging instance = new GetForging();

    private GetForging() {
        super(new APITag[] {APITag.FORGING}, "secretPhrase", "adminPassword");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {

        String secretPhrase = ParameterParser.getSecretPhrase(req, false);
        int elapsedTime = Nxt.getEpochTime() - Nxt.getBlockchain().getLastBlock().getTimestamp();
        if (secretPhrase != null) {
            Account account = Account.getAccount(Crypto.getPublicKey(secretPhrase));
            if (account == null) {
                return UNKNOWN_ACCOUNT;
            }
            Generator generator = Generator.getGenerator(secretPhrase);
            if (generator == null) {
                return NOT_FORGING;
            }
            return JSONData.generator(generator, elapsedTime);
        } else {
            API.verifyPassword(req);
            JSONObject response = new JSONObject();
            JSONArray generators = new JSONArray();
            Generator.getSortedForgers().forEach(generator -> generators.add(JSONData.generator(generator, elapsedTime)));
            response.put("generators", generators);
            return response;
        }
    }

    @Override
    protected boolean allowRequiredBlockParameters() {
        return false;
    }

    @Override
    protected boolean requireFullClient() {
        return true;
    }

}
