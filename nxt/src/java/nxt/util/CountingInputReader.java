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

package nxt.util;

import nxt.NxtException.NxtIOException;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

/**
 * CountingInputReader extends Reader to count the number of characters read
 */
public class CountingInputReader extends FilterReader {

    /** Current character count */
    private long count = 0;

    /** Maximum character count */
    private final long limit;

    /**
     * Create a CountingInputReader for the supplied Reader
     *
     * @param   reader              Input reader
     * @param   limit               Maximum number of characters to be read
     */
    public CountingInputReader(Reader reader, long limit) {
        super(reader);
        this.limit = limit;
    }

    /**
     * Read a single character
     *
     * @return                      Character or -1 if end of stream reached
     * @throws  IOException         I/O error occurred
     */
    @Override
    public int read() throws IOException {
        int c = super.read();
        if (c != -1)
            incCount(1);
        return c;
    }

    /**
     * Read characters into an array starting at the specified offset
     *
     * @param   cbuf                Character array
     * @param   off                 Starting offset
     * @param   len                 Number of characters to be read
     * @return                      Number of characters read or -1 if end of stream reached
     * @throws  IOException         I/O error occurred
     */
    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        int c = super.read(cbuf, off, len);
        if (c != -1)
            incCount(c);
        return c;
    }

    /**
     * Skip characters in the input stream
     *
     * @param   n                   Number of characters to skip
     * @return                      Number of characters skipped or -1 if end of stream reached
     * @throws  IOException         I/O error occurred
     */
    @Override
    public long skip(long n) throws IOException {
        long c = super.skip(n);
        if (c != -1)
            incCount(c);
        return c;
    }

    /**
     * Return the total number of characters read
     *
     * @return                      Character count
     */
    public long getCount() {
        return count;
    }

    /**
     * Increment the character count and check if the maximum count has been exceeded
     *
     * @param   c                   Number of characters read
     * @throws  NxtIOException      Maximum count exceeded
     */
    private void incCount(long c) throws NxtIOException {
        count += c;
        if (count > limit)
            throw new NxtIOException("Maximum size exceeded: " + count);
    }
}
