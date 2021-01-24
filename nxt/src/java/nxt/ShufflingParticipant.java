/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2020 Jelurida IP B.V.
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
import nxt.db.PrunableDbTable;
import nxt.db.VersionedEntityDbTable;
import nxt.util.Convert;
import nxt.util.Listener;
import nxt.util.Listeners;
import nxt.util.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

public final class ShufflingParticipant {

    public enum State {
        REGISTERED((byte)0, new byte[]{1}),
        PROCESSED((byte)1, new byte[]{2,3}),
        VERIFIED((byte)2, new byte[]{3}),
        CANCELLED((byte)3, new byte[]{});

        private final byte code;
        private final byte[] allowedNext;

        State(byte code, byte[] allowedNext) {
            this.code = code;
            this.allowedNext = allowedNext;
        }

        static State get(byte code) {
            for (State state : State.values()) {
                if (state.code == code) {
                    return state;
                }
            }
            throw new IllegalArgumentException("No matching state for " + code);
        }

        public byte getCode() {
            return code;
        }

        public boolean canBecome(State nextState) {
            return Arrays.binarySearch(allowedNext, nextState.code) >= 0;
        }
    }

    public enum Event {
        PARTICIPANT_REGISTERED, PARTICIPANT_PROCESSED, PARTICIPANT_VERIFIED, PARTICIPANT_CANCELLED
    }

    private final static class ShufflingData {

        private final long shufflingId;
        private final long accountId;
        private final DbKey dbKey;
        private final byte[][] data;
        private final int transactionTimestamp;
        private final int height;

        private ShufflingData(long shufflingId, long accountId, byte[][] data, int transactionTimestamp, int height) {
            this.shufflingId = shufflingId;
            this.accountId = accountId;
            this.dbKey = shufflingDataDbKeyFactory.newKey(shufflingId, accountId);
            this.data = data;
            this.transactionTimestamp = transactionTimestamp;
            this.height = height;
        }

        private ShufflingData(ResultSet rs, DbKey dbKey) throws SQLException {
            this.shufflingId = rs.getLong("shuffling_id");
            this.accountId = rs.getLong("account_id");
            this.dbKey = dbKey;
            this.data = DbUtils.getArray(rs, "data", byte[][].class, Convert.EMPTY_BYTES);
            this.transactionTimestamp = rs.getInt("transaction_timestamp");
            this.height = rs.getInt("height");
        }

        private void save(Connection con) throws SQLException {
            try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO shuffling_data (shuffling_id, account_id, data, "
                    + "transaction_timestamp, height) "
                    + "VALUES (?, ?, ?, ?, ?)")) {
                int i = 0;
                pstmt.setLong(++i, this.shufflingId);
                pstmt.setLong(++i, this.accountId);
                DbUtils.setArrayEmptyToNull(pstmt, ++i, this.data);
                pstmt.setInt(++i, this.transactionTimestamp);
                pstmt.setInt(++i, this.height);
                pstmt.executeUpdate();
            }
        }

    }

    private static final Listeners<ShufflingParticipant, Event> listeners = new Listeners<>();

    private static final DbKey.LinkKeyFactory<ShufflingParticipant> shufflingParticipantDbKeyFactory = new DbKey.LinkKeyFactory<ShufflingParticipant>("shuffling_id", "account_id") {

        @Override
        public DbKey newKey(ShufflingParticipant participant) {
            return participant.dbKey;
        }

    };

    private static final VersionedEntityDbTable<ShufflingParticipant> shufflingParticipantTable = new VersionedEntityDbTable<ShufflingParticipant>("shuffling_participant", shufflingParticipantDbKeyFactory) {

        @Override
        protected ShufflingParticipant load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
            return new ShufflingParticipant(rs, dbKey);
        }

        @Override
        protected void save(Connection con, ShufflingParticipant participant) throws SQLException {
            participant.save(con);
        }

    };

    private static final DbKey.LinkKeyFactory<ShufflingData> shufflingDataDbKeyFactory = new DbKey.LinkKeyFactory<ShufflingData>("shuffling_id", "account_id") {

        @Override
        public DbKey newKey(ShufflingData shufflingData) {
            return shufflingData.dbKey;
        }

    };

    private static final PrunableDbTable<ShufflingData> shufflingDataTable = new PrunableDbTable<ShufflingData>("shuffling_data", shufflingDataDbKeyFactory) {

        @Override
        protected ShufflingData load(Connection con, ResultSet rs, DbKey dbKey) throws SQLException {
            return new ShufflingData(rs, dbKey);
        }

        @Override
        protected void save(Connection con, ShufflingData shufflingData) throws SQLException {
            shufflingData.save(con);
        }

    };

    public static boolean addListener(Listener<ShufflingParticipant> listener, Event eventType) {
        return listeners.addListener(listener, eventType);
    }

    public static boolean removeListener(Listener<ShufflingParticipant> listener, Event eventType) {
        return listeners.removeListener(listener, eventType);
    }

    public static DbIterator<ShufflingParticipant> getParticipants(long shufflingId) {
        return shufflingParticipantTable.getManyBy(new DbClause.LongClause("shuffling_id", shufflingId), 0, -1, " ORDER BY participant_index ");
    }

    public static ShufflingParticipant getParticipant(long shufflingId, long accountId) {
        return shufflingParticipantTable.get(shufflingParticipantDbKeyFactory.newKey(shufflingId, accountId));
    }

    static ShufflingParticipant getLastParticipant(long shufflingId) {
        return shufflingParticipantTable.getBy(new DbClause.LongClause("shuffling_id", shufflingId).and(new DbClause.NullClause("next_account_id")));
    }

    static void addParticipant(long shufflingId, long accountId, int index) {
        ShufflingParticipant participant = new ShufflingParticipant(shufflingId, accountId, index);
        shufflingParticipantTable.insert(participant);
        listeners.notify(participant, Event.PARTICIPANT_REGISTERED);
    }

    static int getVerifiedCount(long shufflingId) {
        return shufflingParticipantTable.getCount(new DbClause.LongClause("shuffling_id", shufflingId).and(
                new DbClause.ByteClause("state", State.VERIFIED.getCode())));
    }

    static void init() {}

    private final long shufflingId;
    private final long accountId; // sender account
    private final DbKey dbKey;
    private final int index;

    private long nextAccountId; // pointer to the next shuffling participant updated during registration
    private State state; // tracks the state of the participant in the process
    private byte[][] blameData; // encrypted data saved as intermediate result in the shuffling process
    private byte[][] keySeeds; // to be revealed only if shuffle is being cancelled
    private byte[] dataTransactionFullHash;

    private ShufflingParticipant(long shufflingId, long accountId, int index) {
        this.shufflingId = shufflingId;
        this.accountId = accountId;
        this.dbKey = shufflingParticipantDbKeyFactory.newKey(shufflingId, accountId);
        this.index = index;
        this.state = State.REGISTERED;
        this.blameData = Convert.EMPTY_BYTES;
        this.keySeeds = Convert.EMPTY_BYTES;
    }

    private ShufflingParticipant(ResultSet rs, DbKey dbKey) throws SQLException {
        this.shufflingId = rs.getLong("shuffling_id");
        this.accountId = rs.getLong("account_id");
        this.dbKey = dbKey;
        this.nextAccountId = rs.getLong("next_account_id");
        this.index = rs.getInt("participant_index");
        this.state = State.get(rs.getByte("state"));
        this.blameData = DbUtils.getArray(rs, "blame_data", byte[][].class, Convert.EMPTY_BYTES);
        this.keySeeds = DbUtils.getArray(rs, "key_seeds", byte[][].class, Convert.EMPTY_BYTES);
        this.dataTransactionFullHash = rs.getBytes("data_transaction_full_hash");
    }

    private void save(Connection con) throws SQLException {
        try (PreparedStatement pstmt = con.prepareStatement("MERGE INTO shuffling_participant (shuffling_id, "
                + "account_id, next_account_id, participant_index, state, blame_data, key_seeds, data_transaction_full_hash, height, latest) "
                + "KEY (shuffling_id, account_id, height) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE)")) {
            int i = 0;
            pstmt.setLong(++i, this.shufflingId);
            pstmt.setLong(++i, this.accountId);
            DbUtils.setLongZeroToNull(pstmt, ++i, this.nextAccountId);
            pstmt.setInt(++i, this.index);
            pstmt.setByte(++i, this.getState().getCode());
            DbUtils.setArrayEmptyToNull(pstmt, ++i, this.blameData);
            DbUtils.setArrayEmptyToNull(pstmt, ++i, this.keySeeds);
            DbUtils.setBytes(pstmt, ++i, this.dataTransactionFullHash);
            pstmt.setInt(++i, Nxt.getBlockchain().getHeight());
            pstmt.executeUpdate();
        }
    }

    public long getShufflingId() {
        return shufflingId;
    }

    public long getAccountId() {
        return accountId;
    }

    public long getNextAccountId() {
        return nextAccountId;
    }

    void setNextAccountId(long nextAccountId) {
        if (this.nextAccountId != 0) {
            throw new IllegalStateException("nextAccountId already set to " + Long.toUnsignedString(this.nextAccountId));
        }
        this.nextAccountId = nextAccountId;
        shufflingParticipantTable.insert(this);
    }

    public int getIndex() {
        return index;
    }

    public State getState() {
        return state;
    }

    // caller must update database
    private void setState(State state) {
        if (!this.state.canBecome(state)) {
            throw new IllegalStateException(String.format("Shuffling participant in state %s cannot go to state %s", this.state, state));
        }
        this.state = state;
        Logger.logDebugMessage("Shuffling participant %s changed state to %s", Long.toUnsignedString(accountId), this.state);
    }

    public byte[][] getData() {
        return getData(shufflingId, accountId);
    }

    static byte[][] getData(long shufflingId, long accountId) {
        ShufflingData shufflingData = shufflingDataTable.get(shufflingDataDbKeyFactory.newKey(shufflingId, accountId));
        return shufflingData != null ? shufflingData.data : null;
    }

    void setData(byte[][] data, int timestamp) {
        if (data != null && Nxt.getEpochTime() - timestamp < Constants.MAX_PRUNABLE_LIFETIME && getData() == null) {
            shufflingDataTable.insert(new ShufflingData(shufflingId, accountId, data, timestamp, Nxt.getBlockchain().getHeight()));
        }
    }

    static void restoreData(long shufflingId, long accountId, byte[][] data, int timestamp, int height) {
        if (data != null && getData(shufflingId, accountId) == null) {
            shufflingDataTable.insert(new ShufflingData(shufflingId, accountId, data, timestamp, height));
        }
    }

    public byte[][] getBlameData() {
        return blameData;
    }

    public byte[][] getKeySeeds() {
        return keySeeds;
    }

    void cancel(byte[][] blameData, byte[][] keySeeds) {
        if (this.keySeeds.length > 0) {
            throw new IllegalStateException("keySeeds already set");
        }
        this.blameData = blameData;
        this.keySeeds = keySeeds;
        setState(State.CANCELLED);
        shufflingParticipantTable.insert(this);
        listeners.notify(this, Event.PARTICIPANT_CANCELLED);
    }

    public byte[] getDataTransactionFullHash() {
        return dataTransactionFullHash;
    }

    void setProcessed(byte[] dataTransactionFullHash) {
        if (this.dataTransactionFullHash != null) {
            throw new IllegalStateException("dataTransactionFullHash already set");
        }
        setState(State.PROCESSED);
        this.dataTransactionFullHash = dataTransactionFullHash;
        shufflingParticipantTable.insert(this);
        listeners.notify(this, Event.PARTICIPANT_PROCESSED);
    }

    public ShufflingParticipant getPreviousParticipant() {
        if (index == 0) {
            return null;
        }
        return shufflingParticipantTable.getBy(new DbClause.LongClause("shuffling_id", shufflingId).and(new DbClause.IntClause("participant_index", index - 1)));
    }

    void verify() {
        setState(State.VERIFIED);
        shufflingParticipantTable.insert(this);
        listeners.notify(this, Event.PARTICIPANT_VERIFIED);
    }

    void delete() {
        shufflingParticipantTable.delete(this);
    }

}
