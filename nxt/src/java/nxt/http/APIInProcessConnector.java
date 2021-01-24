/*
 * Copyright © 2016-2020 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of this software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package nxt.http;

import nxt.util.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class APIInProcessConnector implements APIConnector {

    private final Map<String, List<String>> params;
    private final Map<String, byte[]> parts;

    APIInProcessConnector(Map<String, List<String>> params, Map<String, byte[]> parts) {
        this.params = params;
        this.parts = parts;
    }

    public InputStream getInputStream() {
        logRequestParameters();
        HttpServletRequest req = new MockedRequest(params, parts);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        HttpServletResponse resp = new MockedResponse(out);
        try {
            APIServlet apiServlet = new APIServlet();
            apiServlet.doPost(req, resp);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void logRequestParameters() {
        if (!Logger.isInfoEnabled()) {
            Logger.logInfoMessage("%s", params.get("requestType"));
            return;
        }
        String paramsStr = params.entrySet().stream().map(e -> {
            if (API.SENSITIVE_PARAMS.contains(e.getKey())) {
                return e.getKey() + "={hidden}";
            }
            if (e.getValue().size() == 1) {
                return e.getKey() + "=" + e.getValue().get(0);
            }
            return e.getKey() + "=" + e.getValue().toString();
        }).collect(Collectors.joining("&"));
        Logger.logInfoMessage("%s: request %s", params.get("requestType"), paramsStr);
    }
}
