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

import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static nxt.user.JSONResponses.LOCK_ACCOUNT;

public final class LockAccount extends UserServlet.UserRequestHandler {

    static final LockAccount instance = new LockAccount();

    private LockAccount() {}

    @Override
    JSONStreamAware processRequest(HttpServletRequest req, User user) throws IOException {

        user.lockAccount();

        return LOCK_ACCOUNT;
    }
}
