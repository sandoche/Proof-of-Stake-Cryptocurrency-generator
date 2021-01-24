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

package nxt.util;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

public class TrustAllSSLProvider {

    // Verify-all name verifier
    private final static HostnameVerifier hostNameVerifier = (hostname, session) -> true;

    // Trust-all socket factory
    private static final SSLSocketFactory sslSocketFactory;
    static {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};
        SSLContext sc;
        try {
            sc = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
        try {
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
        } catch (KeyManagementException e) {
            throw new IllegalStateException(e);
        }
        sslSocketFactory = sc.getSocketFactory();
    }

    public static HostnameVerifier getHostNameVerifier() {
        return hostNameVerifier;
    }

    public static SSLSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }
}
