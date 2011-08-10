/*******************************************************************************
 * Copyright 2011 ifreebudget@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
