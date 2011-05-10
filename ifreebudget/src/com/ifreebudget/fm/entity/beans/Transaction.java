package com.ifreebudget.fm.entity.beans;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class Transaction implements FManEntity {
    private static final long serialVersionUID = 1L;
    long txId;
    String fitid;
    long fromAccountId;
    long toAccountId;
    BigDecimal txAmount;
    String txNotes;
    Long txDate;
    Long createDate;
    int txStatus;

    Long parentTxId;

    BigDecimal fromAccountEndingBal;
    BigDecimal toAccountEndingBal;

    private int isParent;

    public int getTxStatus() {
        return txStatus;
    }

    public void setTxStatus(int txStatus) {
        this.txStatus = txStatus;
    }

    public Long getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Long createDate) {
        this.createDate = createDate;
    }

    public Long getTxDate() {
        return txDate;
    }

    public void setTxDate(Long txDate) {
        this.txDate = txDate;
    }

    public long getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(long fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public long getTxId() {
        return txId;
    }

    public void setTxId(long id) {
        this.txId = id;
    }

    public long getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(long toAccountId) {
        this.toAccountId = toAccountId;
    }

    public BigDecimal getTxAmount() {
        return txAmount;
    }

    public void setTxAmount(BigDecimal txAmount) {
        this.txAmount = txAmount;
    }

    public String getTxNotes() {
        return txNotes;
    }

    public void setTxNotes(String txNotes) {
        this.txNotes = txNotes;
    }

    public BigDecimal getFromAccountEndingBal() {
        return fromAccountEndingBal;
    }

    public void setFromAccountEndingBal(BigDecimal fromAccountEndingBal) {
        this.fromAccountEndingBal = fromAccountEndingBal;
    }

    public BigDecimal getToAccountEndingBal() {
        return toAccountEndingBal;
    }

    public void setToAccountEndingBal(BigDecimal toAccountEndingBal) {
        this.toAccountEndingBal = toAccountEndingBal;
    }

    public String getFitid() {
        return fitid;
    }

    public void setFitid(String fitid) {
        this.fitid = fitid;
    }

    public Long getParentTxId() {
        return parentTxId;
    }

    public void setParentTxId(Long parentTxId) {
        this.parentTxId = parentTxId;
    }

    public int getIsParent() {
        return isParent;
    }

    public void setIsParent(int isParent) {
        this.isParent = isParent;
    }

    // Interface methods - FManEntity
    public String getPKColumnName() {
        return "txId";
    }

    public Object getPK() {
        return getTxId();
    }

    public void setPK(Object pk) {
        setTxId((Long) pk);
    }

    public TableMapper getTableMapper() {
        return new TransactionMapper();
    }
}