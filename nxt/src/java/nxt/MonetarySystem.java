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

import nxt.AccountLedger.LedgerEvent;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.Map;

public abstract class MonetarySystem extends TransactionType {

    private static final byte SUBTYPE_MONETARY_SYSTEM_CURRENCY_ISSUANCE = 0;
    private static final byte SUBTYPE_MONETARY_SYSTEM_RESERVE_INCREASE = 1;
    private static final byte SUBTYPE_MONETARY_SYSTEM_RESERVE_CLAIM = 2;
    private static final byte SUBTYPE_MONETARY_SYSTEM_CURRENCY_TRANSFER = 3;
    private static final byte SUBTYPE_MONETARY_SYSTEM_PUBLISH_EXCHANGE_OFFER = 4;
    private static final byte SUBTYPE_MONETARY_SYSTEM_EXCHANGE_BUY = 5;
    private static final byte SUBTYPE_MONETARY_SYSTEM_EXCHANGE_SELL = 6;
    private static final byte SUBTYPE_MONETARY_SYSTEM_CURRENCY_MINTING = 7;
    private static final byte SUBTYPE_MONETARY_SYSTEM_CURRENCY_DELETION = 8;

    static TransactionType findTransactionType(byte subtype) {
        switch (subtype) {
            case MonetarySystem.SUBTYPE_MONETARY_SYSTEM_CURRENCY_ISSUANCE:
                return MonetarySystem.CURRENCY_ISSUANCE;
            case MonetarySystem.SUBTYPE_MONETARY_SYSTEM_RESERVE_INCREASE:
                return MonetarySystem.RESERVE_INCREASE;
            case MonetarySystem.SUBTYPE_MONETARY_SYSTEM_RESERVE_CLAIM:
                return MonetarySystem.RESERVE_CLAIM;
            case MonetarySystem.SUBTYPE_MONETARY_SYSTEM_CURRENCY_TRANSFER:
                return MonetarySystem.CURRENCY_TRANSFER;
            case MonetarySystem.SUBTYPE_MONETARY_SYSTEM_PUBLISH_EXCHANGE_OFFER:
                return MonetarySystem.PUBLISH_EXCHANGE_OFFER;
            case MonetarySystem.SUBTYPE_MONETARY_SYSTEM_EXCHANGE_BUY:
                return MonetarySystem.EXCHANGE_BUY;
            case MonetarySystem.SUBTYPE_MONETARY_SYSTEM_EXCHANGE_SELL:
                return MonetarySystem.EXCHANGE_SELL;
            case MonetarySystem.SUBTYPE_MONETARY_SYSTEM_CURRENCY_MINTING:
                return MonetarySystem.CURRENCY_MINTING;
            case MonetarySystem.SUBTYPE_MONETARY_SYSTEM_CURRENCY_DELETION:
                return MonetarySystem.CURRENCY_DELETION;
            default:
                return null;
        }
    }

    private MonetarySystem() {}

    @Override
    public final byte getType() {
        return TransactionType.TYPE_MONETARY_SYSTEM;
    }

    @Override
    boolean isDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
        Attachment.MonetarySystemAttachment attachment = (Attachment.MonetarySystemAttachment) transaction.getAttachment();
        Currency currency = Currency.getCurrency(attachment.getCurrencyId());
        String nameLower = currency.getName().toLowerCase(Locale.ROOT);
        String codeLower = currency.getCode().toLowerCase(Locale.ROOT);
        boolean isDuplicate = TransactionType.isDuplicate(CURRENCY_ISSUANCE, nameLower, duplicates, false);
        if (! nameLower.equals(codeLower)) {
            isDuplicate = isDuplicate || TransactionType.isDuplicate(CURRENCY_ISSUANCE, codeLower, duplicates, false);
        }
        return isDuplicate;
    }

    @Override
    public final boolean isPhasingSafe() {
        return false;
    }

    public static final TransactionType CURRENCY_ISSUANCE = new MonetarySystem() {

        private final Fee FIVE_LETTER_CURRENCY_ISSUANCE_FEE = new Fee.ConstantFee(40 * Constants.ONE_NXT);
        private final Fee FOUR_LETTER_CURRENCY_ISSUANCE_FEE = new Fee.ConstantFee(1000 * Constants.ONE_NXT);
        private final Fee THREE_LETTER_CURRENCY_ISSUANCE_FEE = new Fee.ConstantFee(25000 * Constants.ONE_NXT);

        @Override
        public byte getSubtype() {
            return SUBTYPE_MONETARY_SYSTEM_CURRENCY_ISSUANCE;
        }

        @Override
        public LedgerEvent getLedgerEvent() {
            return LedgerEvent.CURRENCY_ISSUANCE;
        }

        @Override
        public String getName() {
            return "CurrencyIssuance";
        }

        @Override
        Fee getBaselineFee(Transaction transaction) {
            Attachment.MonetarySystemCurrencyIssuance attachment = (Attachment.MonetarySystemCurrencyIssuance) transaction.getAttachment();
            if (Nxt.getBlockchain().getHeight() < Constants.IGNIS_BLOCK && (Currency.getCurrencyByCode(attachment.getCode()) != null || Currency.getCurrencyByCode(attachment.getName()) != null
                    || Currency.getCurrencyByName(attachment.getName()) != null || Currency.getCurrencyByName(attachment.getCode()) != null)) {
                return FIVE_LETTER_CURRENCY_ISSUANCE_FEE;
            }
            int minLength = Math.min(attachment.getCode().length(), attachment.getName().length());
            Currency oldCurrency;
            int oldMinLength = Integer.MAX_VALUE;
            if ((oldCurrency = Currency.getCurrencyByCode(attachment.getCode())) != null) {
                oldMinLength = Math.min(oldMinLength, Math.min(oldCurrency.getCode().length(), oldCurrency.getName().length()));
            }
            if ((oldCurrency = Currency.getCurrencyByCode(attachment.getName())) != null) {
                oldMinLength = Math.min(oldMinLength, Math.min(oldCurrency.getCode().length(), oldCurrency.getName().length()));
            }
            if ((oldCurrency = Currency.getCurrencyByName(attachment.getName())) != null) {
                oldMinLength = Math.min(oldMinLength, Math.min(oldCurrency.getCode().length(), oldCurrency.getName().length()));
            }
            if ((oldCurrency = Currency.getCurrencyByName(attachment.getCode())) != null) {
                oldMinLength = Math.min(oldMinLength, Math.min(oldCurrency.getCode().length(), oldCurrency.getName().length()));
            }
            if (minLength >= oldMinLength) {
                return FIVE_LETTER_CURRENCY_ISSUANCE_FEE;
            }
            switch (minLength) {
                case 3:
                    return THREE_LETTER_CURRENCY_ISSUANCE_FEE;
                case 4:
                    return FOUR_LETTER_CURRENCY_ISSUANCE_FEE;
                case 5:
                    return FIVE_LETTER_CURRENCY_ISSUANCE_FEE;
                default:
                    // never, invalid code length will be checked and caught later
                    return THREE_LETTER_CURRENCY_ISSUANCE_FEE;
            }
        }

        @Override
        long[] getBackFees(Transaction transaction) {
            long feeNQT = transaction.getFeeNQT();
            return new long[] {feeNQT * 3 / 10, feeNQT * 2 / 10, feeNQT / 10};
        }

        @Override
        Attachment.MonetarySystemCurrencyIssuance parseAttachment(ByteBuffer buffer, byte transactionVersion) throws NxtException.NotValidException {
            return new Attachment.MonetarySystemCurrencyIssuance(buffer, transactionVersion);
        }

        @Override
        Attachment.MonetarySystemCurrencyIssuance parseAttachment(JSONObject attachmentData) {
            return new Attachment.MonetarySystemCurrencyIssuance(attachmentData);
        }

        @Override
        boolean isDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            Attachment.MonetarySystemCurrencyIssuance attachment = (Attachment.MonetarySystemCurrencyIssuance) transaction.getAttachment();
            String nameLower = attachment.getName().toLowerCase(Locale.ROOT);
            String codeLower = attachment.getCode().toLowerCase(Locale.ROOT);
            boolean isDuplicate = TransactionType.isDuplicate(CURRENCY_ISSUANCE, nameLower, duplicates, true);
            if (! nameLower.equals(codeLower)) {
                isDuplicate = isDuplicate || TransactionType.isDuplicate(CURRENCY_ISSUANCE, codeLower, duplicates, true);
            }
            return isDuplicate;
        }

        @Override
        boolean isBlockDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            return Nxt.getBlockchain().getHeight() > Constants.SHUFFLING_BLOCK
                    && isDuplicate(CURRENCY_ISSUANCE, getName(), duplicates, true);
        }

        @Override
        void validateAttachment(Transaction transaction) throws NxtException.ValidationException {
            Attachment.MonetarySystemCurrencyIssuance attachment = (Attachment.MonetarySystemCurrencyIssuance) transaction.getAttachment();
            if (attachment.getMaxSupply() > Constants.MAX_CURRENCY_TOTAL_SUPPLY
                    || attachment.getMaxSupply() <= 0
                    || attachment.getInitialSupply() < 0
                    || attachment.getInitialSupply() > attachment.getMaxSupply()
                    || attachment.getReserveSupply() < 0
                    || attachment.getReserveSupply() > attachment.getMaxSupply()
                    || attachment.getIssuanceHeight() < 0
                    || attachment.getMinReservePerUnitNQT() < 0
                    || attachment.getDecimals() < 0 || attachment.getDecimals() > 8
                    || attachment.getRuleset() != 0) {
                throw new NxtException.NotValidException("Invalid currency issuance: " + attachment.getJSONObject());
            }
            int t = 1;
            for (int i = 0; i < 32; i++) {
                if ((t & attachment.getType()) != 0 && CurrencyType.get(t) == null) {
                    throw new NxtException.NotValidException("Invalid currency type: " + attachment.getType());
                }
                t <<= 1;
            }
            CurrencyType.validate(attachment.getType(), transaction);
            CurrencyType.validateCurrencyNaming(transaction.getSenderId(), attachment);
        }


        @Override
        boolean applyAttachmentUnconfirmed(Transaction transaction, Account senderAccount) {
            return true;
        }

        @Override
        void undoAttachmentUnconfirmed(Transaction transaction, Account senderAccount) {
        }

        @Override
        void applyAttachment(Transaction transaction, Account senderAccount, Account recipientAccount) {
            Attachment.MonetarySystemCurrencyIssuance attachment = (Attachment.MonetarySystemCurrencyIssuance) transaction.getAttachment();
            long transactionId = transaction.getId();
            Currency.addCurrency(getLedgerEvent(), transactionId, transaction, senderAccount, attachment);
            senderAccount.addToCurrencyAndUnconfirmedCurrencyUnits(getLedgerEvent(), transactionId,
                    transactionId, attachment.getInitialSupply());
        }

        @Override
        public boolean canHaveRecipient() {
            return false;
        }

    };

    public static final TransactionType RESERVE_INCREASE = new MonetarySystem() {

        @Override
        public byte getSubtype() {
            return SUBTYPE_MONETARY_SYSTEM_RESERVE_INCREASE;
        }

        @Override
        public LedgerEvent getLedgerEvent() {
            return LedgerEvent.CURRENCY_RESERVE_INCREASE;
        }

        @Override
        public String getName() {
            return "ReserveIncrease";
        }

        @Override
        Attachment.MonetarySystemReserveIncrease parseAttachment(ByteBuffer buffer, byte transactionVersion) {
            return new Attachment.MonetarySystemReserveIncrease(buffer, transactionVersion);
        }

        @Override
        Attachment.MonetarySystemReserveIncrease parseAttachment(JSONObject attachmentData) {
            return new Attachment.MonetarySystemReserveIncrease(attachmentData);
        }

        @Override
        void validateAttachment(Transaction transaction) throws NxtException.ValidationException {
            Attachment.MonetarySystemReserveIncrease attachment = (Attachment.MonetarySystemReserveIncrease) transaction.getAttachment();
            if (attachment.getAmountPerUnitNQT() <= 0) {
                throw new NxtException.NotValidException("Reserve increase NXT amount must be positive: " + attachment.getAmountPerUnitNQT());
            }
            CurrencyType.validate(Currency.getCurrency(attachment.getCurrencyId()), transaction);
        }

        @Override
        boolean applyAttachmentUnconfirmed(Transaction transaction, Account senderAccount) {
            Attachment.MonetarySystemReserveIncrease attachment = (Attachment.MonetarySystemReserveIncrease) transaction.getAttachment();
            Currency currency = Currency.getCurrency(attachment.getCurrencyId());
            if (senderAccount.getUnconfirmedBalanceNQT() >= Math.multiplyExact(currency.getReserveSupply(), attachment.getAmountPerUnitNQT())) {
                senderAccount.addToUnconfirmedBalanceNQT(getLedgerEvent(), transaction.getId(),
                        -Math.multiplyExact(currency.getReserveSupply(), attachment.getAmountPerUnitNQT()));
                return true;
            }
            return false;
        }

        @Override
        void undoAttachmentUnconfirmed(Transaction transaction, Account senderAccount) {
            Attachment.MonetarySystemReserveIncrease attachment = (Attachment.MonetarySystemReserveIncrease) transaction.getAttachment();
            long reserveSupply;
            Currency currency = Currency.getCurrency(attachment.getCurrencyId());
            if (currency != null) {
                reserveSupply = currency.getReserveSupply();
            } else { // currency must have been deleted, get reserve supply from the original issuance transaction
                Transaction currencyIssuance = Nxt.getBlockchain().getTransaction(attachment.getCurrencyId());
                Attachment.MonetarySystemCurrencyIssuance currencyIssuanceAttachment = (Attachment.MonetarySystemCurrencyIssuance) currencyIssuance.getAttachment();
                reserveSupply = currencyIssuanceAttachment.getReserveSupply();
            }
            senderAccount.addToUnconfirmedBalanceNQT(getLedgerEvent(), transaction.getId(),
                    Math.multiplyExact(reserveSupply, attachment.getAmountPerUnitNQT()));
        }

        @Override
        void applyAttachment(Transaction transaction, Account senderAccount, Account recipientAccount) {
            Attachment.MonetarySystemReserveIncrease attachment = (Attachment.MonetarySystemReserveIncrease) transaction.getAttachment();
            Currency.increaseReserve(getLedgerEvent(), transaction.getId(), senderAccount, attachment.getCurrencyId(),
                    attachment.getAmountPerUnitNQT());
        }

        @Override
        public boolean canHaveRecipient() {
            return false;
        }

    };

    public static final TransactionType RESERVE_CLAIM = new MonetarySystem() {

        @Override
        public byte getSubtype() {
            return SUBTYPE_MONETARY_SYSTEM_RESERVE_CLAIM;
        }

        @Override
        public LedgerEvent getLedgerEvent() {
            return LedgerEvent.CURRENCY_RESERVE_CLAIM;
        }

        @Override
        public String getName() {
            return "ReserveClaim";
        }

        @Override
        Attachment.MonetarySystemReserveClaim parseAttachment(ByteBuffer buffer, byte transactionVersion) {
            return new Attachment.MonetarySystemReserveClaim(buffer, transactionVersion);
        }

        @Override
        Attachment.MonetarySystemReserveClaim parseAttachment(JSONObject attachmentData) {
            return new Attachment.MonetarySystemReserveClaim(attachmentData);
        }

        @Override
        void validateAttachment(Transaction transaction) throws NxtException.ValidationException {
            Attachment.MonetarySystemReserveClaim attachment = (Attachment.MonetarySystemReserveClaim) transaction.getAttachment();
            if (attachment.getUnits() <= 0) {
                throw new NxtException.NotValidException("Reserve claim number of units must be positive: " + attachment.getUnits());
            }
            CurrencyType.validate(Currency.getCurrency(attachment.getCurrencyId()), transaction);
        }

        @Override
        boolean applyAttachmentUnconfirmed(Transaction transaction, Account senderAccount) {
            Attachment.MonetarySystemReserveClaim attachment = (Attachment.MonetarySystemReserveClaim) transaction.getAttachment();
            if (senderAccount.getUnconfirmedCurrencyUnits(attachment.getCurrencyId()) >= attachment.getUnits()) {
                senderAccount.addToUnconfirmedCurrencyUnits(getLedgerEvent(), transaction.getId(),
                        attachment.getCurrencyId(), -attachment.getUnits());
                return true;
            }
            return false;
        }

        @Override
        void undoAttachmentUnconfirmed(Transaction transaction, Account senderAccount) {
            Attachment.MonetarySystemReserveClaim attachment = (Attachment.MonetarySystemReserveClaim) transaction.getAttachment();
            Currency currency = Currency.getCurrency(attachment.getCurrencyId());
            if (currency != null) {
                senderAccount.addToUnconfirmedCurrencyUnits(getLedgerEvent(), transaction.getId(), attachment.getCurrencyId(),
                        attachment.getUnits());
            }
        }

        @Override
        void applyAttachment(Transaction transaction, Account senderAccount, Account recipientAccount) {
            Attachment.MonetarySystemReserveClaim attachment = (Attachment.MonetarySystemReserveClaim) transaction.getAttachment();
            Currency.claimReserve(getLedgerEvent(), transaction.getId(), senderAccount, attachment.getCurrencyId(),
                    attachment.getUnits());
        }

        @Override
        public boolean canHaveRecipient() {
            return false;
        }

    };

    public static final TransactionType CURRENCY_TRANSFER = new MonetarySystem() {

        @Override
        public byte getSubtype() {
            return SUBTYPE_MONETARY_SYSTEM_CURRENCY_TRANSFER;
        }

        @Override
        public LedgerEvent getLedgerEvent() {
            return LedgerEvent.CURRENCY_TRANSFER;
        }

        @Override
        public String getName() {
            return "CurrencyTransfer";
        }

        @Override
        Attachment.MonetarySystemCurrencyTransfer parseAttachment(ByteBuffer buffer, byte transactionVersion) {
            return new Attachment.MonetarySystemCurrencyTransfer(buffer, transactionVersion);
        }

        @Override
        Attachment.MonetarySystemCurrencyTransfer parseAttachment(JSONObject attachmentData) {
            return new Attachment.MonetarySystemCurrencyTransfer(attachmentData);
        }

        @Override
        void validateAttachment(Transaction transaction) throws NxtException.ValidationException {
            Attachment.MonetarySystemCurrencyTransfer attachment = (Attachment.MonetarySystemCurrencyTransfer) transaction.getAttachment();
            if (attachment.getUnits() <= 0) {
                throw new NxtException.NotValidException("Invalid currency transfer: " + attachment.getJSONObject());
            }
            if (transaction.getRecipientId() == Genesis.CREATOR_ID) {
                throw new NxtException.NotValidException("Currency transfer to genesis account not allowed");
            }
            Currency currency = Currency.getCurrency(attachment.getCurrencyId());
            CurrencyType.validate(currency, transaction);
            if (! currency.isActive()) {
                throw new NxtException.NotCurrentlyValidException("Currency not currently active: " + attachment.getJSONObject());
            }
        }

        @Override
        boolean applyAttachmentUnconfirmed(Transaction transaction, Account senderAccount) {
            Attachment.MonetarySystemCurrencyTransfer attachment = (Attachment.MonetarySystemCurrencyTransfer) transaction.getAttachment();
            if (attachment.getUnits() > senderAccount.getUnconfirmedCurrencyUnits(attachment.getCurrencyId())) {
                return false;
            }
            senderAccount.addToUnconfirmedCurrencyUnits(getLedgerEvent(), transaction.getId(),
                    attachment.getCurrencyId(), -attachment.getUnits());
            return true;
        }

        @Override
        void undoAttachmentUnconfirmed(Transaction transaction, Account senderAccount) {
            Attachment.MonetarySystemCurrencyTransfer attachment = (Attachment.MonetarySystemCurrencyTransfer) transaction.getAttachment();
            Currency currency = Currency.getCurrency(attachment.getCurrencyId());
            if (currency != null) {
                senderAccount.addToUnconfirmedCurrencyUnits(getLedgerEvent(), transaction.getId(),
                        attachment.getCurrencyId(), attachment.getUnits());
            }
        }

        @Override
        void applyAttachment(Transaction transaction, Account senderAccount, Account recipientAccount) {
            Attachment.MonetarySystemCurrencyTransfer attachment = (Attachment.MonetarySystemCurrencyTransfer) transaction.getAttachment();
            Currency.transferCurrency(getLedgerEvent(), transaction.getId(), senderAccount, recipientAccount,
                    attachment.getCurrencyId(), attachment.getUnits());
            CurrencyTransfer.addTransfer(transaction, attachment);
        }

        @Override
        public boolean canHaveRecipient() {
            return true;
        }

    };

    public static final TransactionType PUBLISH_EXCHANGE_OFFER = new MonetarySystem() {

        @Override
        public byte getSubtype() {
            return SUBTYPE_MONETARY_SYSTEM_PUBLISH_EXCHANGE_OFFER;
        }

        @Override
        public LedgerEvent getLedgerEvent() {
            return LedgerEvent.CURRENCY_PUBLISH_EXCHANGE_OFFER;
        }

        @Override
        public String getName() {
            return "PublishExchangeOffer";
        }

        @Override
        Attachment.MonetarySystemPublishExchangeOffer parseAttachment(ByteBuffer buffer, byte transactionVersion) {
            return new Attachment.MonetarySystemPublishExchangeOffer(buffer, transactionVersion);
        }

        @Override
        Attachment.MonetarySystemPublishExchangeOffer parseAttachment(JSONObject attachmentData) {
            return new Attachment.MonetarySystemPublishExchangeOffer(attachmentData);
        }

        @Override
        void validateAttachment(Transaction transaction) throws NxtException.ValidationException {
            Attachment.MonetarySystemPublishExchangeOffer attachment = (Attachment.MonetarySystemPublishExchangeOffer) transaction.getAttachment();
            if (attachment.getBuyRateNQT() <= 0
                    || attachment.getSellRateNQT() <= 0
                    || attachment.getBuyRateNQT() > attachment.getSellRateNQT()) {
                throw new NxtException.NotValidException(String.format("Invalid exchange offer, buy rate %d and sell rate %d has to be larger than 0, buy rate cannot be larger than sell rate",
                        attachment.getBuyRateNQT(), attachment.getSellRateNQT()));
            }
            if (attachment.getTotalBuyLimit() < 0
                    || attachment.getTotalSellLimit() < 0
                    || attachment.getInitialBuySupply() < 0
                    || attachment.getInitialSellSupply() < 0
                    || attachment.getExpirationHeight() < 0) {
                throw new NxtException.NotValidException("Invalid exchange offer, units and height cannot be negative: " + attachment.getJSONObject());
            }
            if (attachment.getTotalBuyLimit() < attachment.getInitialBuySupply()
                    || attachment.getTotalSellLimit() < attachment.getInitialSellSupply()) {
                throw new NxtException.NotValidException("Initial supplies must not exceed total limits");
            }
            if (Nxt.getBlockchain().getHeight() > Constants.SHUFFLING_BLOCK) {
                if (attachment.getTotalBuyLimit() == 0 && attachment.getTotalSellLimit() == 0) {
                    throw new NxtException.NotValidException("Total buy and sell limits cannot be both 0");
                }
                if (attachment.getInitialBuySupply() == 0 && attachment.getInitialSellSupply() == 0) {
                    throw new NxtException.NotValidException("Initial buy and sell supply cannot be both 0");
                }
            }
            if (attachment.getExpirationHeight() <= attachment.getFinishValidationHeight(transaction)) {
                throw new NxtException.NotCurrentlyValidException("Expiration height must be after transaction execution height");
            }
            Currency currency = Currency.getCurrency(attachment.getCurrencyId());
            CurrencyType.validate(currency, transaction);
            if (! currency.isActive()) {
                throw new NxtException.NotCurrentlyValidException("Currency not currently active: " + attachment.getJSONObject());
            }
        }

        @Override
        boolean applyAttachmentUnconfirmed(Transaction transaction, Account senderAccount) {
            Attachment.MonetarySystemPublishExchangeOffer attachment = (Attachment.MonetarySystemPublishExchangeOffer) transaction.getAttachment();
            if (senderAccount.getUnconfirmedBalanceNQT() >= Math.multiplyExact(attachment.getInitialBuySupply(), attachment.getBuyRateNQT())
                    && senderAccount.getUnconfirmedCurrencyUnits(attachment.getCurrencyId()) >= attachment.getInitialSellSupply()) {
                senderAccount.addToUnconfirmedBalanceNQT(getLedgerEvent(), transaction.getId(),
                        -Math.multiplyExact(attachment.getInitialBuySupply(), attachment.getBuyRateNQT()));
                senderAccount.addToUnconfirmedCurrencyUnits(getLedgerEvent(), transaction.getId(),
                        attachment.getCurrencyId(), -attachment.getInitialSellSupply());
                return true;
            }
            return false;
        }

        @Override
        void undoAttachmentUnconfirmed(Transaction transaction, Account senderAccount) {
            Attachment.MonetarySystemPublishExchangeOffer attachment = (Attachment.MonetarySystemPublishExchangeOffer) transaction.getAttachment();
            senderAccount.addToUnconfirmedBalanceNQT(getLedgerEvent(), transaction.getId(),
                    Math.multiplyExact(attachment.getInitialBuySupply(), attachment.getBuyRateNQT()));
            Currency currency = Currency.getCurrency(attachment.getCurrencyId());
            if (currency != null) {
                senderAccount.addToUnconfirmedCurrencyUnits(getLedgerEvent(), transaction.getId(),
                        attachment.getCurrencyId(), attachment.getInitialSellSupply());
            }
        }

        @Override
        void applyAttachment(Transaction transaction, Account senderAccount, Account recipientAccount) {
            Attachment.MonetarySystemPublishExchangeOffer attachment = (Attachment.MonetarySystemPublishExchangeOffer) transaction.getAttachment();
            CurrencyExchangeOffer.publishOffer(transaction, attachment);
        }

        @Override
        public boolean canHaveRecipient() {
            return false;
        }

    };

    abstract static class MonetarySystemExchange extends MonetarySystem {

        @Override
        final void validateAttachment(Transaction transaction) throws NxtException.ValidationException {
            Attachment.MonetarySystemExchange attachment = (Attachment.MonetarySystemExchange) transaction.getAttachment();
            if (attachment.getRateNQT() <= 0 || attachment.getUnits() == 0) {
                throw new NxtException.NotValidException("Invalid exchange: " + attachment.getJSONObject());
            }
            Currency currency = Currency.getCurrency(attachment.getCurrencyId());
            CurrencyType.validate(currency, transaction);
            if (! currency.isActive()) {
                throw new NxtException.NotCurrentlyValidException("Currency not active: " + attachment.getJSONObject());
            }
        }

        @Override
        public final boolean canHaveRecipient() {
            return false;
        }

    }

    public static final TransactionType EXCHANGE_BUY = new MonetarySystemExchange() {

        @Override
        public byte getSubtype() {
            return SUBTYPE_MONETARY_SYSTEM_EXCHANGE_BUY;
        }

        @Override
        public LedgerEvent getLedgerEvent() {
            return LedgerEvent.CURRENCY_EXCHANGE_BUY;
        }

        @Override
        public String getName() {
            return "ExchangeBuy";
        }

        @Override
        Attachment.MonetarySystemExchangeBuy parseAttachment(ByteBuffer buffer, byte transactionVersion) {
            return new Attachment.MonetarySystemExchangeBuy(buffer, transactionVersion);
        }

        @Override
        Attachment.MonetarySystemExchangeBuy parseAttachment(JSONObject attachmentData) {
            return new Attachment.MonetarySystemExchangeBuy(attachmentData);
        }


        @Override
        boolean applyAttachmentUnconfirmed(Transaction transaction, Account senderAccount) {
            Attachment.MonetarySystemExchangeBuy attachment = (Attachment.MonetarySystemExchangeBuy) transaction.getAttachment();
            if (senderAccount.getUnconfirmedBalanceNQT() >= Math.multiplyExact(attachment.getUnits(), attachment.getRateNQT())) {
                senderAccount.addToUnconfirmedBalanceNQT(getLedgerEvent(), transaction.getId(),
                        -Math.multiplyExact(attachment.getUnits(), attachment.getRateNQT()));
                return true;
            }
            return false;
        }

        @Override
        void undoAttachmentUnconfirmed(Transaction transaction, Account senderAccount) {
            Attachment.MonetarySystemExchangeBuy attachment = (Attachment.MonetarySystemExchangeBuy) transaction.getAttachment();
            senderAccount.addToUnconfirmedBalanceNQT(getLedgerEvent(), transaction.getId(),
                    Math.multiplyExact(attachment.getUnits(), attachment.getRateNQT()));
        }

        @Override
        void applyAttachment(Transaction transaction, Account senderAccount, Account recipientAccount) {
            Attachment.MonetarySystemExchangeBuy attachment = (Attachment.MonetarySystemExchangeBuy) transaction.getAttachment();
            ExchangeRequest.addExchangeRequest(transaction, attachment);
            CurrencyExchangeOffer.exchangeNXTForCurrency(transaction, senderAccount, attachment.getCurrencyId(), attachment.getRateNQT(), attachment.getUnits());
        }

    };

    public static final TransactionType EXCHANGE_SELL = new MonetarySystemExchange() {

        @Override
        public byte getSubtype() {
            return SUBTYPE_MONETARY_SYSTEM_EXCHANGE_SELL;
        }

        @Override
        public LedgerEvent getLedgerEvent() {
            return LedgerEvent.CURRENCY_EXCHANGE_SELL;
        }

        @Override
        public String getName() {
            return "ExchangeSell";
        }

        @Override
        Attachment.MonetarySystemExchangeSell parseAttachment(ByteBuffer buffer, byte transactionVersion) {
            return new Attachment.MonetarySystemExchangeSell(buffer, transactionVersion);
        }

        @Override
        Attachment.MonetarySystemExchangeSell parseAttachment(JSONObject attachmentData) {
            return new Attachment.MonetarySystemExchangeSell(attachmentData);
        }

        @Override
        boolean applyAttachmentUnconfirmed(Transaction transaction, Account senderAccount) {
            Attachment.MonetarySystemExchangeSell attachment = (Attachment.MonetarySystemExchangeSell) transaction.getAttachment();
            if (senderAccount.getUnconfirmedCurrencyUnits(attachment.getCurrencyId()) >= attachment.getUnits()) {
                senderAccount.addToUnconfirmedCurrencyUnits(getLedgerEvent(), transaction.getId(),
                        attachment.getCurrencyId(), -attachment.getUnits());
                return true;
            }
            return false;
        }

        @Override
        void undoAttachmentUnconfirmed(Transaction transaction, Account senderAccount) {
            Attachment.MonetarySystemExchangeSell attachment = (Attachment.MonetarySystemExchangeSell) transaction.getAttachment();
            Currency currency = Currency.getCurrency(attachment.getCurrencyId());
            if (currency != null) {
                senderAccount.addToUnconfirmedCurrencyUnits(getLedgerEvent(), transaction.getId(),
                        attachment.getCurrencyId(), attachment.getUnits());
            }
        }

        @Override
        void applyAttachment(Transaction transaction, Account senderAccount, Account recipientAccount) {
            Attachment.MonetarySystemExchangeSell attachment = (Attachment.MonetarySystemExchangeSell) transaction.getAttachment();
            ExchangeRequest.addExchangeRequest(transaction, attachment);
            CurrencyExchangeOffer.exchangeCurrencyForNXT(transaction, senderAccount, attachment.getCurrencyId(), attachment.getRateNQT(), attachment.getUnits());
        }

    };

    public static final TransactionType CURRENCY_MINTING = new MonetarySystem() {

        @Override
        public byte getSubtype() {
            return SUBTYPE_MONETARY_SYSTEM_CURRENCY_MINTING;
        }

        @Override
        public LedgerEvent getLedgerEvent() {
            return LedgerEvent.CURRENCY_MINTING;
        }

        @Override
        public String getName() {
            return "CurrencyMinting";
        }

        @Override
        Attachment.MonetarySystemCurrencyMinting parseAttachment(ByteBuffer buffer, byte transactionVersion) {
            return new Attachment.MonetarySystemCurrencyMinting(buffer, transactionVersion);
        }

        @Override
        Attachment.MonetarySystemCurrencyMinting parseAttachment(JSONObject attachmentData) {
            return new Attachment.MonetarySystemCurrencyMinting(attachmentData);
        }

        @Override
        void validateAttachment(Transaction transaction) throws NxtException.ValidationException {
            Attachment.MonetarySystemCurrencyMinting attachment = (Attachment.MonetarySystemCurrencyMinting) transaction.getAttachment();
            Currency currency = Currency.getCurrency(attachment.getCurrencyId());
            CurrencyType.validate(currency, transaction);
            if (attachment.getUnits() <= 0) {
                throw new NxtException.NotValidException("Invalid number of units: " + attachment.getUnits());
            }
            if (attachment.getUnits() > (currency.getMaxSupply() - currency.getReserveSupply()) / Constants.MAX_MINTING_RATIO) {
                throw new NxtException.NotValidException(String.format("Cannot mint more than 1/%d of the total units supply in a single request", Constants.MAX_MINTING_RATIO));
            }
            if (!currency.isActive()) {
                throw new NxtException.NotCurrentlyValidException("Currency not currently active " + attachment.getJSONObject());
            }
            long counter = CurrencyMint.getCounter(attachment.getCurrencyId(), transaction.getSenderId());
            if (attachment.getCounter() <= counter) {
                throw new NxtException.NotCurrentlyValidException(String.format("Counter %d has to be bigger than %d", attachment.getCounter(), counter));
            }
            if (!CurrencyMinting.meetsTarget(transaction.getSenderId(), currency, attachment)) {
                throw new NxtException.NotCurrentlyValidException(String.format("Hash doesn't meet target %s", attachment.getJSONObject()));
            }
        }

        @Override
        boolean applyAttachmentUnconfirmed(Transaction transaction, Account senderAccount) {
            return true;
        }

        @Override
        void undoAttachmentUnconfirmed(Transaction transaction, Account senderAccount) {
        }

        @Override
        void applyAttachment(Transaction transaction, Account senderAccount, Account recipientAccount) {
            Attachment.MonetarySystemCurrencyMinting attachment = (Attachment.MonetarySystemCurrencyMinting) transaction.getAttachment();
            CurrencyMint.mintCurrency(getLedgerEvent(), transaction.getId(), senderAccount, attachment);
        }

        @Override
        boolean isDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            Attachment.MonetarySystemCurrencyMinting attachment = (Attachment.MonetarySystemCurrencyMinting) transaction.getAttachment();
            return TransactionType.isDuplicate(CURRENCY_MINTING, attachment.getCurrencyId() + ":" + transaction.getSenderId(), duplicates, true)
                    || super.isDuplicate(transaction, duplicates);
        }

        @Override
        boolean isUnconfirmedDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            Attachment.MonetarySystemCurrencyMinting attachment = (Attachment.MonetarySystemCurrencyMinting) transaction.getAttachment();
            return TransactionType.isDuplicate(CURRENCY_MINTING, attachment.getCurrencyId() + ":" + transaction.getSenderId(), duplicates, true);
        }

        @Override
        public boolean canHaveRecipient() {
            return false;
        }

    };

    public static final TransactionType CURRENCY_DELETION = new MonetarySystem() {

        @Override
        public byte getSubtype() {
            return SUBTYPE_MONETARY_SYSTEM_CURRENCY_DELETION;
        }

        @Override
        public LedgerEvent getLedgerEvent() {
            return LedgerEvent.CURRENCY_DELETION;
        }

        @Override
        public String getName() {
            return "CurrencyDeletion";
        }

        @Override
        Attachment.MonetarySystemCurrencyDeletion parseAttachment(ByteBuffer buffer, byte transactionVersion) {
            return new Attachment.MonetarySystemCurrencyDeletion(buffer, transactionVersion);
        }

        @Override
        Attachment.MonetarySystemCurrencyDeletion parseAttachment(JSONObject attachmentData) {
            return new Attachment.MonetarySystemCurrencyDeletion(attachmentData);
        }

        @Override
        boolean isDuplicate(Transaction transaction, Map<TransactionType, Map<String, Integer>> duplicates) {
            Attachment.MonetarySystemCurrencyDeletion attachment = (Attachment.MonetarySystemCurrencyDeletion) transaction.getAttachment();
            Currency currency = Currency.getCurrency(attachment.getCurrencyId());
            String nameLower = currency.getName().toLowerCase(Locale.ROOT);
            String codeLower = currency.getCode().toLowerCase(Locale.ROOT);
            boolean isDuplicate = TransactionType.isDuplicate(CURRENCY_ISSUANCE, nameLower, duplicates, true);
            if (! nameLower.equals(codeLower)) {
                isDuplicate = isDuplicate || TransactionType.isDuplicate(CURRENCY_ISSUANCE, codeLower, duplicates, true);
            }
            return isDuplicate;
        }

        @Override
        void validateAttachment(Transaction transaction) throws NxtException.ValidationException {
            Attachment.MonetarySystemCurrencyDeletion attachment = (Attachment.MonetarySystemCurrencyDeletion) transaction.getAttachment();
            Currency currency = Currency.getCurrency(attachment.getCurrencyId());
            CurrencyType.validate(currency, transaction);
            if (!currency.canBeDeletedBy(transaction.getSenderId())) {
                throw new NxtException.NotCurrentlyValidException("Currency " + Long.toUnsignedString(currency.getId()) + " cannot be deleted by account " +
                        Long.toUnsignedString(transaction.getSenderId()));
            }
        }

        @Override
        boolean applyAttachmentUnconfirmed(Transaction transaction, Account senderAccount) {
            return true;
        }

        @Override
        void undoAttachmentUnconfirmed(Transaction transaction, Account senderAccount) {
        }

        @Override
        void applyAttachment(Transaction transaction, Account senderAccount, Account recipientAccount) {
            Attachment.MonetarySystemCurrencyDeletion attachment = (Attachment.MonetarySystemCurrencyDeletion) transaction.getAttachment();
            Currency currency = Currency.getCurrency(attachment.getCurrencyId());
            currency.delete(getLedgerEvent(), transaction.getId(), senderAccount);
        }

        @Override
        public boolean canHaveRecipient() {
            return false;
        }

    };

}
