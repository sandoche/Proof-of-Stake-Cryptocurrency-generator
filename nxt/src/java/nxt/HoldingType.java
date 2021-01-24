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

public enum HoldingType {

    NXT((byte)0) {

        @Override
        public long getBalance(Account account, long holdingId) {
            if (holdingId != 0) {
                throw new IllegalArgumentException("holdingId must be 0");
            }
            return account.getBalanceNQT();
        }

        @Override
        public long getUnconfirmedBalance(Account account, long holdingId) {
            if (holdingId != 0) {
                throw new IllegalArgumentException("holdingId must be 0");
            }
            return account.getUnconfirmedBalanceNQT();
        }

        @Override
        void addToBalance(Account account, AccountLedger.LedgerEvent event, long eventId, long holdingId, long amount) {
            if (holdingId != 0) {
                throw new IllegalArgumentException("holdingId must be 0");
            }
            account.addToBalanceNQT(event, eventId, amount);
        }

        @Override
        void addToUnconfirmedBalance(Account account, AccountLedger.LedgerEvent event, long eventId, long holdingId, long amount) {
            if (holdingId != 0) {
                throw new IllegalArgumentException("holdingId must be 0");
            }
            account.addToUnconfirmedBalanceNQT(event, eventId, amount);
        }

        @Override
        void addToBalanceAndUnconfirmedBalance(Account account, AccountLedger.LedgerEvent event, long eventId, long holdingId, long amount) {
            if (holdingId != 0) {
                throw new IllegalArgumentException("holdingId must be 0");
            }
            account.addToBalanceAndUnconfirmedBalanceNQT(event, eventId, amount);
        }

    },

    ASSET((byte)1) {

        @Override
        public long getBalance(Account account, long holdingId) {
            return account.getAssetBalanceQNT(holdingId);
        }

        @Override
        public long getUnconfirmedBalance(Account account, long holdingId) {
            return account.getUnconfirmedAssetBalanceQNT(holdingId);
        }

        @Override
        void addToBalance(Account account, AccountLedger.LedgerEvent event, long eventId, long holdingId, long amount) {
            account.addToAssetBalanceQNT(event, eventId, holdingId, amount);
        }

        @Override
        void addToUnconfirmedBalance(Account account, AccountLedger.LedgerEvent event, long eventId, long holdingId, long amount) {
            account.addToUnconfirmedAssetBalanceQNT(event, eventId, holdingId, amount);
        }

        @Override
        void addToBalanceAndUnconfirmedBalance(Account account, AccountLedger.LedgerEvent event, long eventId, long holdingId, long amount) {
            account.addToAssetAndUnconfirmedAssetBalanceQNT(event, eventId, holdingId, amount);
        }

    },

    CURRENCY((byte)2) {

        @Override
        public long getBalance(Account account, long holdingId) {
            return account.getCurrencyUnits(holdingId);
        }

        @Override
        public long getUnconfirmedBalance(Account account, long holdingId) {
            return account.getUnconfirmedCurrencyUnits(holdingId);
        }

        @Override
        void addToBalance(Account account, AccountLedger.LedgerEvent event, long eventId, long holdingId, long amount) {
            account.addToCurrencyUnits(event, eventId, holdingId, amount);
        }

        @Override
        void addToUnconfirmedBalance(Account account, AccountLedger.LedgerEvent event, long eventId, long holdingId, long amount) {
            account.addToUnconfirmedCurrencyUnits(event, eventId, holdingId, amount);
        }

        @Override
        void addToBalanceAndUnconfirmedBalance(Account account, AccountLedger.LedgerEvent event, long eventId, long holdingId, long amount) {
            account.addToCurrencyAndUnconfirmedCurrencyUnits(event, eventId, holdingId, amount);
        }

    };

    public static HoldingType get(byte code) {
        for (HoldingType holdingType : values()) {
            if (holdingType.getCode() == code) {
                return holdingType;
            }
        }
        throw new IllegalArgumentException("Invalid holdingType code: " + code);
    }

    private final byte code;

    HoldingType(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public abstract long getBalance(Account account, long holdingId);

    public abstract long getUnconfirmedBalance(Account account, long holdingId);

    abstract void addToBalance(Account account, AccountLedger.LedgerEvent event, long eventId, long holdingId, long amount);

    abstract void addToUnconfirmedBalance(Account account, AccountLedger.LedgerEvent event, long eventId, long holdingId, long amount);

    abstract void addToBalanceAndUnconfirmedBalance(Account account, AccountLedger.LedgerEvent event, long eventId, long holdingId, long amount);

}
