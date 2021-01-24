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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

class MockedResponse implements HttpServletResponse {

    private int contentLength;
    private final ServletOutputStream servletOutputStream;
    private final PrintWriter writer;

    MockedResponse(ByteArrayOutputStream out) {
        this.servletOutputStream = new MockedServletOutputStream(out);
        this.writer = new PrintWriter(new OutputStreamWriter(out));
    }

    @Override
    public void addCookie(Cookie cookie) {
        throw new UnsupportedOperationException();        
    }

    @Override
    public boolean containsHeader(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String encodeURL(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String encodeRedirectURL(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public String encodeUrl(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public String encodeRedirectUrl(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendError(int i, String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendError(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendRedirect(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setDateHeader(String s, long l) {}

    @Override
    public void addDateHeader(String s, long l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHeader(String s, String s1) {}

    @Override
    public void addHeader(String s, String s1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setIntHeader(String s, int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addIntHeader(String s, int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStatus(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public void setStatus(int i, String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getStatus() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getHeader(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> getHeaders(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> getHeaderNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getCharacterEncoding() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getContentType() {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unused")
    public int getContentLength() {
        return contentLength;
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return servletOutputStream;
    }

    @Override
    public PrintWriter getWriter() {
        return writer;
    }

    @Override
    public void setCharacterEncoding(String s) {
    }

    @Override
    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    @Override
    public void setContentLengthLong(long l) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setContentType(String s) {}

    @Override
    public void setBufferSize(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getBufferSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void flushBuffer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resetBuffer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCommitted() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLocale(Locale locale) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Locale getLocale() {
        throw new UnsupportedOperationException();
    }
}
