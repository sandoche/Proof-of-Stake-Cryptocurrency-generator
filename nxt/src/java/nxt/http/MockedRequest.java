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

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import static java.util.stream.Collectors.toMap;

public class MockedRequest implements HttpServletRequest {

    private final Map<String, List<String>> params;
    private final Map<String, String[]> parameterMap;
    private final Map<String, Part> partsMap;

    public MockedRequest(Map<String, List<String>> params, Map<String, byte[]> parts) {
        this.params = params;
        parameterMap = params.entrySet().stream()
                .collect(toMap(Entry::getKey, e -> e.getValue().toArray(new String[0])));
        partsMap = parts.entrySet().stream()
                .collect(toMap(Entry::getKey, e -> new MockedPart(e.getValue())));
    }

    private String firstOrNull(List<String> list) {
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    private String[] toArrayOrNull(List<String> list) {
        if (list != null) {
            return list.toArray(new String[0]);
        }
        return null;
    }

    @Override
    public String getAuthType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Cookie[] getCookies() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getDateHeader(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getHeader(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Enumeration<String> getHeaders(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getIntHeader(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getMethod() {
        return "POST";
    }

    @Override
    public String getPathInfo() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPathTranslated() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getContextPath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getQueryString() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRemoteUser() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isUserInRole(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Principal getUserPrincipal() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRequestedSessionId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRequestURI() {
        throw new UnsupportedOperationException();
    }

    @Override
    public StringBuffer getRequestURL() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getServletPath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpSession getSession(boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpSession getSession() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String changeSessionId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public boolean isRequestedSessionIdFromUrl() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean authenticate(HttpServletResponse httpServletResponse) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void login(String s, String s1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void logout() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Part> getParts() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Part getPart(String key) {
        return partsMap.get(key);
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> aClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getAttribute(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCharacterEncoding() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCharacterEncoding(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getContentLength() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getContentLengthLong() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getContentType() {
        if (partsMap.isEmpty()) {
            return "application/x-www-form-urlencoded";
        }
        return "multipart/form-data";
    }

    @Override
    public ServletInputStream getInputStream() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getParameter(String key) {
        return firstOrNull(params.get(key));
    }

    @Override
    public Enumeration<String> getParameterNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getParameterValues(String key) {
        return toArrayOrNull(params.get(key));
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return parameterMap;
    }

    @Override
    public String getProtocol() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getScheme() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getServerName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getServerPort() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BufferedReader getReader() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRemoteAddr() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRemoteHost() {
        return "localhost";
    }

    @Override
    public void setAttribute(String s, Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAttribute(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Locale getLocale() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Enumeration<Locale> getLocales() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSecure() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public String getRealPath(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getRemotePort() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLocalName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getLocalAddr() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getLocalPort() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServletContext getServletContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAsyncStarted() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAsyncSupported() {
        throw new UnsupportedOperationException();
    }

    @Override
    public AsyncContext getAsyncContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DispatcherType getDispatcherType() {
        throw new UnsupportedOperationException();
    }

    static class MockedPart implements Part {

        private final byte[] bytes;

        MockedPart(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(bytes);
        }

        @Override
        public String getContentType() {
            return null;
        }

        @Override
        public String getName() {
            return "testName";
        }

        @Override
        public String getSubmittedFileName() {
            return "testSubmittedFileName";
        }

        @Override
        public long getSize() {
            return bytes.length;
        }

        @Override
        public void write(String s) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getHeader(String s) {
            return null;
        }

        @Override
        public Collection<String> getHeaders(String s) {
            return null;
        }

        @Override
        public Collection<String> getHeaderNames() {
            return null;
        }
    }

}
