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

package nxt.addons;

import nxt.http.APIServlet;

import java.util.Map;

public interface AddOn {

    default void init() {}

    default void shutdown() {}

    default APIServlet.APIRequestHandler getAPIRequestHandler() {
        return null;
    }

    default String getAPIRequestType() {
        return null;
    }

    default Map<String, APIServlet.APIRequestHandler> getAPIRequests() { return null; }
}
