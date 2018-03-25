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

import nxt.db.DbUtils;
import nxt.util.Convert;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class TransactionDb {

    static TransactionImpl findTransaction(long transactionId) {
        return findTransaction(transactionId, Integer.MAX_VALUE);
    }

    static TransactionImpl findTransaction(long transactionId, int height) {
        // Check the block cache
        synchronized (BlockDb.blockCache) {
            TransactionImpl transaction = BlockDb.transactionCache.get(transactionId);
            if (transaction != null) {
                return transaction.getHeight() <= height ? transaction : null;
            }
        }
        // Search the database
        try (Connection con = Db.db.getConnection();
             PreparedStatement pstmt = con.prepareStatement("SELECT * FROM transaction WHERE id = ?")) {
            pstmt.setLong(1, transactionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && rs.getInt("height") <= height) {
                    return loadTransaction(con, rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        } catch (NxtException.ValidationException e) {
            throw new RuntimeException("Transaction already in database, id = " + transactionId + ", does not pass validation!", e);
        }
    }

    static TransactionImpl findTransactionByFullHash(byte[] fullHash) {
        return findTransactionByFullHash(fullHash, Integer.MAX_VALUE);
    }

    static TransactionImpl findTransactionByFullHash(byte[] fullHash, int height) {
        long transactionId = Convert.fullHashToId(fullHash);
        // Check the cache
        synchronized(BlockDb.blockCache) {
            TransactionImpl transaction = BlockDb.transactionCache.get(transactionId);
            if (transaction != null) {
                return (transaction.getHeight() <= height &&
                        Arrays.equals(transaction.fullHash(), fullHash) ? transaction : null);
            }
        }
        // Search the database
        try (Connection con = Db.db.getConnection();
             PreparedStatement pstmt = con.prepareStatement("SELECT * FROM transaction WHERE id = ?")) {
            pstmt.setLong(1, transactionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && Arrays.equals(rs.getBytes("full_hash"), fullHash) && rs.getInt("height") <= height) {
                    return loadTransaction(con, rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        } catch (NxtException.ValidationException e) {
            throw new RuntimeException("Transaction already in database, full_hash = " + Convert.toHexString(fullHash)
                    + ", does not pass validation!", e);
        }
    }

    static boolean hasTransaction(long transactionId) {
        return hasTransaction(transactionId, Integer.MAX_VALUE);
    }

    static boolean hasTransaction(long transactionId, int height) {
        // Check the block cache
        synchronized(BlockDb.blockCache) {
            TransactionImpl transaction = BlockDb.transactionCache.get(transactionId);
            if (transaction != null) {
                return (transaction.getHeight() <= height);
            }
        }
        // Search the database
        try (Connection con = Db.db.getConnection();
             PreparedStatement pstmt = con.prepareStatement("SELECT height FROM transaction WHERE id = ?")) {
            pstmt.setLong(1, transactionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt("height") <= height;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    static boolean hasTransactionByFullHash(byte[] fullHash) {
        return Arrays.equals(fullHash, getFullHash(Convert.fullHashToId(fullHash)));
    }

    static boolean hasTransactionByFullHash(byte[] fullHash, int height) {
        long transactionId = Convert.fullHashToId(fullHash);
        // Check the block cache
        synchronized(BlockDb.blockCache) {
            TransactionImpl transaction = BlockDb.transactionCache.get(transactionId);
            if (transaction != null) {
                return (transaction.getHeight() <= height &&
                        Arrays.equals(transaction.fullHash(), fullHash));
            }
        }
        // Search the database
        try (Connection con = Db.db.getConnection();
             PreparedStatement pstmt = con.prepareStatement("SELECT full_hash, height FROM transaction WHERE id = ?")) {
            pstmt.setLong(1, transactionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && Arrays.equals(rs.getBytes("full_hash"), fullHash) && rs.getInt("height") <= height;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    static byte[] getFullHash(long transactionId) {
        // Check the block cache
        synchronized(BlockDb.blockCache) {
            TransactionImpl transaction = BlockDb.transactionCache.get(transactionId);
            if (transaction != null) {
                return transaction.fullHash();
            }
        }
        // Search the database
        try (Connection con = Db.db.getConnection();
             PreparedStatement pstmt = con.prepareStatement("SELECT full_hash FROM transaction WHERE id = ?")) {
            pstmt.setLong(1, transactionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? rs.getBytes("full_hash") : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    static TransactionImpl loadTransaction(Connection con, ResultSet rs) throws NxtException.NotValidException {
        try {

            byte type = rs.getByte("type");
            byte subtype = rs.getByte("subtype");
            int timestamp = rs.getInt("timestamp");
            short deadline = rs.getShort("deadline");
            long amountNQT = rs.getLong("amount");
            long feeNQT = rs.getLong("fee");
            byte[] referencedTransactionFullHash = rs.getBytes("referenced_transaction_full_hash");
            int ecBlockHeight = rs.getInt("ec_block_height");
            long ecBlockId = rs.getLong("ec_block_id");
            byte[] signature = rs.getBytes("signature");
            long blockId = rs.getLong("block_id");
            int height = rs.getInt("height");
            long id = rs.getLong("id");
            long senderId = rs.getLong("sender_id");
            byte[] attachmentBytes = rs.getBytes("attachment_bytes");
            int blockTimestamp = rs.getInt("block_timestamp");
            byte[] fullHash = rs.getBytes("full_hash");
            byte version = rs.getByte("version");
            short transactionIndex = rs.getShort("transaction_index");

            ByteBuffer buffer = null;
            if (attachmentBytes != null) {
                buffer = ByteBuffer.wrap(attachmentBytes);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
            }

            TransactionType transactionType = TransactionType.findTransactionType(type, subtype);
            TransactionImpl.BuilderImpl builder = new TransactionImpl.BuilderImpl(version, null,
                    amountNQT, feeNQT, deadline, transactionType.parseAttachment(buffer, version))
                    .timestamp(timestamp)
                    .referencedTransactionFullHash(referencedTransactionFullHash)
                    .signature(signature)
                    .blockId(blockId)
                    .height(height)
                    .id(id)
                    .senderId(senderId)
                    .blockTimestamp(blockTimestamp)
                    .fullHash(fullHash)
                    .ecBlockHeight(ecBlockHeight)
                    .ecBlockId(ecBlockId)
                    .index(transactionIndex);
            if (transactionType.canHaveRecipient()) {
                long recipientId = rs.getLong("recipient_id");
                if (! rs.wasNull()) {
                    builder.recipientId(recipientId);
                }
            }
            if (rs.getBoolean("has_message")) {
                builder.appendix(new Appendix.Message(buffer, version));
            }
            if (rs.getBoolean("has_encrypted_message")) {
                builder.appendix(new Appendix.EncryptedMessage(buffer, version));
            }
            if (rs.getBoolean("has_public_key_announcement")) {
                builder.appendix(new Appendix.PublicKeyAnnouncement(buffer, version));
            }
            if (rs.getBoolean("has_encrypttoself_message")) {
                builder.appendix(new Appendix.EncryptToSelfMessage(buffer, version));
            }
            if (rs.getBoolean("phased")) {
                builder.appendix(new Appendix.Phasing(buffer, version));
            }
            if (rs.getBoolean("has_prunable_message")) {
                builder.appendix(new Appendix.PrunablePlainMessage(buffer, version));
            }
            if (rs.getBoolean("has_prunable_encrypted_message")) {
                builder.appendix(new Appendix.PrunableEncryptedMessage(buffer, version));
            }

            return builder.build();

        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    static List<TransactionImpl> findBlockTransactions(long blockId) {
        // Check the block cache
        synchronized(BlockDb.blockCache) {
            BlockImpl block = BlockDb.blockCache.get(blockId);
            if (block != null) {
                return block.getTransactions();
            }
        }
        // Search the database
        try (Connection con = Db.db.getConnection()) {
            return findBlockTransactions(con, blockId);
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    static List<TransactionImpl> findBlockTransactions(Connection con, long blockId) {
        try (PreparedStatement pstmt = con.prepareStatement("SELECT * FROM transaction WHERE block_id = ? ORDER BY transaction_index")) {
            pstmt.setLong(1, blockId);
            pstmt.setFetchSize(50);
            try (ResultSet rs = pstmt.executeQuery()) {
                List<TransactionImpl> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(loadTransaction(con, rs));
                }
                return list;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        } catch (NxtException.ValidationException e) {
            throw new RuntimeException("Transaction already in database for block_id = " + Long.toUnsignedString(blockId)
                    + " does not pass validation!", e);
        }
    }

    static List<PrunableTransaction> findPrunableTransactions(Connection con, int minTimestamp, int maxTimestamp) {
        List<PrunableTransaction> result = new ArrayList<>();
        try (PreparedStatement pstmt = con.prepareStatement("SELECT id, type, subtype, "
                + "has_prunable_attachment AS prunable_attachment, "
                + "has_prunable_message AS prunable_plain_message, "
                + "has_prunable_encrypted_message AS prunable_encrypted_message "
                + "FROM transaction WHERE (timestamp BETWEEN ? AND ?) AND "
                + "(has_prunable_attachment = TRUE OR has_prunable_message = TRUE OR has_prunable_encrypted_message = TRUE)")) {
            pstmt.setInt(1, minTimestamp);
            pstmt.setInt(2, maxTimestamp);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    byte type = rs.getByte("type");
                    byte subtype = rs.getByte("subtype");
                    TransactionType transactionType = TransactionType.findTransactionType(type, subtype);
                    result.add(new PrunableTransaction(id, transactionType,
                            rs.getBoolean("prunable_attachment"),
                            rs.getBoolean("prunable_plain_message"),
                            rs.getBoolean("prunable_encrypted_message")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
        return result;
    }

    static void saveTransactions(Connection con, List<TransactionImpl> transactions) {
        try {
            short index = 0;
            for (TransactionImpl transaction : transactions) {
                try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO transaction (id, deadline, "
                        + "recipient_id, amount, fee, referenced_transaction_full_hash, height, "
                        + "block_id, signature, timestamp, type, subtype, sender_id, attachment_bytes, "
                        + "block_timestamp, full_hash, version, has_message, has_encrypted_message, has_public_key_announcement, "
                        + "has_encrypttoself_message, phased, has_prunable_message, has_prunable_encrypted_message, "
                        + "has_prunable_attachment, ec_block_height, ec_block_id, transaction_index) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                    int i = 0;
                    pstmt.setLong(++i, transaction.getId());
                    pstmt.setShort(++i, transaction.getDeadline());
                    DbUtils.setLongZeroToNull(pstmt, ++i, transaction.getRecipientId());
                    pstmt.setLong(++i, transaction.getAmountNQT());
                    pstmt.setLong(++i, transaction.getFeeNQT());
                    DbUtils.setBytes(pstmt, ++i, transaction.referencedTransactionFullHash());
                    pstmt.setInt(++i, transaction.getHeight());
                    pstmt.setLong(++i, transaction.getBlockId());
                    pstmt.setBytes(++i, transaction.getSignature());
                    pstmt.setInt(++i, transaction.getTimestamp());
                    pstmt.setByte(++i, transaction.getType().getType());
                    pstmt.setByte(++i, transaction.getType().getSubtype());
                    pstmt.setLong(++i, transaction.getSenderId());
                    int bytesLength = 0;
                    for (Appendix appendage : transaction.getAppendages()) {
                        bytesLength += appendage.getSize();
                    }
                    if (bytesLength == 0) {
                        pstmt.setNull(++i, Types.VARBINARY);
                    } else {
                        ByteBuffer buffer = ByteBuffer.allocate(bytesLength);
                        buffer.order(ByteOrder.LITTLE_ENDIAN);
                        for (Appendix appendage : transaction.getAppendages()) {
                            appendage.putBytes(buffer);
                        }
                        pstmt.setBytes(++i, buffer.array());
                    }
                    pstmt.setInt(++i, transaction.getBlockTimestamp());
                    pstmt.setBytes(++i, transaction.fullHash());
                    pstmt.setByte(++i, transaction.getVersion());
                    pstmt.setBoolean(++i, transaction.getMessage() != null);
                    pstmt.setBoolean(++i, transaction.getEncryptedMessage() != null);
                    pstmt.setBoolean(++i, transaction.getPublicKeyAnnouncement() != null);
                    pstmt.setBoolean(++i, transaction.getEncryptToSelfMessage() != null);
                    pstmt.setBoolean(++i, transaction.getPhasing() != null);
                    pstmt.setBoolean(++i, transaction.hasPrunablePlainMessage());
                    pstmt.setBoolean(++i, transaction.hasPrunableEncryptedMessage());
                    pstmt.setBoolean(++i, transaction.getAttachment() instanceof Appendix.Prunable);
                    pstmt.setInt(++i, transaction.getECBlockHeight());
                    DbUtils.setLongZeroToNull(pstmt, ++i, transaction.getECBlockId());
                    pstmt.setShort(++i, index++);
                    pstmt.executeUpdate();
                }
                if (transaction.referencedTransactionFullHash() != null) {
                    try (PreparedStatement pstmt = con.prepareStatement("INSERT INTO referenced_transaction "
                         + "(transaction_id, referenced_transaction_id) VALUES (?, ?)")) {
                        pstmt.setLong(1, transaction.getId());
                        pstmt.setLong(2, Convert.fullHashToId(transaction.referencedTransactionFullHash()));
                        pstmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.toString(), e);
        }
    }

    static class PrunableTransaction {
        private final long id;
        private final TransactionType transactionType;
        private final boolean prunableAttachment;
        private final boolean prunablePlainMessage;
        private final boolean prunableEncryptedMessage;

        private PrunableTransaction(long id, TransactionType transactionType, boolean prunableAttachment,
                                    boolean prunablePlainMessage, boolean prunableEncryptedMessage) {
            this.id = id;
            this.transactionType = transactionType;
            this.prunableAttachment = prunableAttachment;
            this.prunablePlainMessage = prunablePlainMessage;
            this.prunableEncryptedMessage = prunableEncryptedMessage;
        }

        public long getId() {
            return id;
        }

        public TransactionType getTransactionType() {
            return transactionType;
        }

        public boolean hasPrunableAttachment() {
            return prunableAttachment;
        }

        public boolean hasPrunablePlainMessage() {
            return prunablePlainMessage;
        }

        public boolean hasPrunableEncryptedMessage() {
            return prunableEncryptedMessage;
        }
    }

}
