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

import nxt.crypto.Crypto;
import nxt.db.DbIterator;
import nxt.util.Convert;
import nxt.util.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Shuffler {

    private static final int MAX_SHUFFLERS = Nxt.getIntProperty("nxt.maxNumberOfShufflers");
    private static final Map<String, Map<Long, Shuffler>> shufflingsMap = new HashMap<>();
    private static final Map<Integer, Set<String>> expirations = new HashMap<>();

    public static Shuffler addOrGetShuffler(String secretPhrase, byte[] recipientPublicKey, byte[] shufflingFullHash) throws ShufflerException {
        String hash = Convert.toHexString(shufflingFullHash);
        long accountId = Account.getId(Crypto.getPublicKey(secretPhrase));
        BlockchainImpl.getInstance().writeLock();
        try {
            Map<Long, Shuffler> map = shufflingsMap.computeIfAbsent(hash, k -> new HashMap<>());
            Shuffler shuffler = map.get(accountId);
            if (recipientPublicKey == null) {
                return shuffler;
            }
            if (shufflingsMap.size() > MAX_SHUFFLERS) {
                throw new ShufflerLimitException("Cannot run more than " + MAX_SHUFFLERS + " shufflers on the same node");
            }
            if (shuffler == null) {
                Shuffling shuffling = Shuffling.getShuffling(shufflingFullHash);
                if (shuffling == null && Account.getAccount(recipientPublicKey) != null) {
                    throw new InvalidRecipientException("Existing account cannot be used as shuffling recipient");
                }
                if (getRecipientShuffler(Account.getId(recipientPublicKey)) != null) {
                    throw new InvalidRecipientException("Another shuffler with the same recipient account already running");
                }
                if (map.size() >= (shuffling == null ? Constants.MAX_NUMBER_OF_SHUFFLING_PARTICIPANTS : shuffling.getParticipantCount())) {
                    throw new ShufflerLimitException("Cannot run shufflers for more than " + map.size() + " accounts for this shuffling");
                }
                Account account = Account.getAccount(accountId);
                if (account != null && account.getControls().contains(Account.ControlType.PHASING_ONLY)) {
                    throw new ControlledAccountException("Cannot run a shuffler for an account under phasing only control");
                }
                shuffler = new Shuffler(secretPhrase, recipientPublicKey, shufflingFullHash);
                if (shuffling != null) {
                    shuffler.init(shuffling);
                    clearExpiration(shuffling);
                }
                map.put(accountId, shuffler);
                Logger.logMessage(String.format("Started shuffler for account %s, shuffling %s",
                        Long.toUnsignedString(accountId), Long.toUnsignedString(Convert.fullHashToId(shufflingFullHash))));
            } else if (!Arrays.equals(shuffler.recipientPublicKey, recipientPublicKey)) {
                throw new DuplicateShufflerException("A shuffler with different recipientPublicKey already started");
            } else if (!Arrays.equals(shuffler.shufflingFullHash, shufflingFullHash)) {
                throw new DuplicateShufflerException("A shuffler with different shufflingFullHash already started");
            } else {
                Logger.logMessage("Shuffler already started");
            }
            return shuffler;
        } finally {
            BlockchainImpl.getInstance().writeUnlock();
        }
    }

    public static List<Shuffler> getAllShufflers() {
        List<Shuffler> shufflers = new ArrayList<>();
        BlockchainImpl.getInstance().readLock();
        try {
            shufflingsMap.values().forEach(shufflerMap -> shufflers.addAll(shufflerMap.values()));
        } finally {
            BlockchainImpl.getInstance().readUnlock();
        }
        return shufflers;
    }

    public static List<Shuffler> getShufflingShufflers(byte[] shufflingFullHash) {
        List<Shuffler> shufflers = new ArrayList<>();
        BlockchainImpl.getInstance().readLock();
        try {
            Map<Long, Shuffler> shufflerMap = shufflingsMap.get(Convert.toHexString(shufflingFullHash));
            if (shufflerMap != null) {
                shufflers.addAll(shufflerMap.values());
            }
        } finally {
            BlockchainImpl.getInstance().readUnlock();
        }
        return shufflers;
    }

    public static List<Shuffler> getAccountShufflers(long accountId) {
        List<Shuffler> shufflers = new ArrayList<>();
        BlockchainImpl.getInstance().readLock();
        try {
            shufflingsMap.values().forEach(shufflerMap -> {
                Shuffler shuffler = shufflerMap.get(accountId);
                if (shuffler != null) {
                    shufflers.add(shuffler);
                }
            });
        } finally {
            BlockchainImpl.getInstance().readUnlock();
        }
        return shufflers;
    }

    public static Shuffler getShuffler(long accountId, byte[] shufflingFullHash) {
        BlockchainImpl.getInstance().readLock();
        try {
            Map<Long, Shuffler> shufflerMap = shufflingsMap.get(Convert.toHexString(shufflingFullHash));
            if (shufflerMap != null) {
                return shufflerMap.get(accountId);
            }
        } finally {
            BlockchainImpl.getInstance().readUnlock();
        }
        return null;
    }

    public static Shuffler stopShuffler(long accountId, byte[] shufflingFullHash) {
        BlockchainImpl.getInstance().writeLock();
        try {
            Map<Long, Shuffler> shufflerMap = shufflingsMap.get(Convert.toHexString(shufflingFullHash));
            if (shufflerMap != null) {
                return shufflerMap.remove(accountId);
            }
        } finally {
            BlockchainImpl.getInstance().writeUnlock();
        }
        return null;
    }

    public static void stopAllShufflers() {
        BlockchainImpl.getInstance().writeLock();
        try {
            shufflingsMap.clear();
        } finally {
            BlockchainImpl.getInstance().writeUnlock();
        }
    }

    private static Shuffler getRecipientShuffler(long recipientId) {
        BlockchainImpl.getInstance().readLock();
        try {
            for (Map<Long,Shuffler> shufflerMap : shufflingsMap.values()) {
                for (Shuffler shuffler : shufflerMap.values()) {
                    if (Account.getId(shuffler.recipientPublicKey) == recipientId) {
                        return shuffler;
                    }
                }
            }
            return null;
        } finally {
            BlockchainImpl.getInstance().readUnlock();
        }
    }

    static {

        Shuffling.addListener(shuffling -> {
            Map<Long, Shuffler> shufflerMap = getShufflers(shuffling);
            if (shufflerMap != null) {
                shufflerMap.values().forEach(shuffler -> {
                    if (shuffler.accountId != shuffling.getIssuerId()) {
                        try {
                            shuffler.submitRegister(shuffling);
                        } catch (RuntimeException e) {
                            Logger.logErrorMessage(e.toString(), e);
                        }
                    }
                });
                clearExpiration(shuffling);
            }
        }, Shuffling.Event.SHUFFLING_CREATED);

        Shuffling.addListener(shuffling -> {
            Map<Long, Shuffler> shufflerMap = getShufflers(shuffling);
            if (shufflerMap != null) {
                Shuffler shuffler = shufflerMap.get(shuffling.getAssigneeAccountId());
                if (shuffler != null) {
                    try {
                        shuffler.submitProcess(shuffling);
                    } catch (RuntimeException e) {
                        Logger.logErrorMessage(e.toString(), e);
                    }
                }
                clearExpiration(shuffling);
            }
        }, Shuffling.Event.SHUFFLING_PROCESSING_ASSIGNED);

        Shuffling.addListener(shuffling -> {
            Map<Long, Shuffler> shufflerMap = getShufflers(shuffling);
            if (shufflerMap != null) {
                shufflerMap.values().forEach(shuffler -> {
                    try {
                        shuffler.verify(shuffling);
                    } catch (RuntimeException e) {
                        Logger.logErrorMessage(e.toString(), e);
                    }
                });
                clearExpiration(shuffling);
            }
        }, Shuffling.Event.SHUFFLING_PROCESSING_FINISHED);

        Shuffling.addListener(shuffling -> {
            Map<Long, Shuffler> shufflerMap = getShufflers(shuffling);
            if (shufflerMap != null) {
                shufflerMap.values().forEach(shuffler -> {
                    try {
                        shuffler.cancel(shuffling);
                    } catch (RuntimeException e) {
                        Logger.logErrorMessage(e.toString(), e);
                    }
                });
                clearExpiration(shuffling);
            }
        }, Shuffling.Event.SHUFFLING_BLAME_STARTED);

        Shuffling.addListener(Shuffler::scheduleExpiration, Shuffling.Event.SHUFFLING_DONE);

        Shuffling.addListener(Shuffler::scheduleExpiration, Shuffling.Event.SHUFFLING_CANCELLED);

        BlockchainProcessorImpl.getInstance().addListener(block -> {
            Set<String> expired = expirations.get(block.getHeight());
            if (expired != null) {
                expired.forEach(shufflingsMap::remove);
                expirations.remove(block.getHeight());
            }
        }, BlockchainProcessor.Event.AFTER_BLOCK_APPLY);

        BlockchainProcessorImpl.getInstance().addListener(block -> shufflingsMap.values().forEach(shufflerMap -> shufflerMap.values().forEach(shuffler -> {
            if (shuffler.failedTransaction != null) {
                try {
                    TransactionProcessorImpl.getInstance().broadcast(shuffler.failedTransaction);
                    shuffler.failedTransaction = null;
                    shuffler.failureCause = null;
                } catch (NxtException.ValidationException ignore) {
                }
            }
        })), BlockchainProcessor.Event.AFTER_BLOCK_ACCEPT);

        BlockchainProcessorImpl.getInstance().addListener(block -> stopAllShufflers(), BlockchainProcessor.Event.RESCAN_BEGIN);

    }

    private static Map<Long, Shuffler> getShufflers(Shuffling shuffling) {
        return shufflingsMap.get(Convert.toHexString(shuffling.getFullHash()));
    }

    private static void scheduleExpiration(Shuffling shuffling) {
        int expirationHeight = Nxt.getBlockchain().getHeight() + 720;
        Set<String> shufflingIds = expirations.computeIfAbsent(expirationHeight, k -> new HashSet<>());
        shufflingIds.add(Convert.toHexString(shuffling.getFullHash()));
    }

    private static void clearExpiration(Shuffling shuffling) {
        for (Set shufflingIds : expirations.values()) {
            if (shufflingIds.remove(shuffling.getId())) {
                return;
            }
        }
    }

    private final long accountId;
    private final String secretPhrase;
    private final byte[] recipientPublicKey;
    private final byte[] shufflingFullHash;
    private volatile Transaction failedTransaction;
    private volatile NxtException.NotCurrentlyValidException failureCause;

    private Shuffler(String secretPhrase, byte[] recipientPublicKey, byte[] shufflingFullHash) {
        this.secretPhrase = secretPhrase;
        this.accountId = Account.getId(Crypto.getPublicKey(secretPhrase));
        this.recipientPublicKey = recipientPublicKey;
        this.shufflingFullHash = shufflingFullHash;
    }

    public long getAccountId() {
        return accountId;
    }

    public byte[] getRecipientPublicKey() {
        return recipientPublicKey;
    }

    public byte[] getShufflingFullHash() {
        return shufflingFullHash;
    }

    public Transaction getFailedTransaction() {
        return failedTransaction;
    }

    public NxtException.NotCurrentlyValidException getFailureCause() {
        return failureCause;
    }

    private void init(Shuffling shuffling) throws ShufflerException {
        ShufflingParticipant shufflingParticipant = shuffling.getParticipant(accountId);
        switch (shuffling.getStage()) {
            case REGISTRATION:
                if (Account.getAccount(recipientPublicKey) != null) {
                    throw new InvalidRecipientException("Existing account cannot be used as shuffling recipient");
                }
                if (shufflingParticipant == null) {
                    submitRegister(shuffling);
                }
                break;
            case PROCESSING:
                if (shufflingParticipant == null) {
                    throw new InvalidStageException("Account has not registered for this shuffling");
                }
                if (Account.getAccount(recipientPublicKey) != null) {
                    throw new InvalidRecipientException("Existing account cannot be used as shuffling recipient");
                }
                if (accountId == shuffling.getAssigneeAccountId()) {
                    submitProcess(shuffling);
                }
                break;
            case VERIFICATION:
                if (shufflingParticipant == null) {
                    throw new InvalidStageException("Account has not registered for this shuffling");
                }
                if (shufflingParticipant.getState() == ShufflingParticipant.State.PROCESSED) {
                    verify(shuffling);
                }
                break;
            case BLAME:
                if (shufflingParticipant == null) {
                    throw new InvalidStageException("Account has not registered for this shuffling");
                }
                if (shufflingParticipant.getState() != ShufflingParticipant.State.CANCELLED) {
                    cancel(shuffling);
                }
                break;
            case DONE:
            case CANCELLED:
                scheduleExpiration(shuffling);
                break;
            default:
                throw new RuntimeException("Unsupported shuffling stage " + shuffling.getStage());
        }
        if (failureCause != null) {
            throw new ShufflerException(failureCause.getMessage(), failureCause);
        }
    }

    private void verify(Shuffling shuffling) {
        ShufflingParticipant shufflingParticipant = shuffling.getParticipant(accountId);
        if (shufflingParticipant != null && shufflingParticipant.getIndex() != shuffling.getParticipantCount() - 1) {
            boolean found = false;
            for (byte[] key : shuffling.getRecipientPublicKeys()) {
                if (Arrays.equals(key, recipientPublicKey)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                submitVerify(shuffling);
            } else {
                submitCancel(shuffling);
            }
        }
    }

    private void cancel(Shuffling shuffling) {
        if (accountId == shuffling.getAssigneeAccountId()) {
            return;
        }
        ShufflingParticipant shufflingParticipant = shuffling.getParticipant(accountId);
        if (shufflingParticipant == null || shufflingParticipant.getIndex() == shuffling.getParticipantCount() - 1) {
            return;
        }
        if (ShufflingParticipant.getData(shuffling.getId(), accountId) == null) {
            return;
        }
        submitCancel(shuffling);
    }

    private void submitRegister(Shuffling shuffling) {
        Logger.logDebugMessage("Account %s registering for shuffling %s", Long.toUnsignedString(accountId), Long.toUnsignedString(shuffling.getId()));
        Attachment.ShufflingRegistration attachment = new Attachment.ShufflingRegistration(shufflingFullHash);
        submitTransaction(attachment);
    }

    private void submitProcess(Shuffling shuffling) {
        Logger.logDebugMessage("Account %s processing shuffling %s", Long.toUnsignedString(accountId), Long.toUnsignedString(shuffling.getId()));
        Attachment.ShufflingAttachment attachment = shuffling.process(accountId, secretPhrase, recipientPublicKey);
        submitTransaction(attachment);
    }

    private void submitVerify(Shuffling shuffling) {
        Logger.logDebugMessage("Account %s verifying shuffling %s", Long.toUnsignedString(accountId), Long.toUnsignedString(shuffling.getId()));
        Attachment.ShufflingVerification attachment = new Attachment.ShufflingVerification(shuffling.getId(), shuffling.getStateHash());
        submitTransaction(attachment);
    }

    private void submitCancel(Shuffling shuffling) {
        Logger.logDebugMessage("Account %s cancelling shuffling %s", Long.toUnsignedString(accountId), Long.toUnsignedString(shuffling.getId()));
        Attachment.ShufflingCancellation attachment = shuffling.revealKeySeeds(secretPhrase, shuffling.getAssigneeAccountId(), shuffling.getStateHash());
        submitTransaction(attachment);
    }

    private void submitTransaction(Attachment.ShufflingAttachment attachment) {
        if (BlockchainProcessorImpl.getInstance().isProcessingBlock()) {
            if (hasUnconfirmedTransaction(attachment, TransactionProcessorImpl.getInstance().getWaitingTransactions())) {
                Logger.logDebugMessage("Transaction already submitted");
                return;
            }
        } else {
            try (DbIterator<UnconfirmedTransaction> unconfirmedTransactions = TransactionProcessorImpl.getInstance().getAllUnconfirmedTransactions()) {
                if (hasUnconfirmedTransaction(attachment, unconfirmedTransactions)) {
                    Logger.logDebugMessage("Transaction already submitted");
                    return;
                }
            }
        }
        try {
            Transaction.Builder builder = Nxt.newTransactionBuilder(Crypto.getPublicKey(secretPhrase), 0, 0,
                    (short) 1440, attachment);
            builder.timestamp(Nxt.getBlockchain().getLastBlockTimestamp());
            Transaction transaction = builder.build(secretPhrase);
            failedTransaction = null;
            failureCause = null;
            Account participantAccount = Account.getAccount(this.accountId);
            if (participantAccount == null || transaction.getFeeNQT() > participantAccount.getUnconfirmedBalanceNQT()) {
                failedTransaction = transaction;
                failureCause = new NxtException.NotCurrentlyValidException("Insufficient balance");
                Logger.logDebugMessage("Error submitting shuffler transaction", failureCause);
            }
            try {
                TransactionProcessorImpl.getInstance().broadcast(transaction);
            } catch (NxtException.NotCurrentlyValidException e) {
                failedTransaction = transaction;
                failureCause = e;
                Logger.logDebugMessage("Error submitting shuffler transaction", e);
            }
        } catch (NxtException.ValidationException e) {
            Logger.logErrorMessage("Fatal error submitting shuffler transaction", e);
        }
    }

    private boolean hasUnconfirmedTransaction(Attachment.ShufflingAttachment shufflingAttachment, Iterable<UnconfirmedTransaction> unconfirmedTransactions) {
        for (UnconfirmedTransaction unconfirmedTransaction : unconfirmedTransactions) {
            if (unconfirmedTransaction.getSenderId() != accountId) {
                continue;
            }
            Attachment attachment = unconfirmedTransaction.getAttachment();
            if (!attachment.getClass().equals(shufflingAttachment.getClass())) {
                continue;
            }
            if (Arrays.equals(shufflingAttachment.getShufflingStateHash(), ((Attachment.ShufflingAttachment)attachment).getShufflingStateHash())) {
                return true;
            }
        }
        return false;
    }

    public static class ShufflerException extends NxtException {

        private ShufflerException(String message) {
            super(message);
        }

        private ShufflerException(String message, Throwable cause) {
            super(message, cause);
        }

    }

    public static final class ShufflerLimitException extends ShufflerException {

        private ShufflerLimitException(String message) {
            super(message);
        }

    }

    public static final class DuplicateShufflerException extends ShufflerException {

        private DuplicateShufflerException(String message) {
            super(message);
        }

    }

    public static final class InvalidRecipientException extends ShufflerException {

        private InvalidRecipientException(String message) {
            super(message);
        }

    }

    public static final class ControlledAccountException extends ShufflerException {

        private ControlledAccountException(String message) {
            super(message);
        }

    }

    public static final class InvalidStageException extends ShufflerException {

        private InvalidStageException(String message) {
            super(message);
        }

    }

}
