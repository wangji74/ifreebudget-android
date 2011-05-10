package com.ifreebudget.fm.entity.beans;

import java.math.BigDecimal;

public class BudgetedAccount implements FManEntity {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long budgetId;
    private Long accountId;
    private BigDecimal allocatedAmount;
    private transient BigDecimal actualAmount;
    private transient String accountName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getAllocatedAmount() {
        return allocatedAmount;
    }

    public void setAllocatedAmount(BigDecimal allocatedAmount) {
        this.allocatedAmount = allocatedAmount;
    }

    public BigDecimal getActualAmount() {
        return actualAmount;
    }

    public void setActualAmount(BigDecimal actualAmount) {
        this.actualAmount = actualAmount;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public Long getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(Long budgetId) {
        this.budgetId = budgetId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (accountId ^ (accountId >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BudgetedAccount other = (BudgetedAccount) obj;
        if (accountId != other.accountId)
            return false;
        return true;
    }

    public String toString() {
        StringBuilder ret = new StringBuilder();
        ret.append("<i>");
        ret.append(getAccountName());
        ret.append("</i>");
        return ret.toString();
    }

    @Override
    public String getPKColumnName() {
        return "id";
    }

    @Override
    public Object getPK() {
        return getId();
    }

    @Override
    public void setPK(Object pk) {
        setId((Long) pk);
    }

    @Override
    public TableMapper getTableMapper() {
        return new BudgetedAccountMapper();
    }
}
