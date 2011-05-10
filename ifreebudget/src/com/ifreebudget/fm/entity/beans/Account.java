package com.ifreebudget.fm.entity.beans;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Date;

import com.ifreebudget.fm.constants.AccountTypes;
import com.ifreebudget.fm.services.SessionManager;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class Account implements FManEntity, Cloneable, Comparable<Account> {
    private static final long serialVersionUID = 1L;
    private long accountId;
    private int accountType;
    private int accountParentType;
    private int status;
    private String accountName;
    private String accountNotes;
    private BigDecimal startingBalance;
    private BigDecimal currentBalance;
    private Long startDate;
    private Long categoryId;
    private String accountNumber;

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountNotes() {
        return accountNotes;
    }

    public void setAccountNotes(String accountNotes) {
        this.accountNotes = accountNotes;
    }

    public int getAccountType() {
        return accountType;
    }

    public void setAccountType(int accountType) {
        this.accountType = accountType;
    }

    public int getAccountParentType() {
        return accountParentType;
    }

    public void setAccountParentType(int accountParentType) {
        this.accountParentType = accountParentType;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public BigDecimal getStartingBalance() {
        return startingBalance;
    }

    public void setStartingBalance(BigDecimal startingBalance) {
        this.startingBalance = startingBalance;
    }

    public Long getCategoryId() {
        if (categoryId == null)
            return Long.valueOf(accountType);
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder(accountName);

        if (currentBalance != null) {
            ret.append("\t\t");
            ret.append(NumberFormat.getCurrencyInstance(
                    SessionManager.getCurrencyLocale()).format(currentBalance));
            ret.append("");
        }

        return ret.toString();
    }

    @Override
    public final Object clone() throws CloneNotSupportedException {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException e) {
            return null;
        }
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + accountName.hashCode();
        hash = hash * 31 + (accountNotes == null ? 0 : accountNotes.hashCode());
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Account)) {
            return false;
        }
        Account otherAcct = (Account) o;
        return (accountId == otherAcct.accountId);
    }

    public int compareTo(Account o) {
        return accountName.compareTo(o.getAccountName());
    }

    public String getPKColumnName() {
        return "accountId";
    }

    public Object getPK() {
        return Long.valueOf(accountId);
    }

    public void setPK(Object pk) {
        setAccountId((Long) pk);
    }

    public TableMapper getTableMapper() {
        return new AccountMapper();
    }
}