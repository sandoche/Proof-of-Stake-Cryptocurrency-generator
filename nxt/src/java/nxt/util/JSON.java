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

package nxt.util;

import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JSON {

    private JSON() {} //never

    public final static JSONStreamAware emptyJSON = prepare(new JSONObject());

    public static JSONStreamAware prepare(final JSONObject json) {
        return new JSONStreamAware() {
            private final char[] jsonChars = JSON.toJSONString(json).toCharArray();
            @Override
            public void writeJSONString(Writer out) throws IOException {
                out.write(jsonChars);
            }
        };
    }

    public static JSONStreamAware prepareRequest(final JSONObject json) {
        json.put("protocol", 1);
        return prepare(json);
    }

    public static String toString(JSONStreamAware jsonStreamAware) {
        StringWriter stringWriter = new StringWriter();
        try {
            JSON.writeJSONString(jsonStreamAware, stringWriter);
        } catch (IOException ignore) {}
        return stringWriter.toString();
    }

    /** String escape pattern */
    private static final Pattern pattern = Pattern.compile(
            "[\"\\\\\\u0008\\f\\n\\r\\t/\\u0000-\\u001f\\u007f-\\u009f\\u2000-\\u20ff\\ud800-\\udbff]");

    /**
     * Create a formatted JSON string
     *
     * @param   json                            JSON list or map
     * @return                                  Formatted string
     */
    public static String toJSONString(JSONAware json) {
        if (json == null)
            return "null";
        if (json instanceof Map) {
            StringBuilder sb = new StringBuilder(1024);
            encodeObject((Map)json, sb);
            return sb.toString();
        }
        if (json instanceof List) {
            StringBuilder sb = new StringBuilder(1024);
            encodeArray((List)json, sb);
            return sb.toString();
        }
        return json.toJSONString();
    }

    /**
     * Write a formatted JSON string
     *
     * @param   json                            JSON list or map
     * @param   writer                          Writer
     * @throws  IOException                     I/O error occurred
     */
    public static void writeJSONString(JSONStreamAware json, Writer writer) throws IOException {
        if (json == null) {
            writer.write("null");
            return;
        }
        if (json instanceof Map) {
            StringBuilder sb = new StringBuilder(1024);
            encodeObject((Map)json, sb);
            writer.write(sb.toString());
            return;
        }
        if (json instanceof List) {
            StringBuilder sb = new StringBuilder(1024);
            encodeArray((List)json, sb);
            writer.write(sb.toString());
            return;
        }
        json.writeJSONString(writer);
    }

    /**
     * Create a formatted string from a list
     *
     * @param   list                            List
     * @param   sb                              String builder
     */
    private static void encodeArray(List<?> list, StringBuilder sb) {
        if (list == null) {
            sb.append("null");
            return;
        }
        boolean firstElement = true;
        sb.append('[');
        for (Object obj : list) {
            if (firstElement)
                firstElement = false;
            else
                sb.append(',');
            encodeValue(obj, sb);
        }
        sb.append(']');
    }

    /**
     * Create a formatted string from a map
     *
     * @param   map                             Map
     * @param   sb                              String builder
     */
    public static void encodeObject(Map<?, ?> map, StringBuilder sb) {
        if (map == null) {
            sb.append("null");
            return;
        }
        Set<Map.Entry<Object, Object>> entries = (Set)map.entrySet();
        Iterator<Map.Entry<Object, Object>> it = entries.iterator();
        boolean firstElement = true;
        sb.append('{');
        while (it.hasNext()) {
            Map.Entry<Object, Object> entry = it.next();
            Object key = entry.getKey();
            Object value = entry.getValue();
            if (key == null)
                continue;
            if (firstElement)
                firstElement = false;
            else
                sb.append(',');
            sb.append('\"').append(key.toString()).append("\":");
            encodeValue(value, sb);
        }
        sb.append('}');
    }

    /**
     * Encode a JSON value
     *
     * @param   value                           JSON value
     * @param   sb                              String builder
     */
    public static void encodeValue(Object value, StringBuilder sb) {
        if (value == null) {
            sb.append("null");
        } else if (value instanceof Double) {
            if (((Double)value).isInfinite() || ((Double)value).isNaN())
                sb.append("null");
            else
                sb.append(value.toString());
        } else if (value instanceof Float) {
            if (((Float)value).isInfinite() || ((Float)value).isNaN())
                sb.append("null");
            else
                sb.append(value.toString());
        } else if (value instanceof Number) {
            sb.append(value.toString());
        } else if (value instanceof Boolean) {
            sb.append(value.toString());
        } else if (value instanceof Map) {
            encodeObject((Map<Object, Object>)value, sb);
        } else if (value instanceof List) {
            encodeArray((List<Object>)value, sb);
        } else {
            sb.append('\"');
            escapeString(value.toString(), sb);
            sb.append('\"');
        }
    }

    /**
     * Escape control characters in a string and append them to the string buffer
     *
     * @param   string                      String to be written
     * @param   sb                          String builder
     */
    private static void escapeString(String string, StringBuilder sb) {
        if (string.length() == 0)
            return;
        //
        // Find the next special character in the string
        //
        int start = 0;
        Matcher matcher = pattern.matcher(string);
        while (matcher.find(start)) {
            int pos = matcher.start();
            if (pos > start)
                sb.append(string.substring(start, pos));
            start = pos + 1;
            //
            // Escape control characters
            //
            char c = string.charAt(pos);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '/':
                    sb.append("\\/");
                    break;
                default:
                    if((c>='\u0000' && c<='\u001F') || (c>='\u007F' && c<='\u009F') || (c>='\u2000' && c<='\u20FF')){
                        sb.append("\\u").append(String.format("%04X", (int)c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        //
        // Append the remainder of the string
        //
        if (start == 0)
            sb.append(string);
        else if (start < string.length())
            sb.append(string.substring(start));
    }
}
