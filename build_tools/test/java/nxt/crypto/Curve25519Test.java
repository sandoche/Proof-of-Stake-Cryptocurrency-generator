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

package nxt.crypto;

import nxt.util.Convert;
import org.bouncycastle.util.Arrays;
import org.junit.Test;

import java.security.SecureRandom;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

public class Curve25519Test {

    /** Curve25519 test vectors from NaCl library: Alice's secret */
    private static final String ALICE_SECRET = "77076d0a7318a57d3c16c17251b26645df4c2f87ebc0992ab177fba51db92c2a";

    /** Curve25519 test vectors from NaCl library: Alice's public key */
    private static final String ALICE_PUBLIC = "8520f0098930a754748b7ddcb43ef75a0dbf3a0d26381af4eba4a98eaa9b4e6a";

    /** Curve25519 test vectors from NaCl library: Bob's secret */
    private static final String BOB_SECRET = "5dab087e624a8a4b79e17f8b83800ee66f3bb1292618b6fd1c2f8b27ff88e0eb";

    /** Curve25519 test vectors from NaCl library: Bob's public key */
    private static final String BOB_PUBLIC = "de9edb7d7b7dc1b4d35b61c2ece435373f8343c85b78674dadfc7e146f882b4f";

    @Test
    public void testAliceVector() {
        checkVector(ALICE_SECRET, ALICE_PUBLIC);
    }

    @Test
    public void testBobVector() {
        checkVector(BOB_SECRET, BOB_PUBLIC);
    }
    
    @Test
    public void testCheckVerify() {
        byte[] sig = new byte[32];
        byte[] h = new byte[32];
        byte[] signPriv = new byte[32];
        byte[] pub = new byte[32];
        byte[] secret = Convert.parseHexString(ALICE_SECRET);
        Curve25519.keygen(pub, signPriv, secret);
        new SecureRandom().nextBytes(h);
        Curve25519.sign(sig, h, secret, signPriv);
        
        byte[] v = new byte[32];
        Curve25519.verify(v, sig, h, pub);
        
        assertArrayEquals(v, pub);
    }

    @Test
    public void testAlterSignature() {
        byte[] sig = new byte[32];
        byte[] h = new byte[32];
        byte[] signPriv = new byte[32];
        byte[] pub = new byte[32];
        byte[] secret = Convert.parseHexString(ALICE_SECRET);
        Curve25519.keygen(pub, signPriv, secret);
        new SecureRandom().nextBytes(h);
        Curve25519.sign(sig, h, secret, signPriv);
        sig[0] += 1;
        byte[] v = new byte[32];
        Curve25519.verify(v, sig, h, pub);
        
        if (Arrays.areEqual(v, pub)) {
            fail("Should not verify, as signature is altered");
        }
    }
    
    private void checkVector(String secret, String pub) {
        byte[] secretKey = Convert.parseHexString(secret);
        byte[] publicKey = Convert.parseHexString(pub);
        
        byte[] pubGen = new byte[32];
        byte[] privGen = new byte[32];
        Curve25519.keygen(pubGen, privGen, secretKey);
        
        assertArrayEquals(publicKey, pubGen);
    }
}
