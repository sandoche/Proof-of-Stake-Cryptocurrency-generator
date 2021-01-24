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

package nxt.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;

public class MultipartUtility {

    private static final String LINE_FEED = "\r\n";

    private final String boundary;
    private final HttpURLConnection connection;
    private final Charset charset;
    private final OutputStream outputStream;
    private final PrintWriter writer;

    /**
     * This constructor initializes a new HTTP POST request with content type
     * is set to multipart/form-data
     *
     * @param url url
     * @param charset character set
     */
    public MultipartUtility(URL url, Charset charset) {
        this.charset = charset;
        // creates a unique boundary based on time stamp
        boundary = "===" + System.currentTimeMillis() + "===";
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setUseCaches(false);
            connection.setDoOutput(true); // indicates POST method
            connection.setDoInput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            outputStream = connection.getOutputStream();
            writer = new PrintWriter(new OutputStreamWriter(outputStream, charset),true);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Adds a form field to the request
     *
     * @param name  field name
     * @param value field value
     */
    public void addFormField(String name, String value) {
        writer.append("--").append(boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"").append(name).append("\"").append(LINE_FEED);
        writer.append("Content-Type: text/plain; charset=").append(charset.displayName()).append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
    }

    /**
     * Adds a upload file section to the request
     * @param fieldName name attribute in &lt;input type="file" name="..." /&gt;
     * @param fileName name of file
     * @param data file bytes
     */
    public void addFilePart(String fieldName, String fileName, byte[] data) {
        writer.append("--").append(boundary).append(LINE_FEED);
        writer.append("Content-Disposition: form-data; name=\"").append(fieldName).append("\"; filename=\"").append(fileName).append("\"").append(LINE_FEED);
        writer.append("Content-Type: ").append(URLConnection.guessContentTypeFromName(fileName)).append(LINE_FEED);
        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();
        try {
            InputStream inputStream = new ByteArrayInputStream(data);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            inputStream.close();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        writer.append(LINE_FEED);
        writer.flush();
    }

    /**
     * Adds a header field to the request. Unused for now.
     *
     * @param name  - name of the header field
     * @param value - value of the header field
     */
    @SuppressWarnings("unused")
    public void addHeaderField(String name, String value) {
        writer.append(name).append(": ").append(value).append(LINE_FEED);
        writer.flush();
    }

    /**
     * Completes the request and receives response from the server.
     *
     * @return a list of Strings as response in case the server returned
     * status OK, otherwise an exception is thrown.
     */
    public InputStream finish() {
        // The line below appears in the original version but it is added to the class file and addFilePart() already adds a new line
        // writer.append(LINE_FEED).flush();
        writer.append("--").append(boundary).append("--").append(LINE_FEED);
        writer.flush();
        writer.close();

        // checks server's status code first
        try {
            final int status = connection.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                return connection.getInputStream();
            } else {
                throw new IOException("Server returned non-OK status: " + status);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}