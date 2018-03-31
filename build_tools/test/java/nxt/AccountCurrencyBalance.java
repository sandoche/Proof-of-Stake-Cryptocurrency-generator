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

import nxt.crypto.Crypto;
import nxt.util.Convert;

public class AccountCurrencyBalance {

    long accountId;
    long currencyId;
    long unconfirmedBalance;
    long balance;
    long unconfirmedCurrencyUnits;
    long currencyUnits;

    public AccountCurrencyBalance(long unconfirmedBalance, long balance, long unconfirmedCurrencyUnits, long currencyUnits) {
        this.unconfirmedBalance = unconfirmedBalance;
        this.balance = balance;
        this.unconfirmedCurrencyUnits = unconfirmedCurrencyUnits;
        this.currencyUnits = currencyUnits;
    }

    public AccountCurrencyBalance(String secretPhrase, String currency) {
        Account account = Account.getAccount(Crypto.getPublicKey(secretPhrase));
        accountId = account.getId();
        this.currencyId = Convert.parseUnsignedLong(currency);
        this.unconfirmedBalance = account.getUnconfirmedBalanceNQT();
        this.balance = account.getBalanceNQT();
        this.unconfirmedCurrencyUnits = account.getUnconfirmedCurrencyUnits(currencyId);
        this.currencyUnits = account.getCurrencyUnits(currencyId);
    }

    public long getAccountId() {
        return accountId;
    }

    public long getCurrencyId() {
        return currencyId;
    }

    public long getUnconfirmedBalance() {
        return unconfirmedBalance;
    }

    public long getBalance() {
        return balance;
    }

    public long getUnconfirmedCurrencyUnits() {
        return unconfirmedCurrencyUnits;
    }

    public long getCurrencyUnits() {
        return currencyUnits;
    }

    public AccountCurrencyBalance diff(AccountCurrencyBalance other) {
        return new AccountCurrencyBalance(unconfirmedBalance - other.unconfirmedBalance,
                balance - other.balance,
                unconfirmedCurrencyUnits - other.unconfirmedCurrencyUnits,
                currencyUnits - other.currencyUnits);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccountCurrencyBalance that = (AccountCurrencyBalance) o;

        if (balance != that.balance) return false;
        if (currencyUnits != that.currencyUnits) return false;
        if (unconfirmedBalance != that.unconfirmedBalance) return false;
        //noinspection RedundantIfStatement
        if (unconfirmedCurrencyUnits != that.unconfirmedCurrencyUnits) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (unconfirmedBalance ^ (unconfirmedBalance >>> 32));
        result = 31 * result + (int) (balance ^ (balance >>> 32));
        result = 31 * result + (int) (unconfirmedCurrencyUnits ^ (unconfirmedCurrencyUnits >>> 32));
        result = 31 * result + (int) (currencyUnits ^ (currencyUnits >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "AccountCurrencyBalance{" +
                "unconfirmedBalance=" + unconfirmedBalance +
                ", balance=" + balance +
                ", unconfirmedCurrencyUnits=" + unconfirmedCurrencyUnits +
                ", currencyUnits=" + currencyUnits +
                '}';
    }
}
