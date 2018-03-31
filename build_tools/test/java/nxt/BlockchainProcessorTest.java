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

import nxt.db.DbIterator;
import nxt.util.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlockchainProcessorTest extends AbstractBlockchainTest {

    private static final String defaultTraceFile = "nxt-trace-default.csv";
    private static final String testTraceFile = "nxt-trace.csv";
    private static final int maxHeight = Constants.LAST_KNOWN_BLOCK;
    private static final int startHeight = 0;

    private static final long[] testLesseeAccounts = new long[]{1460178482, -318308835203526404L, 3312398282095696184L, 6373452498729869295L,
            1088641461782019913L, -7984504957518839920L, 814976497827634325L};
    private static final long[] testAssets = new long[]{6775372232354238105L, 3061160746493230502L, -5981557335608550881L, 4551058913252105307L,
            -318057271556719590L, -2234297255166670436L};

    private static DebugTrace debugTrace;

    @BeforeClass
    public static void init() {
        AbstractBlockchainTest.init(newTestProperties());
        debugTrace = DebugTrace.addDebugTrace(Collections.<Long>emptySet(), BlockchainProcessorTest.testTraceFile);
    }

    @AfterClass
    public static void shutdown() {
        AbstractBlockchainTest.shutdown();
    }

    public void reset(int height) {
        debugTrace.resetLog();
        if (blockchain.getHeight() > height) {
            blockchainProcessor.popOffTo(height);
            Assert.assertEquals(height, blockchain.getHeight());
        }
        Assert.assertTrue(blockchain.getHeight() <= height);
    }

    @Test
    public void fullDownloadAndRescanTest() {
        reset(startHeight);
        download(startHeight, maxHeight);
        blockchainProcessor.scan(0, true);
        Assert.assertEquals(maxHeight, blockchain.getHeight());
        Logger.logMessage("Successfully rescanned blockchain from 0 to " + maxHeight);
        compareTraceFiles();
        debugTrace.resetLog();
    }

    @Test
    public void multipleRescanTest() {
        reset(startHeight);
        int start = startHeight;
        int end;
        downloadTo(start);
        while ((end = start + 2000) <= maxHeight) {
            download(start, end);
            rescan(500);
            rescan(900);
            rescan(720);
            rescan(1439);
            rescan(200);
            rescan(1);
            rescan(2);
            start = end;
        }
    }

    @Test
    public void multiplePopOffTest() {
        reset(startHeight);
        int start = startHeight;
        int end;
        downloadTo(start);
        while ((end = start + 2000) <= maxHeight) {
            download(start, end);
            redownload(800, false);
            redownload(1440, false);
            redownload(720, false);
            redownload(1, false);
            start = end;
        }
    }

    @Test
    public void reprocessTransactionsTest() {
        int start = Constants.LAST_KNOWN_BLOCK - 2000;
        reset(start);
        int end;
        downloadTo(start);
        while (blockchain.getLastBlock().getTimestamp() < Nxt.getEpochTime() - 7200) {
            end = start + 100;
            download(start, end);
            redownload(100, true);
            redownload(800, true);
            redownload(1440, true);
            redownload(2, true);
            redownload(1024, true);
            redownload(10, true);
            redownload(720, true);
            redownload(1, true);
            start = end;
        }
    }

    private static void download(final int startHeight, final int endHeight) {
        Assert.assertEquals(startHeight, blockchain.getHeight());
        downloadTo(endHeight);
        Logger.logMessage("Successfully downloaded blockchain from " + startHeight + " to " + endHeight);
        compareTraceFiles();
        debugTrace.resetLog();
    }

    private static void rescan(final int numBlocks) {
        if (numBlocks > Constants.MAX_ROLLBACK) {
            return;
        }
        int endHeight = blockchain.getHeight();
        int rescanHeight = endHeight - numBlocks;
        blockchainProcessor.scan(rescanHeight, true);
        Assert.assertEquals(endHeight, blockchain.getHeight());
        Logger.logMessage("Successfully rescanned blockchain from " + rescanHeight + " to " + endHeight);
        compareTraceFiles();
        debugTrace.resetLog();
    }

    private static void redownload(final int numBlocks, boolean preserveTransactions) {
        if (numBlocks > Constants.MAX_ROLLBACK) {
            return;
        }
        int endHeight = blockchain.getHeight();
        List<List<Long>> allLessorsBefore = new ArrayList<>();
        List<List<Long>> allLessorBalancesBefore = new ArrayList<>();
        for (long accountId : testLesseeAccounts) {
            List<Long> lessors = new ArrayList<>();
            List<Long> balances = new ArrayList<>();
            allLessorsBefore.add(lessors);
            allLessorBalancesBefore.add(balances);
            Account account = Account.getAccount(accountId);
            if (account == null) {
                continue;
            }
            try (DbIterator<Account> iter = account.getLessors(endHeight - numBlocks)) {
                for (Account lessor : iter) {
                    lessors.add(lessor.getId());
                    balances.add(lessor.getGuaranteedBalanceNQT(Constants.GUARANTEED_BALANCE_CONFIRMATIONS, endHeight - numBlocks));
                }
            }
        }
        List<List<TestAccountAsset>> allAccountAssetsBefore = new ArrayList<>();
        for (long assetId : testAssets) {
            List<TestAccountAsset> accountAssets = new ArrayList<>();
            allAccountAssetsBefore.add(accountAssets);
            Asset asset = Asset.getAsset(assetId);
            if (asset == null) {
                continue;
            }
            try (DbIterator<Account.AccountAsset> iter = asset.getAccounts(endHeight - numBlocks, 0, -1)) {
                for (Account.AccountAsset accountAsset : iter) {
                    accountAssets.add(new TestAccountAsset(accountAsset));
                }
            }
        }
        List<BlockImpl> poppedBlocks = blockchainProcessor.popOffTo(endHeight - numBlocks);
        if (preserveTransactions) {
            for (BlockImpl block : poppedBlocks) {
                TransactionProcessorImpl.getInstance().processLater(block.getTransactions());
            }
        }
        Assert.assertEquals(endHeight - numBlocks, blockchain.getHeight());
        List<List<Long>> allLessorsAfter = new ArrayList<>();
        List<List<Long>> allLessorBalancesAfter = new ArrayList<>();
        for (long accountId : testLesseeAccounts) {
            List<Long> lessors = new ArrayList<>();
            List<Long> balances = new ArrayList<>();
            allLessorsAfter.add(lessors);
            allLessorBalancesAfter.add(balances);
            Account account = Account.getAccount(accountId);
            if (account == null) {
                continue;
            }
            try (DbIterator<Account> iter = account.getLessors()) {
                for (Account lessor : iter) {
                    lessors.add(lessor.getId());
                    balances.add(lessor.getGuaranteedBalanceNQT());
                }
            }
        }
        Assert.assertEquals(allLessorsBefore, allLessorsAfter);
        Assert.assertEquals(allLessorBalancesBefore, allLessorBalancesAfter);
        List<List<TestAccountAsset>> allAccountAssetsAfter = new ArrayList<>();
        for (long assetId : testAssets) {
            List<TestAccountAsset> accountAssets = new ArrayList<>();
            allAccountAssetsAfter.add(accountAssets);
            Asset asset = Asset.getAsset(assetId);
            if (asset == null) {
                continue;
            }
            try (DbIterator<Account.AccountAsset> iter = asset.getAccounts(0, -1)) {
                for (Account.AccountAsset accountAsset : iter) {
                    accountAssets.add(new TestAccountAsset(accountAsset));
                }
            }
        }
        Assert.assertEquals(allAccountAssetsBefore, allAccountAssetsAfter);
        //Logger.logDebugMessage("Assets Before: " + allAccountAssetsBefore);
        //Logger.logDebugMessage("Assets After: " + allAccountAssetsAfter);
        downloadTo(endHeight);
        Logger.logMessage("Successfully redownloaded blockchain from " + (endHeight - numBlocks) + " to " + endHeight);
        compareTraceFiles();
        debugTrace.resetLog();
    }

    private static void compareTraceFiles() {
        try (BufferedReader defaultReader = new BufferedReader(new FileReader(defaultTraceFile));
             BufferedReader testReader = new BufferedReader(new FileReader(testTraceFile))) {
            defaultReader.readLine();
            testReader.readLine();
            String testLine = testReader.readLine();
            if (testLine == null) {
                Logger.logMessage("Empty trace file, nothing to compare");
                return;
            }
            int height = parseHeight(testLine);
            String defaultLine;
            while ((defaultLine = defaultReader.readLine()) != null) {
                if (parseHeight(defaultLine) >= height) {
                    break;
                }
            }
            if (defaultLine == null) {
                Logger.logMessage("End of default trace file, can't compare further");
                return;
            }
            int endHeight = height;
            Assert.assertEquals(defaultLine, testLine);
            while ((testLine = testReader.readLine()) != null) {
                defaultLine = defaultReader.readLine();
                if (defaultLine == null) {
                    Logger.logMessage("End of default trace file, can't compare further");
                    return;
                }
                endHeight = parseHeight(testLine);
                Assert.assertEquals(defaultLine, testLine);
            }
            if ((defaultLine = defaultReader.readLine()) != null) {
                Assert.assertTrue(parseHeight(defaultLine) > endHeight);
            }
            Logger.logMessage("Comparison with default trace file passed from height " + height + " to " + endHeight);
        } catch (IOException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    private static int parseHeight(String line) {
        return Integer.parseInt(line.substring(1, line.indexOf(DebugTrace.SEPARATOR) - 1));
    }

    private static final class TestAccountAsset {

        private final Account.AccountAsset accountAsset;

        private TestAccountAsset(Account.AccountAsset accountAsset) {
            this.accountAsset = accountAsset;
        }

        @Override
        public boolean equals(Object o) {
            if (! (o instanceof TestAccountAsset)) {
                return false;
            }
            Account.AccountAsset other = ((TestAccountAsset)o).accountAsset;
            return this.accountAsset.getAccountId() == other.getAccountId()
                    && this.accountAsset.getAssetId() == other.getAssetId()
                    && this.accountAsset.getQuantityQNT() == other.getQuantityQNT();
        }

        @Override
        public String toString() {
            return accountAsset.toString();
        }

    }
}
