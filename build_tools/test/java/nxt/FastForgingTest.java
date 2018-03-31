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

package nxt;

import org.junit.Test;

import java.util.Properties;

public class FastForgingTest extends AbstractForgingTest {

    @Test
    public void fastForgingTest() {
        Properties properties = FastForgingTest.newTestProperties();
        properties.setProperty("nxt.disableGenerateBlocksThread", "false");
        properties.setProperty("nxt.enableFakeForging", "false");
        properties.setProperty("nxt.timeMultiplier", "1000");
        AbstractForgingTest.init(properties);
        forgeTo(startHeight + 10, testForgingSecretPhrase);
        AbstractForgingTest.shutdown();
    }

}
