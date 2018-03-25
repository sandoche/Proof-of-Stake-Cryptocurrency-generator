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

import nxt.NxtException;
import nxt.util.Convert;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public final class AnonymouslyEncryptedData {

    public static AnonymouslyEncryptedData encrypt(byte[] plaintext, String secretPhrase, byte[] theirPublicKey, byte[] nonce) {
        byte[] keySeed = Crypto.getKeySeed(secretPhrase, theirPublicKey, nonce);
        byte[] myPrivateKey = Crypto.getPrivateKey(keySeed);
        byte[] myPublicKey = Crypto.getPublicKey(keySeed);
        byte[] sharedKey = Crypto.getSharedKey(myPrivateKey, theirPublicKey);
        byte[] data = Crypto.aesGCMEncrypt(plaintext, sharedKey);
        return new AnonymouslyEncryptedData(data, myPublicKey);
    }

    public static AnonymouslyEncryptedData readEncryptedData(ByteBuffer buffer, int length, int maxLength)
            throws NxtException.NotValidException {
        if (length > maxLength) {
            throw new NxtException.NotValidException("Max encrypted data length exceeded: " + length);
        }
        byte[] data = new byte[length];
        buffer.get(data);
        byte[] publicKey = new byte[32];
        buffer.get(publicKey);
        return new AnonymouslyEncryptedData(data, publicKey);
    }

    public static AnonymouslyEncryptedData readEncryptedData(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        try {
            return readEncryptedData(buffer, bytes.length - 32, Integer.MAX_VALUE);
        } catch (NxtException.NotValidException e) {
            throw new RuntimeException(e.toString(), e); // never
        }
    }

    private final byte[] data;
    private final byte[] publicKey;

    public AnonymouslyEncryptedData(byte[] data, byte[] publicKey) {
        this.data = data;
        this.publicKey = publicKey;
    }

    public byte[] decrypt(String secretPhrase) {
        byte[] sharedKey = Crypto.getSharedKey(Crypto.getPrivateKey(secretPhrase), publicKey);
        return Crypto.aesGCMDecrypt(data, sharedKey);
    }

    public byte[] decrypt(byte[] keySeed, byte[] theirPublicKey) {
        if (!Arrays.equals(Crypto.getPublicKey(keySeed), publicKey)) {
            throw new RuntimeException("Data was not encrypted using this keySeed");
        }
        byte[] sharedKey = Crypto.getSharedKey(Crypto.getPrivateKey(keySeed), theirPublicKey);
        return Crypto.aesGCMDecrypt(data, sharedKey);
    }

    public byte[] getData() {
        return data;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public int getSize() {
        return data.length + 32;
    }

    public byte[] getBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(data.length + 32);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(data);
        buffer.put(publicKey);
        return buffer.array();
    }

    @Override
    public String toString() {
        return "data: " + Convert.toHexString(data) + " publicKey: " + Convert.toHexString(publicKey);
    }

}
