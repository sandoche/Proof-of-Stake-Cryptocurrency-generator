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

package nxt.crypto;

public final class KNV25 {

    private static final long[] constants = {
                1L, 32898L, -9223372036854742902L, -9223372034707259392L, 32907L,
                2147483649L, -9223372034707259263L, -9223372036854743031L, 138L, 136L,
                2147516425L, 2147483658L, 2147516555L, -9223372036854775669L, -9223372036854742903L,
                -9223372036854743037L, -9223372036854743038L, -9223372036854775680L, 32778L, -9223372034707292150L,
                -9223372034707259263L, -9223372036854742912L, 2147483649L, -9223372034707259384L, 1L
            };

    @SuppressWarnings("ShiftOutOfRange")
    public static byte[] hash(final byte input[]) {
        int inputOffset = 0;
        int inputLength = input.length;
        if (inputLength % 8 != 0) {
            throw new IllegalArgumentException(String.format("input length %d must be a multiple of 8", inputLength));
        }
        byte[] output = new byte[32];
        long[] state = new long[25];
        while (inputOffset <= inputLength) {
            int i = 0;
            while (inputOffset < inputLength && i < 17) {
                state[i++] ^= ((long)(input[inputOffset++] & 0xFF)) | (((long)(input[inputOffset++] & 0xFF)) << 8) | (((long)(input[inputOffset++] & 0xFF)) << 16) | (((long)(input[inputOffset++] & 0xFF)) << 24) | (((long)(input[inputOffset++] & 0xFF)) << 32) | (((long)(input[inputOffset++] & 0xFF)) << 40) | (((long)(input[inputOffset++] & 0xFF)) << 48) | (((long)(input[inputOffset++] & 0xFF)) << 56);
            }
            if (inputOffset == inputLength && i < 17) {
                state[i] ^= 1;
                state[16] ^= -9223372036854775808L;
                inputOffset++;
            }
            long state0 = state[0], state1 = state[1], state2 = state[2], state3 = state[3], state4 = state[4], state5 = state[5], state6 = state[6], state7 = state[7], state8 = state[8], state9 = state[9], state10 = state[10], state11 = state[11], state12 = state[12], state13 = state[13], state14 = state[14], state15 = state[15], state16 = state[16], state17 = state[17], state18 = state[18], state19 = state[19], state20 = state[20], state21 = state[21], state22 = state[22], state23 = state[23], state24 = state[24];
            for (i = 0; i < 25; ) {
                long t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19;
                t12 = state1 ^ (t3 = (t1 = state0 ^ state5 ^ state10 ^ state15 ^ state20) ^ (((t2 = state2 ^ state7 ^ state12 ^ state17 ^ state22) << 1) | (t2 >>> -1)));
                t13 = state2 ^ (t6 = (t4 = state1 ^ state6 ^ state11 ^ state16 ^ state21) ^ (((t5 = state3 ^ state8 ^ state13 ^ state18 ^ state23) << 1) | (t5 >>> -1)));
                state0 = (t9 = state0 ^ (t8 = ((t4 << 1) | (t4 >>> -1)) ^ (t7 = state4 ^ state9 ^ state14 ^ state19 ^ state24))) ^ ((~(t16 = ((t16 = state6 ^ t3) << 44) | (t16 >>> -44))) & (state2 = ((state2 = state12 ^ t6) << 43) | (state2 >>> -43))) ^ constants[i++];
                t14 = state3 ^ (t10 = ((t7 << 1) | (t7 >>> -1)) ^ t2);
                state1 = t16 ^ ((~state2) & (state3 = ((state3 = state18 ^ t10) << 21) | (state3 >>> -21)));
                t15 = state4 ^ (t11 = ((t1 << 1) | (t1 >>> -1)) ^ t5);
                state2 ^= (~state3) & (state4 = ((state4 = state24 ^ t11) << 14) | (state4 >>> -14));
                state3 ^= (~state4) & t9;
                state4 ^= (~t9) & t16;
                t16 = state5 ^ t8;
                t17 = state7 ^ t6;
                state5 = (t14 = (t14 << 28) | (t14 >>> -28)) ^ ((~(t19 = ((t19 = state9 ^ t11) << 20) | (t19 >>> -20))) & (state7 = ((state7 = state10 ^ t8) << 3) | (state7 >>> -3)));
                t18 = state8 ^ t10;
                state6 = t19 ^ ((~state7) & (state8 = ((state8 = state16 ^ t3) << 45) | (state8 >>> -45)));
                state7 ^= (~state8) & (state9 = ((state9 = state22 ^ t6) << 61) | (state9 >>> -61));
                state8 ^= (~state9) & t14;
                state9 ^= (~t14) & t19;
                t19 = state11 ^ t3;
                state10 = (t12 = (t12 << 1) | (t12 >>> -1)) ^ ((~(t17 = (t17 << 6) | (t17 >>> -6))) & (state12 = ((state12 = state13 ^ t10) << 25) | (state12 >>> -25)));
                state11 = t17 ^ ((~state12) & (state13 = ((state13 = state19 ^ t11) << 8) | (state13 >>> -8)));
                t14 = state14 ^ t11;
                state12 ^= (~state13) & (state14 = ((state14 = state20 ^ t8) << 18) | (state14 >>> -18));
                state13 ^= (~state14) & t12;
                state14 ^= (~t12) & t17;
                t12 = state15 ^ t8;
                t17 = state17 ^ t6;
                state15 = (t15 = (t15 << 27) | (t15 >>> -27)) ^ ((~(t16 = (t16 << 36) | (t16 >>> -36))) & (state17 = (t19 << 10) | (t19 >>> -10)));
                state16 = t16 ^ ((~state17) & (state18 = (t17 << 15) | (t17 >>> -15)));
                state17 ^= (~state18) & (state19 = ((state19 = state23 ^ t10) << 56) | (state19 >>> -56));
                state18 ^= (~state19) & t15;
                state19 ^= (~t15) & t16;
                t19 = state21 ^ t3;
                state20 = (t13 = (t13 << 62) | (t13 >>> -62)) ^ ((~(t18 = (t18 << 55) | (t18 >>> -55))) & (state22 = (t14 << 39) | (t14 >>> -39)));
                state21 = t18 ^ ((~state22) & (state23 = (t12 << 41) | (t12 >>> -41)));
                state22 ^= (~state23) & (state24 = (t19 << 2) | (t19 >>> -2));
                state23 ^= (~state24) & t13;
                state24 ^= (~t13) & t18;
            }
            state[0] = state0;
            state[1] = state1;
            state[2] = state2;
            state[3] = state3;
            state[4] = state4;
            state[5] = state5;
            state[6] = state6;
            state[7] = state7;
            state[8] = state8;
            state[9] = state9;
            state[10] = state10;
            state[11] = state11;
            state[12] = state12;
            state[13] = state13;
            state[14] = state14;
            state[15] = state15;
            state[16] = state16;
            state[17] = state17;
            state[18] = state18;
            state[19] = state19;
            state[20] = state20;
            state[21] = state21;
            state[22] = state22;
            state[23] = state23;
            state[24] = state24;
        }

        for (int i = 0; i < 32; i++) {
            output[i] = (byte)(state[i >> 3] >> ((i & 7) << 3));
        }
        return output;
    }

}
