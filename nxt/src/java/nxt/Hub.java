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
import nxt.db.DbKey;
import nxt.db.VersionedEntityDbTable;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Hub {

    public static class Hit implements Comparable<Hit> {

        public final Hub hub;
        public final long hitTime;

        private Hit(Hub hub, long hitTime) {
            this.hub = hub;
            this.hitTime = hitTime;
        }

        @Override
        public int compareTo(Hit hit) {
            if (this.hitTime < hit.hitTime) {
                return -1;
            } else if (this.hitTime > hit.hitTime) {
                return 1;
            } else {
                return Long.compare(this.hub.accountId, hit.hub.accountId);
            }
        }

    }

    private static final DbKey.LongKeyFactory<Hub> hubDbKeyFactory = null;

    private static final VersionedEntityDbTable<Hub> hubTable = null;

    static void addOrUpdateHub(Transaction transaction, Attachment.MessagingHubAnnouncement attachment) {
        hubTable.insert(new Hub(transaction, attachment));
    }

    private static long lastBlockId;
    private static List<Hit> lastHits;

    public static List<Hit> getHubHits(Block block) {

        synchronized (Hub.class) {
            if (block.getId() == lastBlockId && lastHits != null) {
                return lastHits;
            }
            List<Hit> currentHits = new ArrayList<>();
            long currentLastBlockId;

            BlockchainImpl.getInstance().readLock();
            try {
                currentLastBlockId = BlockchainImpl.getInstance().getLastBlock().getId();
                if (currentLastBlockId != block.getId()) {
                    return Collections.emptyList();
                }
                try (DbIterator<Hub> hubs = hubTable.getAll(0, -1)) {
                    while (hubs.hasNext()) {
                        Hub hub = hubs.next();
                        Account account = Account.getAccount(hub.getAccountId());
                        if (account != null) {
                            long effectiveBalance = account.getEffectiveBalanceNXT(block.getHeight());
                            if (effectiveBalance >= Constants.MIN_HUB_EFFECTIVE_BALANCE) {
                                currentHits.add(new Hit(hub, Generator.getHitTime(BigInteger.valueOf(effectiveBalance),
                                        Generator.getHit(Account.getPublicKey(hub.getAccountId()), block), block)));
                            }
                        }
                    }
                }
            } finally {
                BlockchainImpl.getInstance().readUnlock();
            }

            Collections.sort(currentHits);
            lastHits = currentHits;
            lastBlockId = currentLastBlockId;
        }
        return lastHits;

    }

    static void init() {}


    private final long accountId;
    private final DbKey dbKey;
    private final long minFeePerByteNQT;
    private final List<String> uris;

    private Hub(Transaction transaction, Attachment.MessagingHubAnnouncement attachment) {
        this.accountId = transaction.getSenderId();
        this.dbKey = hubDbKeyFactory.newKey(this.accountId);
        this.minFeePerByteNQT = attachment.getMinFeePerByteNQT();
        this.uris = Collections.unmodifiableList(Arrays.asList(attachment.getUris()));
    }

    private Hub(ResultSet rs) throws SQLException {
        this.accountId = rs.getLong("account_id");
        this.dbKey = hubDbKeyFactory.newKey(this.accountId);
        this.minFeePerByteNQT = rs.getLong("min_fee_per_byte");
        this.uris = Collections.unmodifiableList(Arrays.asList((String[])rs.getObject("uris")));
    }

    private void save(Connection con) throws SQLException {
        try (PreparedStatement pstmt = con.prepareStatement("MERGE INTO hub (account_id, min_fee_per_byte, "
                + "uris, height, latest) KEY (account_id, height) VALUES (?, ?, ?, ?, TRUE)")) {
            int i = 0;
            pstmt.setLong(++i, this.getAccountId());
            pstmt.setLong(++i, this.getMinFeePerByteNQT());
            pstmt.setObject(++i, this.getUris().toArray(new String[this.getUris().size()]));
            pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
            pstmt.executeUpdate();
        }
    }

    public long getAccountId() {
        return accountId;
    }

    public long getMinFeePerByteNQT() {
        return minFeePerByteNQT;
    }

    public List<String> getUris() {
        return uris;
    }

}
