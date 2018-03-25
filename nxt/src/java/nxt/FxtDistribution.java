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

package nxt;

import nxt.db.DbIterator;
import nxt.db.DerivedDbTable;
import nxt.util.Convert;
import nxt.util.JSON;
import nxt.util.Listener;
import nxt.util.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public final class FxtDistribution implements Listener<Block> {

    public static final int DISTRIBUTION_END = Constants.FXT_BLOCK;
    public static final int DISTRIBUTION_START = DISTRIBUTION_END - 90 * 1440; // run for 90 days
    public static final int DISTRIBUTION_FREQUENCY = 720; // run processing every 720 blocks
    public static final int DISTRIBUTION_STEP = 60; // take snapshots every 60 blocks
    public static final long FXT_ASSET_ID = Long.parseUnsignedLong(Constants.isTestnet ? "861080501219231688" : "12422608354438203866");
    public static final long FXT_ISSUER_ID = Convert.parseAccountId(Constants.isTestnet ? "NXT-F8FG-RDWZ-GRW7-4GSK9" : "NXT-FQ28-G9SQ-BG8M-6V6QH");
    private static final BigInteger BALANCE_DIVIDER = BigInteger.valueOf(10000L * (DISTRIBUTION_END - DISTRIBUTION_START) / DISTRIBUTION_STEP);
    private static final String logAccount = Nxt.getStringProperty("nxt.logFxtBalance");
    private static final long logAccountId = Convert.parseAccountId(logAccount);
    private static final String fxtJsonFile = Constants.isTestnet ? "fxt-testnet.json" : "fxt.json";
    private static final boolean hasSnapshot = ClassLoader.getSystemResource(fxtJsonFile) != null;

    public static final long BITSWIFT_ASSET_ID = Long.parseUnsignedLong("12034575542068240440");
    public static final long BITSWIFT_SHAREDROP_ACCOUNT = Convert.parseAccountId("NXT-2HKA-GTP2-ZBFL-34B9L");
    public static final long JANUS_ASSET_ID = Long.parseUnsignedLong("4348103880042995903");
    public static final long JANUSXT_ASSET_ID = Long.parseUnsignedLong("14572747084550678873");
    public static final long COMJNSXT_ASSET_ID = Long.parseUnsignedLong("13363533560620557665");
    public static final Set<Long> ardorSnapshotAssets = Collections.unmodifiableSet(
            Convert.toSet(new long[] {FXT_ASSET_ID, BITSWIFT_ASSET_ID, JANUS_ASSET_ID, JANUSXT_ASSET_ID, COMJNSXT_ASSET_ID}));

    private static final DerivedDbTable accountFXTTable = new DerivedDbTable("account_fxt") {
        @Override
        public void trim(int height) {
            try (Connection con = db.getConnection()) {
                if (height > DISTRIBUTION_END) {
                    try (Statement stmt = con.createStatement()) {
                        stmt.executeUpdate("TRUNCATE TABLE account_fxt");
                    }
                } else {
                    try (PreparedStatement pstmtCreate = con.prepareStatement("CREATE TEMP TABLE account_fxt_tmp NOT PERSISTENT AS "
                            + "SELECT id, MAX(height) AS height FROM account_fxt WHERE height < ? GROUP BY id");
                         PreparedStatement pstmtDrop = con.prepareStatement("DROP TABLE account_fxt_tmp")) {
                        pstmtCreate.setInt(1, height);
                        pstmtCreate.executeUpdate();
                        try (PreparedStatement pstmt = con.prepareStatement("DELETE FROM account_fxt WHERE (id, height) NOT IN "
                                + "(SELECT (id, height) FROM account_fxt_tmp) AND height < ? AND height >= 0")) {
                            pstmt.setInt(1, height);
                            pstmt.executeUpdate();
                        } finally {
                            pstmtDrop.executeUpdate();
                        }
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e.toString(), e);
            }
        }
    };

    static void init() {}

    static {
        Nxt.getBlockchainProcessor().addListener(new FxtDistribution(), BlockchainProcessor.Event.AFTER_BLOCK_ACCEPT);
    }

    @Override
    public void notify(Block block) {

        final int currentHeight = block.getHeight();
        if (currentHeight == Constants.IGNIS_BLOCK) {
            boolean wasInTransaction = Db.db.isInTransaction();
            if (!wasInTransaction) {
                Db.db.beginTransaction();
            }
            try {
                for (long assetId : ardorSnapshotAssets) {
                    int count = 0;
                    try (DbIterator<Order.Ask> askOrders = Order.Ask.getAskOrdersByAsset(assetId, 0, -1)) {
                        while (askOrders.hasNext()) {
                            Order order = askOrders.next();
                            Order.Ask.removeOrder(order.getId());
                            Account.getAccount(order.getAccountId()).addToUnconfirmedAssetBalanceQNT(null, 0, assetId, order.getQuantityQNT());
                            if (++count % 1000 == 0) {
                                Db.db.commitTransaction();
                            }
                        }
                    }
                    Logger.logDebugMessage("Deleted " + count + " ask orders for asset " + Long.toUnsignedString(assetId));
                    count = 0;
                    try (DbIterator<Order.Bid> bidOrders = Order.Bid.getBidOrdersByAsset(assetId, 0, -1)) {
                        while (bidOrders.hasNext()) {
                            Order order = bidOrders.next();
                            Order.Bid.removeOrder(order.getId());
                            Account.getAccount(order.getAccountId()).addToUnconfirmedBalanceNQT(null, 0, Math.multiplyExact(order.getQuantityQNT(), order.getPriceNQT()));
                            if (++count % 1000 == 0) {
                                Db.db.commitTransaction();
                            }
                        }
                    }
                    Logger.logDebugMessage("Deleted " + count + " bid orders for asset " + Long.toUnsignedString(assetId));
                }
                Account issuerAccount = Account.getAccount(FXT_ISSUER_ID);
                AccountRestrictions.PhasingOnly.unset(issuerAccount);
                Db.db.commitTransaction();
            } catch (Exception e) {
                Db.db.rollbackTransaction();
                throw new RuntimeException(e.toString(), e);
            } finally {
                if (!wasInTransaction) {
                    Db.db.endTransaction();
                }
            }
            return;
        }
        if (hasSnapshot) {
            if (currentHeight == DISTRIBUTION_END) {
                Logger.logDebugMessage("Distributing FXT based on snapshot file " + fxtJsonFile);
                JSONObject snapshotJSON;
                try (Reader reader = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(fxtJsonFile)))) {
                    snapshotJSON = (JSONObject) JSONValue.parse(reader);
                } catch (IOException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
                boolean wasInTransaction = Db.db.isInTransaction();
                if (!wasInTransaction) {
                    Db.db.beginTransaction();
                }
                try {
                    long initialQuantity = Asset.getAsset(FXT_ASSET_ID).getInitialQuantityQNT();
                    Account issuerAccount = Account.getAccount(FXT_ISSUER_ID);
                    issuerAccount.addToAssetAndUnconfirmedAssetBalanceQNT(null, block.getId(),
                            FXT_ASSET_ID, -initialQuantity);
                    long totalDistributed = 0;
                    Iterator<Map.Entry> iterator = snapshotJSON.entrySet().iterator();
                    int count = 0;
                    while (iterator.hasNext()) {
                        Map.Entry entry = iterator.next();
                        long accountId = Long.parseUnsignedLong((String) entry.getKey());
                        long quantity = (Long)entry.getValue();
                        Account.getAccount(accountId).addToAssetAndUnconfirmedAssetBalanceQNT(null, block.getId(),
                                FXT_ASSET_ID, quantity);
                        totalDistributed += quantity;
                        if (++count % 1000 == 0) {
                            Db.db.commitTransaction();
                        }
                    }
                    long excessFxtQuantity = initialQuantity - totalDistributed;
                    Asset.deleteAsset(TransactionDb.findTransaction(FXT_ASSET_ID), FXT_ASSET_ID, excessFxtQuantity);
                    Logger.logDebugMessage("Deleted " + excessFxtQuantity + " excess QNT");
                    Logger.logDebugMessage("Distributed " + totalDistributed + " QNT to " + count + " accounts");
                    Db.db.commitTransaction();
                } catch (Exception e) {
                    Db.db.rollbackTransaction();
                    throw new RuntimeException(e.toString(), e);
                } finally {
                    if (!wasInTransaction) {
                        Db.db.endTransaction();
                    }
                }
            }
            return;
        }
        if (currentHeight <= DISTRIBUTION_START || currentHeight > DISTRIBUTION_END || (currentHeight - DISTRIBUTION_START) % DISTRIBUTION_FREQUENCY != 0) {
            return;
        }
        Logger.logDebugMessage("Running FXT balance update at height " + currentHeight);
        Map<Long, BigInteger> accountBalanceTotals = new HashMap<>();
        for (int height = currentHeight - DISTRIBUTION_FREQUENCY + DISTRIBUTION_STEP; height <= currentHeight; height += DISTRIBUTION_STEP) {
            Logger.logDebugMessage("Calculating balances at height " + height);
            try (Connection con = Db.db.getConnection();
                 PreparedStatement pstmtCreate = con.prepareStatement("CREATE TEMP TABLE account_tmp NOT PERSISTENT AS SELECT id, MAX(height) as height FROM account "
                         + "WHERE height <= ? GROUP BY id")) {
                pstmtCreate.setInt(1, height);
                pstmtCreate.executeUpdate();
                try (PreparedStatement pstmtSelect = con.prepareStatement("SELECT account.id, account.balance FROM account, account_tmp WHERE account.id = account_tmp.id "
                        + "AND account.height = account_tmp.height AND account.balance > 0");
                     PreparedStatement pstmtDrop = con.prepareStatement("DROP TABLE account_tmp")) {
                    try (ResultSet rs = pstmtSelect.executeQuery()) {
                        while (rs.next()) {
                            Long accountId = rs.getLong("id");
                            long balance = rs.getLong("balance");
                            if (logAccountId != 0) {
                                if (accountId == logAccountId) {
                                    Logger.logMessage("NXT balance for " + logAccount + " at height " + height + ":\t" + balance);
                                }
                            }
                            BigInteger accountBalanceTotal = accountBalanceTotals.get(accountId);
                            accountBalanceTotals.put(accountId, accountBalanceTotal == null ?
                                    BigInteger.valueOf(balance) : accountBalanceTotal.add(BigInteger.valueOf(balance)));
                        }
                    } finally {
                        pstmtDrop.executeUpdate();
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e.toString(), e);
            }
        }
        Logger.logDebugMessage("Updating balances for " + accountBalanceTotals.size() + " accounts");
        boolean wasInTransaction = Db.db.isInTransaction();
        if (!wasInTransaction) {
            Db.db.beginTransaction();
        }
        Db.db.clearCache();
        try (Connection con = Db.db.getConnection();
             PreparedStatement pstmtSelect = con.prepareStatement("SELECT balance FROM account_fxt WHERE id = ? ORDER BY height DESC LIMIT 1");
             PreparedStatement pstmtInsert = con.prepareStatement("INSERT INTO account_fxt (id, balance, height) values (?, ?, ?)")) {
            int count = 0;
            for (Map.Entry<Long, BigInteger> entry : accountBalanceTotals.entrySet()) {
                long accountId = entry.getKey();
                BigInteger balanceTotal = entry.getValue();
                pstmtSelect.setLong(1, accountId);
                try (ResultSet rs = pstmtSelect.executeQuery()) {
                    if (rs.next()) {
                        balanceTotal = balanceTotal.add(new BigInteger(rs.getBytes("balance")));
                    }
                }
                if (logAccountId != 0) {
                    if (accountId == logAccountId) {
                        Logger.logMessage("Average NXT balance for " + logAccount + " as of height " + currentHeight + ":\t"
                                + balanceTotal.divide(BigInteger.valueOf((currentHeight - DISTRIBUTION_START) / DISTRIBUTION_STEP)).longValueExact());
                    }
                }
                pstmtInsert.setLong(1, accountId);
                pstmtInsert.setBytes(2, balanceTotal.toByteArray());
                pstmtInsert.setInt(3, currentHeight);
                pstmtInsert.executeUpdate();
                if (++count % 1000 == 0) {
                    Db.db.commitTransaction();
                }
            }
            accountBalanceTotals.clear();
            Db.db.commitTransaction();
            if (currentHeight == DISTRIBUTION_END) {
                Logger.logDebugMessage("Running FXT distribution at height " + currentHeight);
                long totalDistributed = 0;
                count = 0;
                SortedMap<String, Long> snapshotMap = new TreeMap<>();
                try (PreparedStatement pstmtCreate = con.prepareStatement("CREATE TEMP TABLE account_fxt_tmp NOT PERSISTENT AS SELECT id, MAX(height) AS height FROM account_fxt "
                        + "WHERE height <= ? GROUP BY id");
                     PreparedStatement pstmtDrop = con.prepareStatement("DROP TABLE account_fxt_tmp")) {
                    pstmtCreate.setInt(1, currentHeight);
                    pstmtCreate.executeUpdate();
                    try (PreparedStatement pstmt = con.prepareStatement("SELECT account_fxt.id, account_fxt.balance FROM account_fxt, account_fxt_tmp "
                            + "WHERE account_fxt.id = account_fxt_tmp.id AND account_fxt.height = account_fxt_tmp.height");
                         ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            long accountId = rs.getLong("id");
                            // 1 NXT held for the full period should give 1 asset unit, i.e. 10000 QNT assuming 4 decimals
                            long quantity = new BigInteger(rs.getBytes("balance")).divide(BALANCE_DIVIDER).longValueExact();
                            if (logAccountId != 0) {
                                if (accountId == logAccountId) {
                                    Logger.logMessage("FXT quantity for " + logAccount + ":\t" + quantity);
                                }
                            }
                            Account.getAccount(accountId).addToAssetAndUnconfirmedAssetBalanceQNT(null, block.getId(),
                                    FXT_ASSET_ID, quantity);
                            snapshotMap.put(Long.toUnsignedString(accountId), quantity);
                            totalDistributed += quantity;
                            if (++count % 1000 == 0) {
                                Db.db.commitTransaction();
                            }
                        }
                    } finally {
                        pstmtDrop.executeUpdate();
                    }
                }
                Account issuerAccount = Account.getAccount(FXT_ISSUER_ID);
                issuerAccount.addToAssetAndUnconfirmedAssetBalanceQNT(null, block.getId(),
                        FXT_ASSET_ID, -totalDistributed);
                long excessFxtQuantity = Asset.getAsset(FXT_ASSET_ID).getInitialQuantityQNT() - totalDistributed;
                issuerAccount.addToAssetAndUnconfirmedAssetBalanceQNT(null, block.getId(),
                        FXT_ASSET_ID, -excessFxtQuantity);
                long issuerAssetBalance = issuerAccount.getAssetBalanceQNT(FXT_ASSET_ID);
                if (issuerAssetBalance > 0) {
                    snapshotMap.put(Long.toUnsignedString(FXT_ISSUER_ID), issuerAssetBalance);
                } else {
                    snapshotMap.remove(Long.toUnsignedString(FXT_ISSUER_ID));
                }
                Asset.deleteAsset(TransactionDb.findTransaction(FXT_ASSET_ID), FXT_ASSET_ID, excessFxtQuantity);
                Logger.logDebugMessage("Deleted " + excessFxtQuantity + " excess QNT");
                Logger.logDebugMessage("Distributed " + totalDistributed + " QNT to " + count + " accounts");
                try (PrintWriter writer = new PrintWriter((new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fxtJsonFile)))), true)) {
                    StringBuilder sb = new StringBuilder(1024);
                    JSON.encodeObject(snapshotMap, sb);
                    writer.write(sb.toString());
                }
                Db.db.commitTransaction();
            }
        } catch (Exception e) {
            Db.db.rollbackTransaction();
            throw new RuntimeException(e.toString(), e);
        } finally {
            if (!wasInTransaction) {
                Db.db.endTransaction();
            }
        }
    }
}
