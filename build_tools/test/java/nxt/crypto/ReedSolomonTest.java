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

import nxt.crypto.ReedSolomon.DecodeException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ReedSolomonTest {

    private Object[][] testAccounts = {
            {8264278205416377583L, "K59H-9RMF-64CY-9X6E7"},
            {8301188658053077183L, "4Q7Z-5BEE-F5JZ-9ZXE8"},
            {1798923958688893959L, "GM29-TWRT-M5CK-3HSXK"},
            {6899983965971136120L, "MHMS-VHZT-W5CY-7CFJZ"},
            {1629938923029941274L, "JM2U-U4AE-G7WF-3NP9F"},
            {6474206656034063375L, "4K2H-NVHQ-7WXY-72AQM"},
            {1691406066100673814L, "Y9AQ-VE8F-U9SY-3NAYG"},
            {2992669254877342352L, "6UNJ-UMFM-Z525-4S24M"},
            {43918951749449909L, "XY7P-3R8Y-26FC-2A293"},
            {9129355674909631300L, "YSU6-MRRL-NSC4-9WHEX"},
            {0L, "2222-2222-2222-22222"},
            {1L, "2223-2222-KB8Y-22222"},
            {10L, "222C-2222-VJTL-22222"},
            {100L, "2256-2222-QFKF-22222"},
            {1000L, "22ZA-2222-ZK43-22222"},
            {10000L, "2BSJ-2222-KC3Y-22222"},
            {100000L, "53P2-2222-SQQW-22222"},
            {1000000L, "YJL2-2222-ZZPC-22222"},
            {10000000L, "K7N2-222B-FVFG-22222"},
            {100000000L, "DSA2-224Z-849U-22222"},
            {1000000000L, "PLJ2-22XT-DVNG-22222"},
            {10000000000L, "RT22-2BC2-SMPD-22222"},
            {100000000000L, "FU22-4X69-74VX-22222"},
            {1000000000000L, "C622-X5CC-EMM8-22222"},
            {10000000000000L, "7A22-5399-RNFK-2B222"},
            {100000000000000L, "NJ22-YEA9-KWDV-2U422"},
            {1000000000000000L, "F222-HULE-NWMS-2FW22"},
            {10000000000000000L, "4222-YBRW-T4XW-28WA2"},
            {100000000000000000L, "N222-H3GS-QPZD-27US4"},
            {1000000000000000000L, "A222-QGMQ-WDH2-2Q7SV"}
    };

    @Test
    public void testSamples() {

        for (Object[] testAccount : testAccounts) {
            assertEquals(testAccount[1], ReedSolomon.encode((Long) testAccount[0]));
            try {
                assertEquals(testAccount[0], ReedSolomon.decode((String) testAccount[1]));
            } catch (DecodeException e) {
                fail(e.toString());
            }
        }
    }

}
