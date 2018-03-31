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

package nxt.peer;

import org.junit.Test;

import java.nio.BufferUnderflowException;

import static org.junit.Assert.assertEquals;

public class HallmarkTest {
    @Test(expected = StringIndexOutOfBoundsException.class)
    public void parseEmptyDateShouldThrowStringIndexOutOfBoundsException() {
        Hallmark.parseDate("");
    }

    @Test
    public void parseValidDate() {
        int actual = Hallmark.parseDate("1984-04-13");
        assertEquals(19840413, actual);
    }

    @Test
    public void formatZeroDate() {
        String actual = Hallmark.formatDate(0);
        assertEquals("0000-00-00", actual);
    }

    @Test
    public void formatValidDate() {
        String actual = Hallmark.formatDate(19840413);
        assertEquals("1984-04-13", actual);
    }

    @Test(expected = BufferUnderflowException.class)
    public void parseEmptyHallmarkShouldThrowBufferUnderflowException() {
        Hallmark.parseHallmark("");
    }

    @Test
    public void parseHallmark() {
        String hallmark = "a946160f377bc3591cd0224bcc38ec120f2c16ab7705ccdb3ddff372c89e7e2407006e78742e6f7267640000009dbd2e012ad53f8bd5e429bdec496345fdfd5af8f9afae7961981233b79efd01faf14769070516b0252662f149b7ae7a5994a854d5a3b13174ed2cf19dd249f5ff3c99f6b1";
        Hallmark actual = Hallmark.parseHallmark(hallmark);
        assertEquals("nxt.org", actual.getHost());
        assertEquals(100, actual.getWeight());
    }

    @Test
    public void generateHallmark() {
        String hallmarkString = Hallmark.generateHallmark("secret", "nxt.org", 100, 19840413);
        Hallmark hallmark = Hallmark.parseHallmark(hallmarkString);

        assertEquals(-8123967981207937138L, hallmark.getAccountId());
        assertEquals("nxt.org", hallmark.getHost());
        assertEquals(100, hallmark.getWeight());
        assertEquals(19840413, hallmark.getDate());
    }
}
