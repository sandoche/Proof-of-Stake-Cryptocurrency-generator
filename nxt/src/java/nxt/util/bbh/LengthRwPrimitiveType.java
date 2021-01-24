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

import java.nio.ByteBuffer;
import java.util.function.ToIntFunction;

public enum LengthRwPrimitiveType implements LengthRw {
    BYTE(1, Byte.MAX_VALUE, ByteBuffer::get, (buffer, b) -> buffer.put((byte) b)),
    UBYTE(1, 255, (buffer) -> buffer.get() & 0xff, (buffer, b) -> buffer.put((byte) b)),
    SHORT(2, Short.MAX_VALUE, ByteBuffer::getShort, (buffer, s) -> buffer.putShort((short) s)),
    INTEGER(4, Integer.MAX_VALUE, ByteBuffer::getInt, ByteBuffer::putInt);

    private final int size;
    private final int maxLength;
    private final ToIntFunction<ByteBuffer> readLength;
    private final IntConsumer<ByteBuffer> writeLength;

    LengthRwPrimitiveType(int size, int maxLength, ToIntFunction<ByteBuffer> readLength, IntConsumer<ByteBuffer> writeLength) {
        this.size = size;
        this.maxLength = maxLength;
        this.readLength = readLength;
        this.writeLength = writeLength;
    }

    static LengthRw getByMaxLength(int maxLength) {
        LengthRwPrimitiveType prevValue = null;
        LengthRw result = null;
        for (LengthRwPrimitiveType v : values()) {
            if (prevValue != null && v.maxLength < prevValue.maxLength) {
                throw new RuntimeException("Primitive types not ordered according to their max length");
            }
            if (result == null && maxLength <= v.maxLength) {
                result = v;
            }
            prevValue = v;
        }
        return result;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public int readFromBuffer(ByteBuffer buffer) {
        return readLength.applyAsInt(buffer);
    }

    @Override
    public void writeToBuffer(ByteBuffer buffer, int length) {
        writeLength.consume(buffer, length);
    }

    @Override
    public boolean validate(int length) {
        return length >= 0 && length <= maxLength;
    }
}
