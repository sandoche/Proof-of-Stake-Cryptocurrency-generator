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

import nxt.db.DbClause;
import nxt.db.DbIterator;
import nxt.db.DbKey;
import nxt.db.DbUtils;
import nxt.db.EntityDbTable;
import nxt.db.ValuesDbTable;
import nxt.util.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;

public final class Poll extends AbstractPoll {

    private static final boolean isPollsProcessing = Nxt.getBooleanProperty("nxt.processPolls");

    public static final class OptionResult {

        private long result;
        private long weight;

        private OptionResult(long result, long weight) {
            this.result = result;
            this.weight = weight;
        }

        public long getResult() {
            return result;
        }

        public long getWeight() {
            return weight;
        }

        private void add(long vote, long weight) {
            this.result += vote;
            this.weight += weight;
        }

    }

    private static final DbKey.LongKeyFactory<Poll> pollDbKeyFactory = new DbKey.LongKeyFactory<Poll>("id") {
        @Override
        public DbKey newKey(Poll poll) {
            return poll.dbKey == null ? newKey(poll.id) : poll.dbKey;
        }
    };

    private final static EntityDbTable<Poll> pollTable = new EntityDbTable<Poll>("poll", pollDbKeyFactory, "name,description") {

        @Override
        protected Poll load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
            return new Poll(rs, dbKey);
        }

        @Override
        protected void save(Connection con, Poll poll) throws SQLException {
            poll.save(con);
        }
    };

    private static final DbKey.LongKeyFactory<Poll> pollResultsDbKeyFactory = new DbKey.LongKeyFactory<Poll>("poll_id") {
        @Override
        public DbKey newKey(Poll poll) {
            return poll.dbKey;
        }
    };

    private static final ValuesDbTable<Poll, OptionResult> pollResultsTable = new ValuesDbTable<Poll, OptionResult>("poll_result", pollResultsDbKeyFactory) {

        @Override
        protected OptionResult load(Connection con, ResultSet rs) throws SQLException {
            long weight = rs.getLong("weight");
            return weight == 0 ? null : new OptionResult(rs.getLong("result"), weight);
        }

        @Override
        protected void save(Connection con, Poll poll, OptionResult optionResult) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO poll_result (poll_id, "
                    + "result, weight, height) VALUES (?, ?, ?, ?)")) {
                int i = 0;
                pstmt.setLong(++i, poll.getId());
                if (optionResult != null) {
                    pstmt.setLong(++i, optionResult.result);
                    pstmt.setLong(++i, optionResult.weight);
                } else {
                    pstmt.setNull(++i, Types.BIGINT);
                    pstmt.setLong(++i, 0);
                }
                pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
                pstmt.executeUpdate();
            }
        }
    };

    public static Poll getPoll(long id) {
        return pollTable.get(pollDbKeyFactory.newKey(id));
    }

    public static DbIterator<Poll> getPollsFinishingAtOrBefore(int height, int from, int to) {
        return pollTable.getManyBy(new DbClause.IntClause("finish_height", DbClause.Op.LTE, height), from, to);
    }

    public static DbIterator<Poll> getAllPolls(int from, int to) {
        return pollTable.getAll(from, to);
    }

    public static DbIterator<Poll> getActivePolls(int from, int to) {
        return pollTable.getManyBy(new DbClause.IntClause("finish_height", DbClause.Op.GT, Nxt.getBlockchain().getHeight()), from, to);
    }

    public static DbIterator<Poll> getPollsByAccount(long accountId, boolean includeFinished, boolean finishedOnly, int from, int to) {
        DbClause dbClause = new DbClause.LongClause("account_id", accountId);
        if (finishedOnly) {
            dbClause = dbClause.and(new DbClause.IntClause("finish_height", DbClause.Op.LTE, Nxt.getBlockchain().getHeight()));
        } else if (!includeFinished) {
            dbClause = dbClause.and(new DbClause.IntClause("finish_height", DbClause.Op.GT, Nxt.getBlockchain().getHeight()));
        }
        return pollTable.getManyBy(dbClause, from, to);
    }

    public static DbIterator<Poll> getPollsFinishingAt(int height) {
        return pollTable.getManyBy(new DbClause.IntClause("finish_height", height), 0, Integer.MAX_VALUE);
    }

    public static DbIterator<Poll> searchPolls(String query, boolean includeFinished, int from, int to) {
        DbClause dbClause = includeFinished ? DbClause.EMPTY_CLAUSE : new DbClause.IntClause("finish_height", DbClause.Op.GT, Nxt.getBlockchain().getHeight());
        return pollTable.search(query, dbClause, from, to, " ORDER BY ft.score DESC, poll.height DESC, poll.db_id DESC ");
    }

    public static int getCount() {
        return pollTable.getCount();
    }

    static void addPoll(Transaction transaction, Attachment.MessagingPollCreation attachment) {
        Poll poll = new Poll(transaction, attachment);
        pollTable.insert(poll);
    }

    static void init() {}

    static {
        if (Poll.isPollsProcessing) {
            Nxt.getBlockchainProcessor().addListener(block -> {
                int height = block.getHeight();
                if (height >= Constants.PHASING_BLOCK) {
                    Poll.checkPolls(height);
                }
            }, BlockchainProcessor.Event.AFTER_BLOCK_APPLY);
        }
    }

    private static void checkPolls(int currentHeight) {
        try (DbIterator<Poll> polls = getPollsFinishingAt(currentHeight)) {
            for (Poll poll : polls) {
                try {
                    List<OptionResult> results = poll.countResults(poll.getVoteWeighting(), currentHeight);
                    pollResultsTable.insert(poll, results);
                    Logger.logDebugMessage("Poll " + Long.toUnsignedString(poll.getId()) + " has been finished");
                } catch (RuntimeException e) {
                    Logger.logErrorMessage("Couldn't count votes for poll " + Long.toUnsignedString(poll.getId()));
                }
            }
        }
    }

    private final DbKey dbKey;
    private final String name;
    private final String description;
    private final String[] options;
    private final byte minNumberOfOptions;
    private final byte maxNumberOfOptions;
    private final byte minRangeValue;
    private final byte maxRangeValue;
    private final int timestamp;

    private Poll(Transaction transaction, Attachment.MessagingPollCreation attachment) {
        super(transaction.getId(), transaction.getSenderId(), attachment.getFinishHeight(), attachment.getVoteWeighting());
        this.dbKey = pollDbKeyFactory.newKey(this.id);
        this.name = attachment.getPollName();
        this.description = attachment.getPollDescription();
        this.options = attachment.getPollOptions();
        this.minNumberOfOptions = attachment.getMinNumberOfOptions();
        this.maxNumberOfOptions = attachment.getMaxNumberOfOptions();
        this.minRangeValue = attachment.getMinRangeValue();
        this.maxRangeValue = attachment.getMaxRangeValue();
        this.timestamp = Nxt.getBlockchain().getLastBlockTimestamp();
    }

    private Poll(ResultSet rs, DbKey dbKey) throws SQLException {
        super(rs);
        this.dbKey = dbKey;
        this.name = rs.getString("name");
        this.description = rs.getString("description");
        this.options = DbUtils.getArray(rs, "options", String[].class);
        this.minNumberOfOptions = rs.getByte("min_num_options");
        this.maxNumberOfOptions = rs.getByte("max_num_options");
        this.minRangeValue = rs.getByte("min_range_value");
        this.maxRangeValue = rs.getByte("max_range_value");
        this.timestamp = rs.getInt("timestamp");
    }

    private void save(Connection con) throws SQLException {
        try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO poll (id, account_id, "
                + "name, description, options, finish_height, voting_model, min_balance, min_balance_model, "
                + "holding_id, min_num_options, max_num_options, min_range_value, max_range_value, timestamp, height) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            int i = 0;
            pstmt.setLong(++i, id);
            pstmt.setLong(++i, accountId);
            pstmt.setString(++i, name);
            pstmt.setString(++i, description);
            DbUtils.setArray(pstmt, ++i, options);
            pstmt.setInt(++i, finishHeight);
            pstmt.setByte(++i, voteWeighting.getVotingModel().getCode());
            DbUtils.setLongZeroToNull(pstmt, ++i, voteWeighting.getMinBalance());
            pstmt.setByte(++i, voteWeighting.getMinBalanceModel().getCode());
            DbUtils.setLongZeroToNull(pstmt, ++i, voteWeighting.getHoldingId());
            pstmt.setByte(++i, minNumberOfOptions);
            pstmt.setByte(++i, maxNumberOfOptions);
            pstmt.setByte(++i, minRangeValue);
            pstmt.setByte(++i, maxRangeValue);
            pstmt.setInt(++i, timestamp);
            pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
            pstmt.executeUpdate();
        }
    }

    public List<OptionResult> getResults(VoteWeighting voteWeighting) {
        if (this.voteWeighting.equals(voteWeighting)) {
            return getResults();
        } else {
            return countResults(voteWeighting);
        }

    }

    public List<OptionResult> getResults() {
        if (Poll.isPollsProcessing && isFinished()) {
            return pollResultsTable.get(pollDbKeyFactory.newKey(this));
        } else {
            return countResults(voteWeighting);
        }
    }

    public DbIterator<Vote> getVotes(){
        return Vote.getVotes(this.getId(), 0, -1);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String[] getOptions() {
        return options;
    }


    public byte getMinNumberOfOptions() {
        return minNumberOfOptions;
    }

    public byte getMaxNumberOfOptions() {
        return maxNumberOfOptions;
    }

    public byte getMinRangeValue() {
        return minRangeValue;
    }

    public byte getMaxRangeValue() {
        return maxRangeValue;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public boolean isFinished() {
        return finishHeight <= Nxt.getBlockchain().getHeight();
    }

    private List<OptionResult> countResults(VoteWeighting voteWeighting) {
        int countHeight = Math.min(finishHeight, Nxt.getBlockchain().getHeight());
        if (countHeight < Nxt.getBlockchainProcessor().getMinRollbackHeight()) {
            return null;
        }
        return countResults(voteWeighting, countHeight);
    }

    private List<OptionResult> countResults(VoteWeighting voteWeighting, int height) {
        final OptionResult[] result = new OptionResult[options.length];
        VoteWeighting.VotingModel votingModel = voteWeighting.getVotingModel();
        try (DbIterator<Vote> votes = Vote.getVotes(this.getId(), 0, -1)) {
            for (Vote vote : votes) {
                long weight = votingModel.calcWeight(voteWeighting, vote.getVoterId(), height);
                if (weight <= 0) {
                    continue;
                }
                long[] partialResult = countVote(vote, weight);
                for (int i = 0; i < partialResult.length; i++) {
                    if (partialResult[i] != Long.MIN_VALUE) {
                        if (result[i] == null) {
                            result[i] = new OptionResult(partialResult[i], weight);
                        } else {
                            result[i].add(partialResult[i], weight);
                        }
                    }
                }
            }
        }
        return Arrays.asList(result);
    }

    private long[] countVote(Vote vote, long weight) {
        final long[] partialResult = new long[options.length];
        final byte[] optionValues = vote.getVoteBytes();
        for (int i = 0; i < optionValues.length; i++) {
            if (optionValues[i] != Constants.NO_VOTE_VALUE) {
                partialResult[i] = (long) optionValues[i] * weight;
            } else {
                partialResult[i] = Long.MIN_VALUE;
            }
        }
        return partialResult;
    }

}