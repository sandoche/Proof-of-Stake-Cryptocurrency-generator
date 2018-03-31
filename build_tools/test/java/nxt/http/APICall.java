/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2017 Jelurida IP B.V.
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

import nxt.util.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Assert;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class APICall {

    private Map<String, List<String>> params;

    private APICall(Builder builder) {
        this.params = builder.params;
    }

    public static class Builder {

        protected Map<String, List<String>> params = new HashMap<>();
        
        public Builder(String requestType) {
            params.put("requestType", Collections.singletonList(requestType));
            params.put("deadline", Collections.singletonList("1440"));
        }

        public Builder param(String key, String value) {
            params.put(key, Collections.singletonList(value));
            return this;
        }

        public Builder param(String key, String[] values) {
            params.put(key, Arrays.asList(values));
            return this;
        }
        
        public Builder param(String key, byte value) {
            return param(key, "" + value);
        }

        public Builder param(String key, int value) {
            return param(key, "" + value);
        }

        public Builder param(String key, long value) {
            return param(key, "" + value);
        }

        public Builder secretPhrase(String value) {
            return param("secretPhrase", value);
        }

        public Builder feeNQT(long value) {
            return param("feeNQT", "" + value);
        }

        public Builder recipient(long id) {
            return param("recipient", Long.toUnsignedString(id));
        }

        public String getParam(String key) {
            return params.get(key).get(0);
        }

        public APICall build() {
            return new APICall(this);
        }
    }

    private String firstOrNull(List<String> list) {
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }
    
    private String[] toArrayOrNull(List<String> list) {
        if (list != null) {
            return list.toArray(new String[list.size()]);
        }
        return null;
    }
    
    public JSONObject invoke() {
        Logger.logDebugMessage("%s: request %s", params.get("requestType"), params);
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        when(req.getRemoteHost()).thenReturn("localhost");
        when(req.getMethod()).thenReturn("POST");
        for (String key : params.keySet()) {
            when(req.getParameter(key)).thenReturn(firstOrNull(params.get(key)));
            when(req.getParameterValues(key)).thenReturn(toArrayOrNull(params.get(key)));
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out));
        try {
            when(resp.getWriter()).thenReturn(writer);
            APIServlet apiServlet = new APIServlet();
            apiServlet.doPost(req, resp);
        } catch (ServletException | IOException e) {
            Assert.fail();
        }
        JSONObject response = (JSONObject) JSONValue.parse(new InputStreamReader(new ByteArrayInputStream(out.toByteArray())));
        Logger.logDebugMessage("%s: response %s", params.get("requestType"), response);
        return response;
    }

}
