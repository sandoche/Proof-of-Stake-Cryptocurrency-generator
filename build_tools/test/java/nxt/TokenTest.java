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

package nxt;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TokenTest extends BlockchainTest {
    @Test
    public void testParseValidToken() throws Exception {
        String token = "6s7hchl9q0e5jgrrtgscoip2lcb2o3oi7ndso1bnjr475suv001ug93uu8aq2f00o7q6pvs2ivrpra1svouvb4k5nreco0tt94qest9mq5jg2qihcvj5n5ljqht5fl6n39nslr7kidqh8kh8u8v6e4rn92f47l3i";
        Token actual = Token.parseToken(token, "http://nxt.org");

        assertEquals(1000, actual.getTimestamp());
        assertTrue(actual.isValid());
    }

    @Test
    public void testParseInValidToken() throws Exception {
        String token = "6s7hchl9q0e5jgrrtgscoip2lcb2o3oi7ndso1bnjr475suv001ug93uu8aq2f00o7q6pvs2ivrpra1svouvb4k5nreco0tt94qest9mq5jg2qihcvj5n5ljqht5fl6n39nslr7kidqh8kh8u8v6e4rn92f47l3i";
        Token actual = Token.parseToken(token, "http://next.org");

        assertEquals(1000, actual.getTimestamp());
        assertFalse(actual.isValid());
    }

    @Test
    public void testGenerateToken() throws Exception {
        int start = Nxt.getEpochTime();
        String tokenString = Token.generateToken("secret", "http://nxt.org");
        int end = Nxt.getEpochTime();
        Token token = Token.parseToken(tokenString, "http://nxt.org");

        assertTrue(token.isValid());
        assertTrue(token.getTimestamp() >= start);
        assertTrue(token.getTimestamp() <= end);
    }

    @Test
    public void emptySecret() throws Exception {
        String tokenString = Token.generateToken("", "http://nxt.org");
        Token token = Token.parseToken(tokenString, "http://nxt.org");
        assertTrue(token.isValid());
    }

    @Test
    public void emptySite() throws Exception {
        String tokenString = Token.generateToken("secret", "");
        Token token = Token.parseToken(tokenString, "");
        assertTrue(token.isValid());
    }

    @Test
    public void veryLongSite() throws Exception {
        StringBuilder site = new StringBuilder(6 * 100000);
        for (int i = 0; i < 100000; i++) {
            site.append("abcd10");
        }
        String tokenString = Token.generateToken("secret", site.toString());

        Token token = Token.parseToken(tokenString, site.toString());
        assertTrue(token.isValid());
    }
}
