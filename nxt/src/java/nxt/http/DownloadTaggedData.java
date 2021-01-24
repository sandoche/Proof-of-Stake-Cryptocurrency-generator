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

import nxt.Nxt;
import nxt.NxtException;
import nxt.TaggedData;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static nxt.http.JSONResponses.PRUNED_TRANSACTION;

public final class DownloadTaggedData extends APIServlet.APIRequestHandler {

    static final DownloadTaggedData instance = new DownloadTaggedData();

    private DownloadTaggedData() {
        super(new APITag[] {APITag.DATA}, "transaction", "retrieve");
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest request, HttpServletResponse response) throws NxtException {
        long transactionId = ParameterParser.getUnsignedLong(request, "transaction", true);
        boolean retrieve = "true".equalsIgnoreCase(request.getParameter("retrieve"));
        TaggedData taggedData = TaggedData.getData(transactionId);
        if (taggedData == null && retrieve) {
            if (Nxt.getBlockchainProcessor().restorePrunedTransaction(transactionId) == null) {
                return PRUNED_TRANSACTION;
            }
            taggedData = TaggedData.getData(transactionId);
        }
        if (taggedData == null) {
            return JSONResponses.incorrect("transaction", "Tagged data not found");
        }
        byte[] data = taggedData.getData();
        if (!taggedData.getType().equals("")) {
            response.setContentType(taggedData.getType());
        } else {
            response.setContentType("application/octet-stream");
        }
        String filename = taggedData.getFilename();
        if (filename == null || filename.trim().isEmpty()) {
            filename = taggedData.getName().trim();
        }
        String contentDisposition = "attachment";
        try {
            URI uri = new URI(null, null, filename, null);
            contentDisposition += "; filename*=UTF-8''" + uri.toASCIIString();
        } catch (URISyntaxException ignore) {}
        response.setHeader("Content-Disposition", contentDisposition);
        response.setContentLength(data.length);
        try (OutputStream out = response.getOutputStream()) {
            try {
                out.write(data);
            } catch (IOException e) {
                throw new ParameterException(JSONResponses.RESPONSE_WRITE_ERROR);
            }
        } catch (IOException e) {
            throw new ParameterException(JSONResponses.RESPONSE_STREAM_ERROR);
        }
        return null;
    }

    @Override
    protected JSONStreamAware processRequest(HttpServletRequest request) {
        throw new UnsupportedOperationException();
    }
}
