package com.ifreebudget.fm.activities;

import java.util.Calendar;
import java.util.Date;

import com.ifreebudget.fm.constants.AccountTypes;
import com.ifreebudget.fm.constants.AccountTypes.TransactionType;
import com.ifreebudget.fm.entity.beans.Account;
import com.ifreebudget.fm.entity.beans.Transaction;
import com.ifreebudget.fm.services.SessionManager;

public class TxHolder {
    Transaction t;
    int iconResource;
    Account fromAcct;
    Account toAcct;
    String displayTxt = null;
    String date = null;
    TxHolder(Transaction t) {
        this.t = t;
        date = SessionManager.getDateFormat().format(
                new Date(t.getTxDate()));
    }
    
    public int getIconResource() {
        return iconResource;
    }
    
    public Transaction getTx() {
        return t;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Long.valueOf(t.getTxId()).hashCode();
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
        TxHolder other = (TxHolder) obj;
        return t.getTxId() == other.t.getTxId();
    }

    @Override
    public String toString() {
        if (displayTxt != null) {
            return displayTxt;
        }

        TransactionType type = AccountTypes.getTransactionType(
                fromAcct.getAccountType(), toAcct.getAccountType());

        StringBuilder ret = new StringBuilder();
        ret.append("<b>");

        if (type == TransactionType.Income) {
            ret.append(fromAcct.getAccountName());
            ret.append("</b>");
            ret.append("<br>");
            ret.append(toAcct.getAccountName());
        }
        else if (type == TransactionType.Expense) {
            ret.append(toAcct.getAccountName());
            ret.append("</i></b>");
            ret.append("<br>");
            ret.append(fromAcct.getAccountName());
        }
        else {
            ret.append(fromAcct.getAccountName());
            ret.append("</i></b>");
            ret.append("<br>");
            ret.append(toAcct.getAccountName());
        }
//        ret.append("<br><i>");
//        ret.append(date);
//        ret.append("</i>");
        return ret.toString();
    }

    public Object getKey() {
        //Date d = new Date(t.getTxDate());
        //return getCalendarKey(d);
        return getFromAccountKey();
    }

    public String getKeyDisplay() {
        return fromAcct.getAccountName();
    }
    
    private Object getFromAccountKey() {
        Long l = fromAcct.getAccountId();
        return l;
    }
    
    private Calendar getCalendarKey(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.AM_PM, Calendar.AM);
        return cal;
    }    
}
