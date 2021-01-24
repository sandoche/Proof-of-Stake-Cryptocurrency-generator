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

import nxt.peer.Peer;
import nxt.peer.Peers;
import nxt.util.Convert;
import nxt.util.JSON;
import nxt.util.Logger;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.proxy.AsyncMiddleManServlet;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
import org.json.simple.JSONStreamAware;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;

import static nxt.http.JSONResponses.ERROR_NOT_ALLOWED;

public final class APIProxyServlet extends AsyncMiddleManServlet {

    private static final String REMOTE_URL = APIProxyServlet.class.getName() + ".remoteUrl";
    private static final String REMOTE_SERVER_IDLE_TIMEOUT = APIProxyServlet.class.getName() + ".remoteServerIdleTimeout";
    static final int PROXY_IDLE_TIMEOUT_DELTA = 5000;

    static void initClass() {}

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        config.getServletContext().setAttribute("apiServlet", new APIServlet());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONStreamAware responseJson = null;
        try {
            if (!API.isAllowed(request.getRemoteHost())) {
                responseJson = ERROR_NOT_ALLOWED;
                return;
            }
            MultiMap<String> parameters = getRequestParameters(request);
            String requestType = getRequestType(parameters);
            if (APIProxy.isActivated() && isForwardable(requestType)) {
                if (parameters.containsKey("secretPhrase") || parameters.containsKey("adminPassword") || parameters.containsKey("sharedKey")) {
                    throw new ParameterException(JSONResponses.PROXY_SECRET_DATA_DETECTED);
                }
                if (!initRemoteRequest(request, requestType)) {
                    if (Peers.getPeers(peer -> peer.getState() == Peer.State.CONNECTED, 1).size() >= 1) {
                        responseJson = JSONResponses.API_PROXY_NO_OPEN_API_PEERS;
                    } else {
                        responseJson = JSONResponses.API_PROXY_NO_PUBLIC_PEERS;
                    }
                } else {
                    super.service(request, response);
                }
            } else {
                APIServlet apiServlet = (APIServlet)request.getServletContext().getAttribute("apiServlet");
                apiServlet.service(request, response);
            }
        } catch (ParameterException e) {
            responseJson = e.getErrorResponse();
        } finally {
            if (responseJson != null) {
                try {
                    try (Writer writer = response.getWriter()) {
                        JSON.writeJSONString(responseJson, writer);
                    }
                } catch(IOException e) {
                    Logger.logInfoMessage("Failed to write response to client", e);
                }
            }
        }
    }

    private MultiMap<String> getRequestParameters(HttpServletRequest request) {
        MultiMap<String> parameters = new MultiMap<>();
        String queryString = request.getQueryString();
        if (queryString != null) {
            UrlEncoded.decodeUtf8To(queryString, parameters);
        }
        return parameters;
    }

    @Override
    protected void addProxyHeaders(HttpServletRequest clientRequest, Request proxyRequest) {

    }

    @Override
    protected HttpClient newHttpClient() {
        return HttpClientFactory.newHttpClient();
    }

    @Override
    protected String rewriteTarget(HttpServletRequest clientRequest) {

        Integer timeout = (Integer) clientRequest.getAttribute(REMOTE_SERVER_IDLE_TIMEOUT);
        HttpClient httpClient = getHttpClient();
        if (timeout != null && httpClient != null) {
            httpClient.setIdleTimeout(Math.max(timeout - PROXY_IDLE_TIMEOUT_DELTA, 0));
        }

        String remoteUrl = (String) clientRequest.getAttribute(REMOTE_URL);
        URI rewrittenURI = URI.create(remoteUrl).normalize();
        return rewrittenURI.toString();
    }

    @Override
    protected void onClientRequestFailure(HttpServletRequest clientRequest, Request proxyRequest,
                                          HttpServletResponse proxyResponse, Throwable failure) {
        if (failure instanceof PasswordDetectedException) {
            PasswordDetectedException passwordDetectedException = (PasswordDetectedException) failure;
            try (Writer writer = proxyResponse.getWriter()) {
                JSON.writeJSONString(passwordDetectedException.errorResponse, writer);
                sendProxyResponseError(clientRequest, proxyResponse, HttpStatus.OK_200);
            } catch (IOException e) {
                e.addSuppressed(failure);
                super.onClientRequestFailure(clientRequest, proxyRequest, proxyResponse, e);
            }
        } else {
            super.onClientRequestFailure(clientRequest, proxyRequest, proxyResponse, failure);
        }
    }

    private String getRequestType(MultiMap<String> parameters) throws ParameterException {
        String requestType = parameters.getString("requestType");
        if (Convert.emptyToNull(requestType) == null) {
            throw new ParameterException(JSONResponses.PROXY_MISSING_REQUEST_TYPE);
        }

        APIServlet.APIRequestHandler apiRequestHandler = APIServlet.apiRequestHandlers.get(requestType);
        if (apiRequestHandler == null) {
            if (APIServlet.disabledRequestHandlers.containsKey(requestType)) {
                throw new ParameterException(JSONResponses.ERROR_DISABLED);
            } else {
                throw new ParameterException(JSONResponses.ERROR_INCORRECT_REQUEST);
            }
        }
        return requestType;
    }

    private boolean initRemoteRequest(HttpServletRequest clientRequest, String requestType) {
        StringBuilder uri;
        if (!APIProxy.forcedServerURL.isEmpty()) {
            uri = new StringBuilder();
            uri.append(APIProxy.forcedServerURL);
        } else {
            Peer servingPeer = APIProxy.getInstance().getServingPeer(requestType);
            if (servingPeer == null) {
                return false;
            }
            uri = servingPeer.getPeerApiUri();
            clientRequest.setAttribute(REMOTE_SERVER_IDLE_TIMEOUT, servingPeer.getApiServerIdleTimeout());
        }
        uri.append("/nxt");
        String query = clientRequest.getQueryString();
        if (query != null) {
            uri.append("?").append(query);
        }
        clientRequest.setAttribute(REMOTE_URL, uri.toString());
        return true;
    }

    private boolean isForwardable(String requestType) {
        APIServlet.APIRequestHandler apiRequestHandler = APIServlet.apiRequestHandlers.get(requestType);
        if (!apiRequestHandler.requireBlockchain()) {
            return false;
        }
        if (apiRequestHandler.requireFullClient()) {
            return false;
        }
        if (APIProxy.NOT_FORWARDED_REQUESTS.contains(requestType)) {
            return false;
        }

        return true;
    }

    @Override
    protected Response.Listener newProxyResponseListener(HttpServletRequest request, HttpServletResponse response) {
        return new APIProxyResponseListener(request, response);
    }

    private class APIProxyResponseListener extends AsyncMiddleManServlet.ProxyResponseListener {

        APIProxyResponseListener(HttpServletRequest request, HttpServletResponse response) {
            super(request, response);
        }

        @Override
        public void onFailure(Response response, Throwable failure) {
            super.onFailure(response, failure);
            Logger.logErrorMessage("proxy failed", failure);
            APIProxy.getInstance().blacklistHost(response.getRequest().getHost());
        }
    }

    @Override
    protected ContentTransformer newClientRequestContentTransformer(HttpServletRequest clientRequest, Request proxyRequest) {
        String contentType = clientRequest.getContentType();
        if (contentType != null && contentType.contains("multipart")) {
            return super.newClientRequestContentTransformer(clientRequest, proxyRequest);
        } else {
            if (APIProxy.isActivated() && isForwardable(clientRequest.getParameter("requestType"))) {
                return new PasswordFilteringContentTransformer();
            } else {
                return super.newClientRequestContentTransformer(clientRequest, proxyRequest);
            }
        }
    }

    private static class PasswordDetectedException extends RuntimeException {
        private final JSONStreamAware errorResponse;

        private PasswordDetectedException(JSONStreamAware errorResponse) {
            this.errorResponse = errorResponse;
        }
    }

    static class PasswordFinder {

        static int process(ByteBuffer buffer, String[] secrets) {
            try {
                int[] pos = new int[secrets.length];
                byte[][] tokens = new byte[secrets.length][];
                for (int i = 0; i < tokens.length; i++) {
                    tokens[i] = secrets[i].getBytes();
                }
                while (buffer.hasRemaining()) {
                    byte current = buffer.get();
                    for (int i = 0; i < tokens.length; i++) {
                        if (current != tokens[i][pos[i]]) {
                            pos[i] = 0;
                            continue;
                        }
                        pos[i]++;
                        if (pos[i] == tokens[i].length) {
                            return buffer.position() - tokens[i].length;
                        }
                    }
                }
                return -1;
            } finally {
                buffer.rewind();
            }
        }
    }

    private static class PasswordFilteringContentTransformer implements AsyncMiddleManServlet.ContentTransformer {

        ByteArrayOutputStream os;

        @Override
        public void transform(ByteBuffer input, boolean finished, List<ByteBuffer> output) throws IOException {
            if (finished) {
                ByteBuffer allInput;
                if (os == null) {
                    allInput = input;
                } else {
                    byte[] b = new byte[input.remaining()];
                    input.get(b);
                    os.write(b);
                    allInput = ByteBuffer.wrap(os.toByteArray());
                }
                int tokenPos = PasswordFinder.process(allInput, new String[] { "secretPhrase=", "adminPassword=", "sharedKey=" });
                if (tokenPos >= 0) {
                    JSONStreamAware error = JSONResponses.PROXY_SECRET_DATA_DETECTED;
                    throw new PasswordDetectedException(error);
                }
                output.add(allInput);
            } else {
                if (os == null) {
                    os = new ByteArrayOutputStream();
                }
                byte[] b = new byte[input.remaining()];
                input.get(b);
                os.write(b);
            }
        }
    }
}