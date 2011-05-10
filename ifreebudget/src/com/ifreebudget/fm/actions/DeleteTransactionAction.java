package com.ifreebudget.fm.actions;

import java.math.BigDecimal;

import com.ifreebudget.fm.constants.AccountTypes;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.Account;
import com.ifreebudget.fm.entity.beans.Transaction;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class DeleteTransactionAction {

    public ActionResponse executeAction(ActionRequest req) throws Exception {
        long txId = (Long) req.getProperty("TXID");

        ActionResponse resp = new ActionResponse();

        /* Begin tx */
        FManEntityManager em = FManEntityManager.getInstance();
        em.beginTransaction();
        try {
            int delCount = deleteTransaction(em, txId);
            if (delCount != 1) {
                resp.setErrorCode(ActionResponse.TX_DELETE_ERROR);
                resp.setErrorMessage("Unable to delete tx for id " + txId);
            }
            else {
                em.setTransactionSuccessful();
            }
        }
        catch (Exception e) {
            resp.setErrorCode(ActionResponse.TX_DELETE_ERROR);
            resp.setErrorMessage(e.getMessage());
            throw e;
        }
        finally {
            em.endTransaction();
        }
        return resp;
    }

    public boolean validate() {
        return false;
    }

    private void updateFromAcct(Account from, Transaction t) {
        if (from.getAccountType() == AccountTypes.ACCT_TYPE_LIABILITY) {
            BigDecimal curr = from.getCurrentBalance();
            from.setCurrentBalance(curr.subtract(t.getTxAmount()));
            return;
        }
        if (from.getAccountType() == AccountTypes.ACCT_TYPE_CASH) {
            BigDecimal curr = from.getCurrentBalance();
            from.setCurrentBalance(curr.add(t.getTxAmount()));
            return;
        }
        if (from.getAccountType() == AccountTypes.ACCT_TYPE_INCOME) {
            BigDecimal curr = from.getCurrentBalance();
            from.setCurrentBalance(curr.subtract(t.getTxAmount()));
            return;
        }
        if (from.getAccountType() != AccountTypes.ACCT_TYPE_EXPENSE) {
            BigDecimal curr = from.getCurrentBalance();
            from.setCurrentBalance(curr.add(t.getTxAmount()));
            return;
        }
    }

    private void updateToAcct(Account to, Transaction t) {
        if (to.getAccountType() == AccountTypes.ACCT_TYPE_LIABILITY) {
            BigDecimal curr = to.getCurrentBalance();
            to.setCurrentBalance(curr.add(t.getTxAmount()));
        }
        else {
            BigDecimal curr = to.getCurrentBalance();
            to.setCurrentBalance(curr.subtract(t.getTxAmount()));
        }
    }

    private int deleteTransaction(FManEntityManager em, long txId)
            throws Exception {
        Transaction t = em.getTransaction(txId);
        return deleteTransaction(em, t);
    }

    private int deleteTransaction(FManEntityManager em, Transaction t)
            throws Exception {
        try {
            if (t.getTxStatus() == AccountTypes.TX_STATUS_PENDING) {
                return em.deleteEntity(t);
            }

            Account from = em.getAccount(t.getFromAccountId());
            Account to = em.getAccount(t.getToAccountId());

            updateFromAcct(from, t);
            updateToAcct(to, t);

            int rc = em.deleteEntity(t);

            if (rc == 1) {
                em.updateEntity(from);
                em.updateEntity(to);
            }
            return rc;
        }
        catch (Exception e) {
            throw e;
        }
    }
}