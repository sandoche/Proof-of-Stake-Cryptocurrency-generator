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

import org.junit.Assert;

import java.util.Properties;

public abstract class AbstractForgingTest extends AbstractBlockchainTest {

    protected static final int minStartHeight = 150000;
    protected static int startHeight;
    protected final static String testForgingSecretPhrase = "aSykrgKGZNlSVOMDxkZZgbTvQqJPGtsBggb";

    protected static Properties newTestProperties() {
        Properties properties = AbstractBlockchainTest.newTestProperties();
        properties.setProperty("nxt.isTestnet", "true");
        properties.setProperty("nxt.isOffline", "true");
        return properties;
    }

    protected static void init(Properties properties) {
        AbstractBlockchainTest.init(properties);
        startHeight = blockchain.getHeight();
        Assert.assertTrue(startHeight >= minStartHeight);
    }

    protected static void shutdown() {
        blockchainProcessor.popOffTo(startHeight);
        AbstractBlockchainTest.shutdown();
    }

}
