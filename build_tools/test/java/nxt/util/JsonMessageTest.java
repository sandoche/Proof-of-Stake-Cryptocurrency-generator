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

package nxt.util;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

public class JsonMessageTest {

    @Test
    public void message() {
        validate("{\n  \"type\": \"dividend\",\n  \"contractId\": \"2112610727280991058\",\n  \"height\": 260315,\n  \"total\": \"42700000000\",\n  \"percentage\": \"0%\",\n  \"shares\": 50\n}");
        validate("{\\n  \"type\": \"dividend\",\\n  \"contractId\": \"2112610727280991058\",\\n  \"height\": 260315,\\n  \"total\": \"42700000000\",\\n  \"percentage\": \"0%\",\\n  \"shares\": 50\\n}");
        validate("{\n" +
                "  \"type\": \"dividend\",\n" +
                "  \"contractId\": \"11263051911300205537\",\n" +
                "  \"height\": 260315,\n" +
                "  \"total\": \"42700000000\",\n" +
                "  \"percentage\": \"0.1%\",\n" +
                "  \"shares\": 1000\n" +
                "}");
        validate("{\n" +
                "  \"type\": \"dividend\",\n" +
                "  \"contractId\": \"11263051911300205537\",\n" +
                "  \"height\": 260315,\n" +
                "  \"total\": \"42700000000\",\n" +
                "  \"percentage\": \"0.01%\",\n" +
                "  \"shares\": 70\n" +
                "}");
        validate("{\n" +
                "  \"type\": \"dividend\",\n" +
                "  \"contractId\": \"2112610727280991058\",\n" +
                "  \"height\": 260315,\n" +
                "  \"total\": \"42700000000\",\n" +
                "  \"percentage\": \"0.33%\",\n" +
                "  \"shares\": 5383\n" +
                "}");
        validate("{\n" +
                "  \"type\": \"dividend\",\n" +
                "  \"contractId\": \"11263051911300205537\",\n" +
                "  \"height\": 260315,\n" +
                "  \"total\": \"42700000000\",\n" +
                "  \"percentage\": \"0.1%\",\n" +
                "  \"shares\": 1000\n" +
                "}");
        validate("{\n" +
                "  \"type\": \"dividend\",\n" +
                "  \"contractId\": \"2112610727280991058\",\n" +
                "  \"height\": 260315,\n" +
                "  \"total\": \"42700000000\",\n" +
                "  \"percentage\": \"0.18%\",\n" +
                "  \"shares\": 3000\n" +
                "}");
        validate("אבג");
        validate("“");
    }

    private void validate(String message) {
        JSONObject request = new JSONObject();
        request.put("message", message);

        JSONObject response;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            try (Writer writer = new OutputStreamWriter(byteArrayOutputStream, "UTF-8")) {
                request.writeJSONString(writer);
            }
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            try (Reader reader = new BufferedReader(new InputStreamReader(byteArrayInputStream, "UTF-8"))) {
                response = (JSONObject) JSONValue.parse(reader);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        Assert.assertEquals(message, response.get("message"));
    }

}
