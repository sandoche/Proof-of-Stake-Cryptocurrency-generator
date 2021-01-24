/*
 * Copyright © 2013-2016 The Nxt Core Developers.
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

package nxt.util.bbh;

import nxt.NxtException;
import nxt.util.Convert;

import java.nio.ByteBuffer;

public class StringRw implements ObjectRw<String> {
    private final ByteArrayRw byteArrayRw;
    private final StringLengthRw lengthRw;

    public StringRw(LengthRwPrimitiveType lengthRw, int maxCharLength) {
        this(new StringLengthRw(lengthRw, maxCharLength));
    }

    StringRw(StringLengthRw lengthRw) {
        this.byteArrayRw = new ByteArrayRw(lengthRw);
        this.lengthRw = lengthRw;
    }

    @Override
    public int getSize(String string) {
        return byteArrayRw.getSize(Convert.toBytes(string));
    }

    @Override
    public String readFromBuffer(ByteBuffer buffer) throws NxtException.NotValidException {
        return fromBytes(byteArrayRw.readFromBuffer(buffer));
    }

    private static String fromBytes(byte[] bytes) {
        return Convert.toString(bytes);
    }

    @Override
    public void writeToBuffer(String t, ByteBuffer buffer) {
        byteArrayRw.writeToBuffer(Convert.toBytes(t), buffer);
    }

    @Override
    public boolean validate(String s) {
        if (s.length() > lengthRw.getMaxCharCount()) {
            return false;
        }
        if (lengthRw.validate(Convert.getMaxStringSize(s.length()))) {
            //The string will fit into the length field however it is converted to bytes. No need to actually convert it
            return true;
        }
        byte[] bytes = Convert.toBytes(s);
        return lengthRw.validate(bytes.length);
    }
}
