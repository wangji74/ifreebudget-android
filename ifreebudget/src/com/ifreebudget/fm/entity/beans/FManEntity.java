package com.ifreebudget.fm.entity.beans;

import java.io.Serializable;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public interface FManEntity extends Serializable {
    public String getPKColumnName();

    public Object getPK();

    public void setPK(Object pk);

    public TableMapper getTableMapper();
}