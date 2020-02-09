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

import nxt.Constants;
import nxt.Db;
import nxt.Nxt;
import nxt.NxtException;
import nxt.addons.AddOns;
import nxt.util.JSON;
import nxt.util.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static nxt.http.JSONResponses.ERROR_DISABLED;
import static nxt.http.JSONResponses.ERROR_INCORRECT_REQUEST;
import static nxt.http.JSONResponses.ERROR_NOT_ALLOWED;
import static nxt.http.JSONResponses.LIGHT_CLIENT_DISABLED_API;
import static nxt.http.JSONResponses.POST_REQUIRED;
import static nxt.http.JSONResponses.REQUIRED_BLOCK_NOT_FOUND;
import static nxt.http.JSONResponses.REQUIRED_LAST_BLOCK_NOT_FOUND;

public final class APIServlet extends HttpServlet {

    public abstract static class APIRequestHandler {

        private final List<String> parameters;
        private final String fileParameter;
        private final Set<APITag> apiTags;

        protected APIRequestHandler(APITag[] apiTags, String... parameters) {
            this(null, apiTags, parameters);
        }

        protected APIRequestHandler(String fileParameter, APITag[] apiTags, String... origParameters) {
            List<String> parameters = new ArrayList<>();
            Collections.addAll(parameters, origParameters);
            if ((requirePassword() || parameters.contains("lastIndex")) && ! API.disableAdminPassword) {
                parameters.add("adminPassword");
            }
            if (allowRequiredBlockParameters()) {
                parameters.add("requireBlock");
                parameters.add("requireLastBlock");
            }
            this.parameters = Collections.unmodifiableList(parameters);
            this.apiTags = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(apiTags)));
            this.fileParameter = fileParameter;
        }

        public final List<String> getParameters() {
            return parameters;
        }

        public final Set<APITag> getAPITags() {
            return apiTags;
        }

        public final String getFileParameter() {
            return fileParameter;
        }

        protected abstract JSONStreamAware processRequest(HttpServletRequest request) throws NxtException;

        protected JSONStreamAware processRequest(HttpServletRequest request, HttpServletResponse response) throws NxtException {
            return processRequest(request);
        }

        protected boolean requirePost() {
            return false;
        }

        protected boolean startDbTransaction() {
            return false;
        }

        protected boolean requirePassword() {
            return false;
        }

        protected boolean allowRequiredBlockParameters() {
            return true;
        }

        protected boolean requireBlockchain() {
            return true;
        }

        protected boolean requireFullClient() {
            return false;
        }

        protected boolean isTextArea(String parameter) {
            return false;
        }

        protected boolean isPassword(String parameter) {
            return false;
        }

    }

    private static final boolean enforcePost = Nxt.getBooleanProperty("nxt.apiServerEnforcePOST");
    private static final boolean fixResponseContentType = Nxt.getBooleanProperty("nxt.apiFixResponseContentType");
    static final Map<String,APIRequestHandler> apiRequestHandlers;
    static final Map<String,APIRequestHandler> disabledRequestHandlers;

    static {

        Map<String,APIRequestHandler> map = new HashMap<>();
        Map<String,APIRequestHandler> disabledMap = new HashMap<>();

        for (APIEnum api : APIEnum.values()) {
            if (!api.getName().isEmpty() && api.getHandler() != null) {
                map.put(api.getName(), api.getHandler());
            }
        }

        AddOns.registerAPIRequestHandlers(map);

        API.disabledAPIs.forEach(api -> {
            APIRequestHandler handler = map.remove(api);
            if (handler == null) {
                throw new RuntimeException("Invalid API in nxt.disabledAPIs: " + api);
            }
            disabledMap.put(api, handler);
        });
        API.disabledAPITags.forEach(apiTag -> {
            Iterator<Map.Entry<String, APIRequestHandler>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, APIRequestHandler> entry = iterator.next();
                if (entry.getValue().getAPITags().contains(apiTag)) {
                    disabledMap.put(entry.getKey(), entry.getValue());
                    iterator.remove();
                }
            }
        });
        if (!API.disabledAPIs.isEmpty()) {
            Logger.logInfoMessage("Disabled APIs: " + API.disabledAPIs);
        }
        if (!API.disabledAPITags.isEmpty()) {
            Logger.logInfoMessage("Disabled APITags: " + API.disabledAPITags);
        }

        apiRequestHandlers = Collections.unmodifiableMap(map);
        disabledRequestHandlers = disabledMap.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(disabledMap);
    }

    public static APIRequestHandler getAPIRequestHandler(String requestType) {
        return apiRequestHandlers.get(requestType);
    }

    static void initClass() {}

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        process(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        process(req, resp);
    }

    private void process(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // Set response values now in case we create an asynchronous context
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, private");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);
        if (fixResponseContentType) {
            resp.setContentType("application/json");
        } else {
            resp.setContentType("text/plain; charset=UTF-8");
        }

        JSONStreamAware response = JSON.emptyJSON;
        long startTime = System.currentTimeMillis();

        try {

            if (!API.isAllowed(req.getRemoteHost())) {
                response = ERROR_NOT_ALLOWED;
                return;
            }

            String requestType = req.getParameter("requestType");
            if (requestType == null) {
                response = ERROR_INCORRECT_REQUEST;
                return;
            }

            APIRequestHandler apiRequestHandler = apiRequestHandlers.get(requestType);
            if (apiRequestHandler == null) {
                if (disabledRequestHandlers.containsKey(requestType)) {
                    response = ERROR_DISABLED;
                } else {
                    response = ERROR_INCORRECT_REQUEST;
                }
                return;
            }

            if (Constants.isLightClient && apiRequestHandler.requireFullClient()) {
                response = LIGHT_CLIENT_DISABLED_API;
                return;
            }

            if (enforcePost && apiRequestHandler.requirePost() && !"POST".equals(req.getMethod())) {
                response = POST_REQUIRED;
                return;
            }

            if (apiRequestHandler.requirePassword()) {
                API.verifyPassword(req);
            }
            final long requireBlockId = apiRequestHandler.allowRequiredBlockParameters() ?
                    ParameterParser.getUnsignedLong(req, "requireBlock", false) : 0;
            final long requireLastBlockId = apiRequestHandler.allowRequiredBlockParameters() ?
                    ParameterParser.getUnsignedLong(req, "requireLastBlock", false) : 0;
            if (requireBlockId != 0 || requireLastBlockId != 0) {
                Nxt.getBlockchain().readLock();
            }
            try {
                try {
                    if (apiRequestHandler.startDbTransaction()) {
                        Db.db.beginTransaction();
                    }
                    if (requireBlockId != 0 && !Nxt.getBlockchain().hasBlock(requireBlockId)) {
                        response = REQUIRED_BLOCK_NOT_FOUND;
                        return;
                    }
                    if (requireLastBlockId != 0 && requireLastBlockId != Nxt.getBlockchain().getLastBlock().getId()) {
                        response = REQUIRED_LAST_BLOCK_NOT_FOUND;
                        return;
                    }
                    response = apiRequestHandler.processRequest(req, resp);
                    if (requireLastBlockId == 0 && requireBlockId != 0 && response instanceof JSONObject) {
                        ((JSONObject) response).put("lastBlock", Nxt.getBlockchain().getLastBlock().getStringId());
                    }
                } finally {
                    if (apiRequestHandler.startDbTransaction()) {
                        Db.db.endTransaction();
                    }
                }
            } finally {
                if (requireBlockId != 0 || requireLastBlockId != 0) {
                    Nxt.getBlockchain().readUnlock();
                }
            }
        } catch (ParameterException e) {
            response = e.getErrorResponse();
        } catch (NxtException | RuntimeException e) {
            Logger.logDebugMessage("Error processing API request", e);
            JSONObject json = new JSONObject();
            JSONData.putException(json, e);
            response = JSON.prepare(json);
        } catch (ExceptionInInitializerError err) {
            Logger.logErrorMessage("Initialization Error", err.getCause());
            response = ERROR_INCORRECT_REQUEST;
        } catch (Exception e) {
            Logger.logErrorMessage("Error processing request", e);
            response = ERROR_INCORRECT_REQUEST;
        } finally {
            // The response will be null if we created an asynchronous context
            if (response != null) {
                if (response instanceof JSONObject) {
                    ((JSONObject) response).put("requestProcessingTime", System.currentTimeMillis() - startTime);
                }
                try (Writer writer = resp.getWriter()) {
                    JSON.writeJSONString(response, writer);
                }
            }
        }

    }

}
