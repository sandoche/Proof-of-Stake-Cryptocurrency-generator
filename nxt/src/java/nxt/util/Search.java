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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.tika.Tika;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class Search {

    private static final Analyzer analyzer = new StandardAnalyzer();

    public static String[] parseTags(String tags, int minTagLength, int maxTagLength, int maxTagCount) {
        if (tags.trim().length() == 0) {
            return Convert.EMPTY_STRING;
        }
        List<String> list = new ArrayList<>();
        try (TokenStream stream = analyzer.tokenStream(null, tags)) {
            CharTermAttribute attribute = stream.addAttribute(CharTermAttribute.class);
            String tag;
            stream.reset();
            while (stream.incrementToken() && list.size() < maxTagCount &&
                    (tag = attribute.toString()).length() <= maxTagLength && tag.length() >= minTagLength) {
                if (!list.contains(tag)) {
                    list.add(tag);
                }
            }
            stream.end();
        } catch (IOException e) {
            throw new RuntimeException(e.toString(), e);
        }
        return list.toArray(new String[list.size()]);
    }

    public static String detectMimeType(byte[] data, String filename) {
        Tika tika = new Tika();
        return tika.detect(data, filename);
    }

    public static String detectMimeType(byte[] data) {
        Tika tika = new Tika();
        try {
            return tika.detect(data);
        } catch (NoClassDefFoundError e) {
            Logger.logErrorMessage("Error running Tika parsers", e);
            return null;
        }
    }

    private Search() {}

}
