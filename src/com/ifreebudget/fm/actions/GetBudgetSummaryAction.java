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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.Account;
import com.ifreebudget.fm.entity.beans.Budget;
import com.ifreebudget.fm.entity.beans.BudgetedAccount;
import com.ifreebudget.fm.entity.beans.FManEntity;
import com.ifreebudget.fm.entity.beans.Transaction;

public class GetBudgetSummaryAction {
    public GetBudgetSummaryAction() {
    }

    public ActionResponse executeAction(ActionRequest request) throws Exception {
        ActionResponse resp = new ActionResponse();
        try {
            Budget b = (Budget) request.getProperty("BUDGET");
            Date d = (Date) request.getProperty("DATE");

            GregorianCalendar start = new GregorianCalendar();
            GregorianCalendar end = new GregorianCalendar();
            start.setTime(d);
            end.setTime(d);
            
            if (b.getType() == Budget.MONTHLY) {
                start.set(Calendar.DAY_OF_MONTH, 1);
                int max = end.getActualMaximum(Calendar.DAY_OF_MONTH);
                end.set(Calendar.DAY_OF_MONTH, max);
            }
            else if (b.getType() == Budget.BIWEEKLY) {
                adjustBiWeekDates(start, end);
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

            FManEntityManager em = FManEntityManager.getInstance();

            List<FManEntity> set = em.getList(BudgetedAccount.class,
                    " WHERE BUDGETID = " + b.getId());
            for (FManEntity e : set) {
                BudgetedAccount a = (BudgetedAccount) e;

                StringBuilder filter = new StringBuilder();
                filter.append(" WHERE TOACCOUNTID= ");
                filter.append(a.getAccountId());
                filter.append(" AND TXDATE >= ");
                filter.append(startS);
                filter.append(" AND TXDATE <= ");
                filter.append(endS);
                List<FManEntity> txList = em.getList(Transaction.class,
                        filter.toString());
                BigDecimal actual = new BigDecimal(0);
                if (txList != null) {
                    for (FManEntity o : txList) {
                        Transaction t = (Transaction) o;
                        BigDecimal amt = t.getTxAmount();
                        actual = actual.add(amt);
                    }
                    a.setActualAmount(actual);
                }

                Account acct = em.getAccount(a.getAccountId());
                a.setAccountName(acct.getAccountName());
            }
            resp.addResult("BUDGET", b);
            resp.addResult("BUDGETEDACCOUNTLIST", set);
            resp.addResult("STARTDATE", start.getTime());
            resp.addResult("ENDDATE", end.getTime());
        }
        catch (Exception e) {
            resp.setErrorCode(ActionResponse.GENERAL_ERROR);
            resp.setErrorMessage("Unable to add alert, reason = "
                    + e.getMessage());
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

    private void adjustBiWeekDates(GregorianCalendar st, GregorianCalendar en) {
        adjustWeekDates(st, en);
        en.add(Calendar.WEEK_OF_YEAR, 1);
    }
}
