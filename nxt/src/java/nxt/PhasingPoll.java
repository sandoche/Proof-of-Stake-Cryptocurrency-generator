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

import nxt.crypto.HashFunction;
import nxt.db.DbClause;
import nxt.db.DbIterator;
import nxt.db.DbKey;
import nxt.db.DbUtils;
import nxt.db.EntityDbTable;
import nxt.db.ValuesDbTable;
import nxt.util.Convert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class PhasingPoll extends AbstractPoll {

    public static final Set<HashFunction> acceptedHashFunctions =
            Collections.unmodifiableSet(EnumSet.of(HashFunction.SHA256, HashFunction.RIPEMD160, HashFunction.RIPEMD160_SHA256));

    public static HashFunction getHashFunction(byte code) {
        try {
            HashFunction hashFunction = HashFunction.getHashFunction(code);
            if (acceptedHashFunctions.contains(hashFunction)) {
                return hashFunction;
            }
        } catch (IllegalArgumentException ignore) {}
        return null;
    }

    public static final class PhasingPollResult {

        private final long id;
        private final DbKey dbKey;
        private final long result;
        private final boolean approved;
        private final int height;

        private PhasingPollResult(PhasingPoll poll, long result) {
            this.id = poll.getId();
            this.dbKey = resultDbKeyFactory.newKey(this.id);
            this.result = result;
            this.approved = result >= poll.getQuorum();
            this.height = Nxt.getBlockchain().getHeight();
        }

        private PhasingPollResult(ResultSet rs, DbKey dbKey) throws SQLException {
            this.id = rs.getLong("id");
            this.dbKey = dbKey;
            this.result = rs.getLong("result");
            this.approved = rs.getBoolean("approved");
            this.height = rs.getInt("height");
        }

        private void save(Connection con) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO phasing_poll_result (id, "
                    + "result, approved, height) VALUES (?, ?, ?, ?)")) {
                int i = 0;
                pstmt.setLong(++i, id);
                pstmt.setLong(++i, result);
                pstmt.setBoolean(++i, approved);
                pstmt.setInt(++i, height);
                pstmt.executeUpdate();
            }
        }

        public long getId() {
            return id;
        }

        public long getResult() {
            return result;
        }

        public boolean isApproved() {
            return approved;
        }

        public int getHeight() {
            return height;
        }
    }

    private static final DbKey.LongKeyFactory<PhasingPoll> phasingPollDbKeyFactory = new DbKey.LongKeyFactory<PhasingPoll>("id") {
        @Override
        public DbKey newKey(PhasingPoll poll) {
            return poll.dbKey;
        }
    };

    private static final EntityDbTable<PhasingPoll> phasingPollTable = new EntityDbTable<PhasingPoll>("phasing_poll", phasingPollDbKeyFactory) {

        @Override
        protected PhasingPoll load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
            return new PhasingPoll(rs, dbKey);
        }

        @Override
        protected void save(Connection con, PhasingPoll poll) throws SQLException {
            poll.save(con);
        }

        @Override
        public void trim(int height) {
            super.trim(height);
            try (Connection con = Db.db.getConnection();
                 DbIterator<PhasingPoll> pollsToTrim = phasingPollTable.getManyBy(new DbClause.IntClause("finish_height", DbClause.Op.LT, height), 0, -1);
                 PreparedStatement pstmt1 = con.prepareStatement("DELETE FROM phasing_poll WHERE id = ?");
                 PreparedStatement pstmt2 = con.prepareStatement("DELETE FROM phasing_poll_voter WHERE transaction_id = ?");
                 PreparedStatement pstmt3 = con.prepareStatement("DELETE FROM phasing_vote WHERE transaction_id = ?");
                 PreparedStatement pstmt4 = con.prepareStatement("DELETE FROM phasing_poll_linked_transaction WHERE transaction_id = ?")) {
                while (pollsToTrim.hasNext()) {
                    long id = pollsToTrim.next().getId();
                    pstmt1.setLong(1, id);
                    pstmt1.executeUpdate();
                    pstmt2.setLong(1, id);
                    pstmt2.executeUpdate();
                    pstmt3.setLong(1, id);
                    pstmt3.executeUpdate();
                    pstmt4.setLong(1, id);
                    pstmt4.executeUpdate();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e.toString(), e);
            }
        }
    };

    private static final DbKey.LongKeyFactory<PhasingPoll> votersDbKeyFactory = new DbKey.LongKeyFactory<PhasingPoll>("transaction_id") {
        @Override
        public DbKey newKey(PhasingPoll poll) {
            return poll.dbKey == null ? newKey(poll.id) : poll.dbKey;
        }
    };

    private static final ValuesDbTable<PhasingPoll, Long> votersTable = new ValuesDbTable<PhasingPoll, Long>("phasing_poll_voter", votersDbKeyFactory) {

        @Override
        protected Long load(Connection con, ResultSet rs) throws SQLException {
            return rs.getLong("voter_id");
        }

        @Override
        protected void save(Connection con, PhasingPoll poll, Long accountId) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO phasing_poll_voter (transaction_id, "
                    + "voter_id, height) VALUES (?, ?, ?)")) {
                int i = 0;
                pstmt.setLong(++i, poll.getId());
                pstmt.setLong(++i, accountId);
                pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
                pstmt.executeUpdate();
            }
        }
    };

    private static final DbKey.LongKeyFactory<PhasingPoll> linkedTransactionDbKeyFactory = new DbKey.LongKeyFactory<PhasingPoll>("transaction_id") {
        @Override
        public DbKey newKey(PhasingPoll poll) {
            return poll.dbKey == null ? newKey(poll.id) : poll.dbKey;
        }
    };

    private static final ValuesDbTable<PhasingPoll, byte[]> linkedTransactionTable = new ValuesDbTable<PhasingPoll, byte[]>("phasing_poll_linked_transaction",
            linkedTransactionDbKeyFactory) {

        @Override
        protected byte[] load(Connection con, ResultSet rs) throws SQLException {
            return rs.getBytes("linked_full_hash");
        }

        @Override
        protected void save(Connection con, PhasingPoll poll, byte[] linkedFullHash) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO phasing_poll_linked_transaction (transaction_id, "
                    + "linked_full_hash, linked_transaction_id, height) VALUES (?, ?, ?, ?)")) {
                int i = 0;
                pstmt.setLong(++i, poll.getId());
                pstmt.setBytes(++i, linkedFullHash);
                pstmt.setLong(++i, Convert.fullHashToId(linkedFullHash));
                pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
                pstmt.executeUpdate();
            }
        }
    };

    private static final DbKey.LongKeyFactory<PhasingPollResult> resultDbKeyFactory = new DbKey.LongKeyFactory<PhasingPollResult>("id") {
        @Override
        public DbKey newKey(PhasingPollResult phasingPollResult) {
            return phasingPollResult.dbKey;
        }
    };

    private static final EntityDbTable<PhasingPollResult> resultTable = new EntityDbTable<PhasingPollResult>("phasing_poll_result", resultDbKeyFactory) {

        @Override
        protected PhasingPollResult load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
            return new PhasingPollResult(rs, dbKey);
        }

        @Override
        protected void save(Connection con, PhasingPollResult phasingPollResult) throws SQLException {
            phasingPollResult.save(con);
        }
    };

    public static PhasingPollResult getResult(long id) {
        return resultTable.get(resultDbKeyFactory.newKey(id));
    }

    public static DbIterator<PhasingPollResult> getApproved(int height) {
        return resultTable.getManyBy(new DbClause.IntClause("height", height).and(new DbClause.BooleanClause("approved", true)),
                0, -1, " ORDER BY db_id ASC ");
    }

    public static PhasingPoll getPoll(long id) {
        return phasingPollTable.get(phasingPollDbKeyFactory.newKey(id));
    }

    static DbIterator<TransactionImpl> getFinishingTransactions(int height) {
        Connection con = null;
        try {
            con = Db.db.getConnection();
            PreparedStatement pstmt = con.prepareStatement("SELECT transaction.* FROM transaction, phasing_poll " +
                    "WHERE phasing_poll.id = transaction.id AND phasing_poll.finish_height = ? " +
                    "ORDER BY transaction.height, transaction.transaction_index"); // ASC, not DESC
            pstmt.setInt(1, height);
            return BlockchainImpl.getInstance().getTransactions(con, pstmt);
        } catch (SQLException e) {
            DbUtils.close(con);
            throw new RuntimeException(e.toString(), e);
        }
    }

    public static DbIterator<TransactionImpl> getVoterPhasedTransactions(long voterId, int from, int to) {
        Connection con = null;
        try {
            con = Db.db.getConnection();
            PreparedStatement pstmt = con.prepareStatement("SELECT transaction.* "
                    + "FROM transaction, phasing_poll_voter, phasing_poll "
                    + "LEFT JOIN phasing_poll_result ON phasing_poll.id = phasing_poll_result.id "
                    + "WHERE transaction.id = phasing_poll.id AND "
                    + "phasing_poll.finish_height > ? AND "
                    + "phasing_poll.id = phasing_poll_voter.transaction_id "
                    + "AND phasing_poll_voter.voter_id = ? "
                    + "AND phasing_poll_result.id IS NULL "
                    + "ORDER BY transaction.height DESC, transaction.transaction_index DESC "
                    + DbUtils.limitsClause(from, to));
            int i = 0;
            pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
            pstmt.setLong(++i, voterId);
            DbUtils.setLimits(++i, pstmt, from, to);

            return BlockchainImpl.getInstance().getTransactions(con, pstmt);
        } catch (SQLException e) {
            DbUtils.close(con);
            throw new RuntimeException(e.toString(), e);
        }
    }

    public static DbIterator<TransactionImpl> getHoldingPhasedTransactions(long holdingId, VoteWeighting.VotingModel votingModel,
                                                                           long accountId, boolean withoutWhitelist, int from, int to) {

        Connection con = null;
        try {
            con = Db.db.getConnection();
            PreparedStatement pstmt = con.prepareStatement("SELECT transaction.* " +
                    "FROM transaction, phasing_poll " +
                    "WHERE phasing_poll.holding_id = ? " +
                    "AND phasing_poll.voting_model = ? " +
                    "AND phasing_poll.id = transaction.id " +
                    "AND phasing_poll.finish_height > ? " +
                    (accountId != 0 ? "AND phasing_poll.account_id = ? " : "") +
                    (withoutWhitelist ? "AND phasing_poll.whitelist_size = 0 " : "") +
                    "ORDER BY transaction.height DESC, transaction.transaction_index DESC " +
                    DbUtils.limitsClause(from, to));
            int i = 0;
            pstmt.setLong(++i, holdingId);
            pstmt.setByte(++i, votingModel.getCode());
            pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
            if (accountId != 0) {
                pstmt.setLong(++i, accountId);
            }
            DbUtils.setLimits(++i, pstmt, from, to);

            return BlockchainImpl.getInstance().getTransactions(con, pstmt);
        } catch (SQLException e) {
            DbUtils.close(con);
            throw new RuntimeException(e.toString(), e);
        }
    }

    public static DbIterator<TransactionImpl> getAccountPhasedTransactions(long accountId, int from, int to) {
        Connection con = null;
        try {
            con = Db.db.getConnection();
            PreparedStatement pstmt = con.prepareStatement("SELECT transaction.* FROM transaction, phasing_poll " +
                    " LEFT JOIN phasing_poll_result ON phasing_poll.id = phasing_poll_result.id " +
                    " WHERE phasing_poll.id = transaction.id AND (transaction.sender_id = ? OR transaction.recipient_id = ?) " +
                    " AND phasing_poll_result.id IS NULL " +
                    " AND phasing_poll.finish_height > ? ORDER BY transaction.height DESC, transaction.transaction_index DESC " +
                    DbUtils.limitsClause(from, to));
            int i = 0;
            pstmt.setLong(++i, accountId);
            pstmt.setLong(++i, accountId);
            pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
            DbUtils.setLimits(++i, pstmt, from, to);

            return BlockchainImpl.getInstance().getTransactions(con, pstmt);
        } catch (SQLException e) {
            DbUtils.close(con);
            throw new RuntimeException(e.toString(), e);
        }
    }

    public static int getAccountPhasedTransactionCount(long accountId) {
        try (Connection con = Db.db.getConnection();
             PreparedStatement pstmt = con.prepareStatement("SELECT COUNT(*) FROM transaction, phasing_poll " +
                     " LEFT JOIN phasing_poll_result ON phasing_poll.id = phasing_poll_result.id " +
                     " WHERE phasing_poll.id = transaction.id AND (transaction.sender_id = ? OR transaction.recipient_id = ?) " +
                     " AND phasing_poll_result.id IS NULL " +
                     " AND phasing_poll.finish_height > ?")) {
            int i = 0;
            pstmt.setLong(++i, accountId);
            pstmt.setLong(++i, accountId);
            pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    public static List<? extends Transaction> getLinkedPhasedTransactions(byte[] linkedTransactionFullHash) {
        try (Connection con = Db.db.getConnection();
             PreparedStatement pstmt = con.prepareStatement("SELECT transaction_id FROM phasing_poll_linked_transaction " +
                     "WHERE linked_transaction_id = ? AND linked_full_hash = ?")) {
            int i = 0;
            pstmt.setLong(++i, Convert.fullHashToId(linkedTransactionFullHash));
            pstmt.setBytes(++i, linkedTransactionFullHash);
            List<TransactionImpl> transactions = new ArrayList<>();
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(TransactionDb.findTransaction(rs.getLong("transaction_id")));
                }
            }
            return transactions;
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    static long getSenderPhasedTransactionFees(long accountId) {
        try (Connection con = Db.db.getConnection();
             PreparedStatement pstmt = con.prepareStatement("SELECT SUM(transaction.fee) AS fees FROM transaction, phasing_poll " +
                     " LEFT JOIN phasing_poll_result ON phasing_poll.id = phasing_poll_result.id " +
                     " WHERE phasing_poll.id = transaction.id AND transaction.sender_id = ? " +
                     " AND phasing_poll_result.id IS NULL " +
                     " AND phasing_poll.finish_height > ?")) {
            int i = 0;
            pstmt.setLong(++i, accountId);
            pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
            try (ResultSet rs = pstmt.executeQuery()) {
                rs.next();
                return rs.getLong("fees");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }


    static void addPoll(Transaction transaction, Appendix.Phasing appendix) {
        PhasingPoll poll = new PhasingPoll(transaction, appendix);
        phasingPollTable.insert(poll);
        long[] voters = poll.whitelist;
        if (voters.length > 0) {
            votersTable.insert(poll, Convert.toList(voters));
        }
        if (appendix.getLinkedFullHashes().length > 0) {
            List<byte[]> linkedFullHashes = new ArrayList<>(appendix.getLinkedFullHashes().length);
            Collections.addAll(linkedFullHashes, appendix.getLinkedFullHashes());
            linkedTransactionTable.insert(poll, linkedFullHashes);
        }
    }

    static void init() {
    }

    private final DbKey dbKey;
    private final long[] whitelist;
    private final long quorum;
    private final byte[] hashedSecret;
    private final byte algorithm;

    private PhasingPoll(Transaction transaction, Appendix.Phasing appendix) {
        super(transaction.getId(), transaction.getSenderId(), appendix.getFinishHeight(), appendix.getVoteWeighting());
        this.dbKey = phasingPollDbKeyFactory.newKey(this.id);
        this.quorum = appendix.getQuorum();
        this.whitelist = appendix.getWhitelist();
        this.hashedSecret = appendix.getHashedSecret();
        this.algorithm = appendix.getAlgorithm();
    }

    private PhasingPoll(ResultSet rs, DbKey dbKey) throws SQLException {
        super(rs);
        this.dbKey = dbKey;
        this.quorum = rs.getLong("quorum");
        this.whitelist = rs.getByte("whitelist_size") == 0 ? Convert.EMPTY_LONG : Convert.toArray(votersTable.get(votersDbKeyFactory.newKey(this)));
        hashedSecret = rs.getBytes("hashed_secret");
        algorithm = rs.getByte("algorithm");
    }

    void finish(long result) {
        PhasingPollResult phasingPollResult = new PhasingPollResult(this, result);
        resultTable.insert(phasingPollResult);
    }

    public long[] getWhitelist() {
        return whitelist;
    }

    public long getQuorum() {
        return quorum;
    }

    public byte[] getFullHash() {
        return TransactionDb.getFullHash(this.id);
    }

    public List<byte[]> getLinkedFullHashes() {
        return linkedTransactionTable.get(linkedTransactionDbKeyFactory.newKey(this));
    }

    public byte[] getHashedSecret() {
        return hashedSecret;
    }

    public byte getAlgorithm() {
        return algorithm;
    }

    public boolean verifySecret(byte[] revealedSecret) {
        HashFunction hashFunction = getHashFunction(algorithm);
        return hashFunction != null && Arrays.equals(hashedSecret, hashFunction.hash(revealedSecret));
    }

    public long countVotes() {
        if (voteWeighting.getVotingModel() == VoteWeighting.VotingModel.NONE) {
            return 0;
        }
        int height = Math.min(this.finishHeight, Nxt.getBlockchain().getHeight());
        if (voteWeighting.getVotingModel() == VoteWeighting.VotingModel.TRANSACTION) {
            int count = 0;
            for (byte[] hash : getLinkedFullHashes()) {
                if (TransactionDb.hasTransactionByFullHash(hash, height)) {
                    count += 1;
                }
            }
            return count;
        }
        if (voteWeighting.isBalanceIndependent()) {
            return PhasingVote.getVoteCount(this.id);
        }
        VoteWeighting.VotingModel votingModel = voteWeighting.getVotingModel();
        long cumulativeWeight = 0;
        try (DbIterator<PhasingVote> votes = PhasingVote.getVotes(this.id, 0, Integer.MAX_VALUE)) {
            for (PhasingVote vote : votes) {
                cumulativeWeight += votingModel.calcWeight(voteWeighting, vote.getVoterId(), height);
            }
        }
        return cumulativeWeight;
    }

    boolean allowEarlyFinish() {
        return voteWeighting.isBalanceIndependent() && (whitelist.length > 0 || voteWeighting.getVotingModel() != VoteWeighting.VotingModel.ACCOUNT);
    }

    private void save(Connection con) throws SQLException {
        try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO phasing_poll (id, account_id, "
                + "finish_height, whitelist_size, voting_model, quorum, min_balance, holding_id, "
                + "min_balance_model, hashed_secret, algorithm, height) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            int i = 0;
            pstmt.setLong(++i, id);
            pstmt.setLong(++i, accountId);
            pstmt.setInt(++i, finishHeight);
            pstmt.setByte(++i, (byte) whitelist.length);
            pstmt.setByte(++i, voteWeighting.getVotingModel().getCode());
            DbUtils.setLongZeroToNull(pstmt, ++i, quorum);
            DbUtils.setLongZeroToNull(pstmt, ++i, voteWeighting.getMinBalance());
            DbUtils.setLongZeroToNull(pstmt, ++i, voteWeighting.getHoldingId());
            pstmt.setByte(++i, voteWeighting.getMinBalanceModel().getCode());
            DbUtils.setBytes(pstmt, ++i, hashedSecret);
            pstmt.setByte(++i, algorithm);
            pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
            pstmt.executeUpdate();
        }
    }
}