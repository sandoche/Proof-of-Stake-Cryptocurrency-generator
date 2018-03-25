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

package nxt.user;

import nxt.util.JSON;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

public final class JSONResponses {

    public static final JSONStreamAware INVALID_SECRET_PHRASE;
    static {
        JSONObject response = new JSONObject();
        response.put("response", "showMessage");
        response.put("message", "Invalid secret phrase!");
        INVALID_SECRET_PHRASE = JSON.prepare(response);
    }

    public static final JSONStreamAware LOCK_ACCOUNT;
    static {
        JSONObject response = new JSONObject();
        response.put("response", "lockAccount");
        LOCK_ACCOUNT = JSON.prepare(response);
    }

    public static final JSONStreamAware LOCAL_USERS_ONLY;
    static {
        JSONObject response = new JSONObject();
        response.put("response", "showMessage");
        response.put("message", "This operation is allowed to local host users only!");
        LOCAL_USERS_ONLY = JSON.prepare(response);
    }

    public static final JSONStreamAware NOTIFY_OF_ACCEPTED_TRANSACTION;
    static {
        JSONObject response = new JSONObject();
        response.put("response", "notifyOfAcceptedTransaction");
        NOTIFY_OF_ACCEPTED_TRANSACTION = JSON.prepare(response);
    }

    public static final JSONStreamAware DENY_ACCESS;
    static {
        JSONObject response = new JSONObject();
        response.put("response", "denyAccess");
        DENY_ACCESS = JSON.prepare(response);
    }

    public static final JSONStreamAware INCORRECT_REQUEST;
    static {
        JSONObject response = new JSONObject();
        response.put("response", "showMessage");
        response.put("message", "Incorrect request!");
        INCORRECT_REQUEST = JSON.prepare(response);
    }

    public static final JSONStreamAware POST_REQUIRED;
    static {
        JSONObject response = new JSONObject();
        response.put("response", "showMessage");
        response.put("message", "This request is only accepted using POST!");
        POST_REQUIRED = JSON.prepare(response);
    }

    private JSONResponses() {} // never

}
