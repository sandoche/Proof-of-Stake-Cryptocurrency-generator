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
import nxt.Shuffler;
import nxt.crypto.Crypto;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;


public final class StopShuffler extends APIServlet.APIRequestHandler {

    static final StopShuffler instance = new StopShuffler();

    private StopShuffler() {
        super(new APITag[] {APITag.SHUFFLING}, "account", "shufflingFullHash", "secretPhrase", "adminPassword");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest req) throws ParameterException {
        String secretPhrase = ParameterParser.getSecretPhrase(req, false);
        byte[] shufflingFullHash = ParameterParser.getBytes(req, "shufflingFullHash", false);
        long accountId = ParameterParser.getAccountId(req, false);
        JSONObject response = new JSONObject();
        if (secretPhrase != null) {
            if (accountId != 0 && Account.getId(Crypto.getPublicKey(secretPhrase)) != accountId) {
                return JSONResponses.INCORRECT_ACCOUNT;
            }
            accountId = Account.getId(Crypto.getPublicKey(secretPhrase));
            if (shufflingFullHash.length == 0) {
                return JSONResponses.missing("shufflingFullHash");
            }
            Shuffler shuffler = Shuffler.stopShuffler(accountId, shufflingFullHash);
            response.put("stoppedShuffler", shuffler != null);
        } else {
            API.verifyPassword(req);
            if (accountId != 0 && shufflingFullHash.length != 0) {
                Shuffler shuffler = Shuffler.stopShuffler(accountId, shufflingFullHash);
                response.put("stoppedShuffler", shuffler != null);
            } else if (accountId == 0 && shufflingFullHash.length == 0) {
                Shuffler.stopAllShufflers();
                response.put("stoppedAllShufflers", true);
            } else if (accountId != 0) {
                return JSONResponses.missing("shufflingFullHash");
            } else if (shufflingFullHash.length != 0) {
                return JSONResponses.missing("account");
            }
        }
        return response;
    }

    @Override
    protected boolean requirePost() {
        return true;
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
