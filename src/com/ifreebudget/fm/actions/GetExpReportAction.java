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
package com.ifreebudget.fm.actions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.ifreebudget.fm.constants.AccountTypes;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.Account;
import com.ifreebudget.fm.entity.beans.FManEntity;
import com.ifreebudget.fm.entity.beans.Transaction;
import com.ifreebudget.fm.search.newfilter.Filter;
import com.ifreebudget.fm.search.newfilter.NewFilterUtils;
import com.ifreebudget.fm.utils.MiscUtils;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class GetExpReportAction {
    private final static String TAG = "GetExpReportAction";
    public GetExpReportAction() {

    }

    public ActionResponse executeAction(ActionRequest request) throws Exception {
        ActionResponse resp = new ActionResponse();
        try {
            FManEntityManager em = FManEntityManager.getInstance();
            Date d = (Date) request.getProperty("DATE");
            String type = (String) request.getProperty("REPORTTYPE");
            
            GregorianCalendar start = new GregorianCalendar();
            GregorianCalendar end = new GregorianCalendar();
            start.setTime(d);
            end.setTime(d);

            if (type.equals("Monthly")) {
                start.set(Calendar.DAY_OF_MONTH, 1);
                int max = end.getActualMaximum(Calendar.DAY_OF_MONTH);
                end.set(Calendar.DAY_OF_MONTH, max);
            }
            else {
                adjustWeekDates(start, end);
            }

            /* set hr:mm:ss of start and end to point to begin and end of range*/
            start.set(Calendar.HOUR_OF_DAY, 0);
            start.set(Calendar.MINUTE, 0);
            start.set(Calendar.SECOND, 0);

            end.set(Calendar.HOUR_OF_DAY, 23);
            end.set(Calendar.MINUTE, 59);
            end.set(Calendar.SECOND, 59);
            /* end set hr:mm:ss of start and end to point to begin and end of range*/
            
            long startS = start.getTimeInMillis();
            long endS = end.getTimeInMillis();

            Filter f = NewFilterUtils.getByDateFilter(startS, endS);

            List<FManEntity> txList = em.executeFilterQuery(
                    f.getQueryObject(false), Transaction.class);

            BigDecimal totalSpending = new BigDecimal("0.0");
            BigDecimal totalIncome = new BigDecimal("0.0");

            Map<Long, ReportItem> items = new LinkedHashMap<Long, ReportItem>();

            for (int i = 0; i < txList.size(); i++) {
                Transaction t = (Transaction) txList.get(i);
                Account from = em.getAccount(t.getFromAccountId());
                Account to = em.getAccount(t.getToAccountId());

                if (to.getAccountType() == AccountTypes.ACCT_TYPE_EXPENSE) {
                    totalSpending = totalSpending.add(t.getTxAmount());
                }
                if (from.getAccountType() == AccountTypes.ACCT_TYPE_INCOME) {
                    totalIncome = totalIncome.add(t.getTxAmount());
                }

                /* Create report item */
                ReportItem item = null;
                if (items.containsKey(to.getAccountId())) {
                    item = items.get(to.getAccountId());
                    item.updateMinAndMax(t);
                }
                else {
                    item = new ReportItem(to, t);
                    items.put(to.getAccountId(), item);
                }
            }

            BigDecimal amtSaved = totalIncome.subtract(totalSpending);

            BigDecimal mult = totalSpending.multiply(new BigDecimal("100"));
            BigDecimal pctSaved = new BigDecimal(0);
            if (totalIncome.intValue() != 0) {
                pctSaved = mult.divide(totalIncome, RoundingMode.HALF_EVEN);
            }
            else {
                pctSaved = new BigDecimal(0);
            }

            resp.setErrorCode(ActionResponse.NOERROR);
            resp.addResult("TOTALINCOME", totalIncome);
            resp.addResult("TOTALSPENDING", totalSpending);
            resp.addResult("SAVINGS", amtSaved);
            resp.addResult("PCTSAVINGS", pctSaved);

            resp.addResult("REPORTITEMS", items.values());
            resp.addResult("STARTDATE", start.getTime());
            resp.addResult("ENDDATE", end.getTime());                      
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
            resp.setErrorCode(ActionResponse.GENERAL_ERROR);
        }

        return resp;
    }
    
    private void adjustWeekDates(GregorianCalendar st, GregorianCalendar en) {
        int dow = st.get(Calendar.DAY_OF_WEEK);
        int toAdd = st.getActualMaximum(Calendar.DAY_OF_WEEK);
        toAdd = toAdd - dow;
        for (int i = 1; i < dow; i++) {
            st.add(Calendar.DATE, -1);
        }
        for (int i = 0; i < toAdd; i++) {
            en.add(Calendar.DATE, 1);
        }
    }    

    public static class ReportItem {
        Account account;
        int numOccurances;
        BigDecimal total;
        BigDecimal min;
        BigDecimal max;

        private ReportItem() {

        }

        public ReportItem(Account account, Transaction t) {
            super();
            this.account = account;
            this.numOccurances = 1;
            this.min = t.getTxAmount();
            this.max = t.getTxAmount();
            this.total = t.getTxAmount();
        }

        public Account getAccount() {
            return account;
        }

        public int getNumOccurances() {
            return numOccurances;
        }

        public BigDecimal getTotal() {
            return total;
        }

        public BigDecimal getMin() {
            return min;
        }

        public BigDecimal getMax() {
            return max;
        }

        private void updateMinAndMax(Transaction t) {
            numOccurances += 1;
            total = total.add(t.getTxAmount());

            if (min != null && max != null) {
                if (t.getTxAmount().compareTo(min) < 0) {
                    min = t.getTxAmount();
                }
                if (t.getTxAmount().compareTo(max) > 0) {
                    max = t.getTxAmount();
                }
            }
            else {
                if (min == null) {
                    min = t.getTxAmount();
                }
                if (max == null) {
                    max = t.getTxAmount();
                }
            }
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((account == null) ? 0 : account.hashCode());
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
            ReportItem other = (ReportItem) obj;
            if (account == null) {
                if (other.account != null)
                    return false;
            }
            else if (!account.equals(other.account))
                return false;
            return true;
        }

        @Override
        public String toString() {
            StringBuilder ret = new StringBuilder();
            ret.append("<i>");
            ret.append(account.getAccountName() + " (" + numOccurances + ")");
            ret.append("</i>");
            return ret.toString();
        }
    }
}
