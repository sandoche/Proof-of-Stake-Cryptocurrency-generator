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

package nxt.http;

import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

public class APIProxyServletTest {

    @Test
    public void passwordFinder() {
        ByteBuffer postData = ByteBuffer.wrap("abcsecretPhrase=def".getBytes());
        Assert.assertEquals(3, APIProxyServlet.PasswordFinder.process(postData, new String[] { "secretPhrase=" }));
        postData.rewind();
        Assert.assertEquals(-1, APIProxyServlet.PasswordFinder.process(postData, new String[] { "mySecret=" }));
        postData.rewind();
        Assert.assertEquals(3, APIProxyServlet.PasswordFinder.process(postData, new String[] { "mySecret=", "secretPhrase=" }));
        postData.rewind();
        Assert.assertEquals(0, APIProxyServlet.PasswordFinder.process(postData, new String[] { "secretPhrase=", "abc" }));
        postData.rewind();
        Assert.assertEquals(16, APIProxyServlet.PasswordFinder.process(postData, new String[] { "def" }));
    }

}
