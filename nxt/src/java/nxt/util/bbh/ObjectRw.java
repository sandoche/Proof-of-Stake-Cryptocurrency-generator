/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2018 Jelurida IP B.V.
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

package nxt.util.bbh;

import nxt.NxtException;

import java.nio.ByteBuffer;

public interface ObjectRw<T> {
    int getSize(T t);
    T readFromBuffer(ByteBuffer buffer) throws NxtException.NotValidException;
    void writeToBuffer(T t, ByteBuffer buffer);
    boolean validate(T t);
}
