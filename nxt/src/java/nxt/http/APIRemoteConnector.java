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
import nxt.util.MultipartUtility;
import nxt.util.TrustAllSSLProvider;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

public class APIRemoteConnector implements APIConnector {

    private final Map<String, List<String>> params;
    private final Map<String, byte[]> parts;
    private final URL url;

    APIRemoteConnector(Map<String, List<String>> params, Map<String, byte[]> parts, URL url, boolean isTrustRemoteCertificate) {
        this.params = params;
        this.parts = parts;
        this.url = url;
        if (url.getProtocol().equals("https") && isTrustRemoteCertificate) {
            HttpsURLConnection.setDefaultSSLSocketFactory(TrustAllSSLProvider.getSslSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(TrustAllSSLProvider.getHostNameVerifier());
        }
    }

    public InputStream getInputStream() {
        if (!parts.isEmpty()) {
            return sendMultipartRequest();
        } else {
            return sendPostRequest();
        }
    }

    private InputStream sendMultipartRequest() {
        MultipartUtility mu = new MultipartUtility(url, UTF_8);
        params.forEach((key, values) -> values.forEach(v -> mu.addFormField(key, v)));
        parts.forEach((k, part) -> mu.addFilePart(k, k, part));
        return mu.finish();
    }

    private InputStream sendPostRequest() {
        try {
            byte[] postDataBytes = paramsToQueryString(params).getBytes(UTF_8);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            connection.setDoOutput(true);
            connection.getOutputStream().write(postDataBytes);
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                return connection.getInputStream();
            } else {
                Logger.logInfoMessage("response code %d", connection.getResponseCode());
                throw new IllegalStateException("Connection failed response code " + connection.getResponseCode());
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    // visible for testing
    static String paramsToQueryString(Map<String, List<String>> params) {
        return params.entrySet().stream()
                .map(param -> param.getValue().stream().map(value -> urlEncode(param.getKey()) + '=' + urlEncode(value)).collect(Collectors.joining("&")))
                .collect(Collectors.joining("&"));
    }

    private static String urlEncode(String string) {
        try {
            return URLEncoder.encode(string, UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
