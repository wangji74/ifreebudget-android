package com.ifreebudget.fm.entity.beans;

import com.ifreebudget.fm.entity.FManEntityManager;

public class TxHistory implements FManEntity {

    private static final long serialVersionUID = 1L;
    private Long id;
    private Long fromAccountId;
    private Long toAccountId;
    private int count;
    private String loc;
    private long lastUpdate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFromAccountId() {
        return fromAccountId;
    }

    public void setFromAccountId(Long fromAccountId) {
        this.fromAccountId = fromAccountId;
    }

    public Long getToAccountId() {
        return toAccountId;
    }

    public void setToAccountId(Long toAccountId) {
        this.toAccountId = toAccountId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getLoc() {
        return loc;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
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
        return FManEntityManager.getInstance().getMapper(TxHistory.class);
    }

    @Override
    public String toString() {
        return "TxHistory [id=" + id + ", fromAccountId=" + fromAccountId
                + ", toAccountId=" + toAccountId + ", count=" + count + "]";
    }
}
