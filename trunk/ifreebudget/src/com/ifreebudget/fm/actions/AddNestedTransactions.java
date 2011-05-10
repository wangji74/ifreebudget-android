package com.ifreebudget.fm.actions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

import android.util.Log;

import com.ifreebudget.fm.constants.AccountTypes;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.Account;
import com.ifreebudget.fm.entity.beans.Transaction;
import com.ifreebudget.fm.utils.MiscUtils;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class AddNestedTransactions {
    FManEntityManager em = null;

    private static final String TAG = "AddNestedTransactions";

    public AddNestedTransactions() {
    }

    @SuppressWarnings("unchecked")
    public ActionResponse executeAction(ActionRequest req) throws Exception {
        em = FManEntityManager.getInstance();
        try {
            ArrayList<Transaction> txList = (ArrayList<Transaction>) req
                    .getProperty("TXLIST");

            Date today = new Date();

            boolean isUpdate = (Boolean) req.getProperty("UPDATETX");

            if (isUpdate && txList.size() != 1) {
                ActionResponse errResp = new ActionResponse();
                errResp.setErrorCode(ActionResponse.INVALID_TX);
                errResp.setErrorMessage("Invalid tx to update");
                return errResp;
            }

            ActionResponse resp = null;
            for (Transaction tx : txList) {
                if (new Date(tx.getTxDate()).after(today)) {
                    tx.setTxStatus(AccountTypes.TX_STATUS_PENDING);
                }
                else {
                    tx.setTxStatus(AccountTypes.TX_STATUS_COMPLETED);
                }
            }
            resp = addTransactions(txList, isUpdate);
            return resp;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public boolean validate() {
        return false;
    }

    public void validate(Transaction t, Account from, Account to,
            ActionResponse resp) throws Exception {
        String fitid = t.getFitid();
        if (fitid != null && fitid.trim().length() > 0) {
            if (em.fitIdExists(t.getFromAccountId(), t.getToAccountId(),
                    t.getFitid())) {
                resp.setErrorCode(ActionResponse.TX_EXISTS_ERROR);
                return;
            }
        }
        /* Basic error checking... */
        if (t.getTxAmount().doubleValue() < 0) {
            resp.setErrorCode(ActionResponse.INVALID_TX);
            resp.setErrorMessage("Transaction amount must be greater than zero");
            return;
        }
        if (from.getAccountId() == to.getAccountId()) {
            resp.setErrorCode(ActionResponse.INVALID_TX);
            resp.setErrorMessage("To and from accounts are same");
            return;
        }
        if (from.getStatus() != AccountTypes.ACCOUNT_ACTIVE) {
            resp.setErrorCode(ActionResponse.INACTIVE_ACCOUNT_OPERATION);
            resp.setErrorMessage("Account is locked [" + from.getAccountName()
                    + "]");
            return;
        }
        if (to.getStatus() != AccountTypes.ACCOUNT_ACTIVE) {
            resp.setErrorCode(ActionResponse.INACTIVE_ACCOUNT_OPERATION);
            resp.setErrorMessage("Account is locked [" + to.getAccountName()
                    + "]");
            return;
        }
    }

    private ActionResponse addTransactions(ArrayList<Transaction> txList,
            boolean isUpdate) throws Exception {

        ActionResponse resp = new ActionResponse();
        em.beginTransaction();
        try {
            int sz = txList.size();

            for (int i = 0; i < sz; i++) {
                Transaction t = txList.get(i);
                if (i > 0) {
                    Transaction parent = txList.get(0);
                    t.setParentTxId(parent.getTxId());
                }
                addTransaction(t, resp, isUpdate);

                if (resp.getErrorCode() != ActionResponse.NOERROR) {
                    return resp;
                }
            }
            em.setTransactionSuccessful();
            return resp;
        }
        catch (Exception e) {
            resp.setErrorCode(ActionResponse.GENERAL_ERROR);
            resp.setErrorMessage(e.getMessage());
            throw e;
        }
        finally {
            em.endTransaction();
        }
    }

    private void addTransaction(Transaction t, ActionResponse resp,
            boolean isUpdate) throws Exception {

        Account from = em.getAccount(t.getFromAccountId());
        Account to = em.getAccount(t.getToAccountId());

        validate(t, from, to, resp);
        if (resp.getErrorCode() != ActionResponse.NOERROR) {
            return;
        }

        if (isUpdate) {
            if(!deleteTransaction(t.getTxId())) {
                resp.setErrorCode(ActionResponse.TX_DELETE_ERROR);
                resp.setErrorMessage("Failed to delete existing tx as part of update");
            }
            /* Refresh from and to after delete to update balances correctly. */
            from = em.getAccount(t.getFromAccountId());
            to = em.getAccount(t.getToAccountId());
        }
        
        /* Assumed to be clean from here on */

        if (t.getTxStatus() == AccountTypes.TX_STATUS_PENDING) {
            em.createEntity(t);
            resp.setErrorCode(ActionResponse.NOERROR);
        }
        else {
            if (from.getAccountType() == AccountTypes.ACCT_TYPE_LIABILITY) {
                BigDecimal curr = from.getCurrentBalance();
                from.setCurrentBalance(curr.add(t.getTxAmount()));
            }
            else if (from.getAccountType() == AccountTypes.ACCT_TYPE_INCOME) {
                BigDecimal curr = from.getCurrentBalance();
                from.setCurrentBalance(curr.add(t.getTxAmount()));
            }
            else {
                if (from.getAccountType() != AccountTypes.ACCT_TYPE_EXPENSE) {
                    BigDecimal curr = from.getCurrentBalance();
                    from.setCurrentBalance(curr.subtract(t.getTxAmount()));
                }
            }

            if (to.getAccountType() == AccountTypes.ACCT_TYPE_LIABILITY) {
                BigDecimal curr = to.getCurrentBalance();
                to.setCurrentBalance(curr.subtract(t.getTxAmount()));
            }
            else {
                BigDecimal curr = to.getCurrentBalance();
                to.setCurrentBalance(curr.add(t.getTxAmount()));
            }

            /* Finally set ending balance for from and to accounts */
            t.setFromAccountEndingBal(from.getCurrentBalance());
            t.setToAccountEndingBal(to.getCurrentBalance());
            t.setCreateDate(new Date().getTime());

            em.createEntity(t);
            em.updateEntity(from);
            em.updateEntity(to);

            try {
                em.createTxHistory(from.getAccountId(), to.getAccountId(), null);
            }
            catch (Exception e) {
                // Ignore if this fails
                Log.e(TAG, MiscUtils.stackTrace2String(e));
            }
            resp.setErrorCode(ActionResponse.NOERROR);
        }
    }

    private boolean deleteTransaction(long id) throws Exception {
        try {
            Transaction t = em.getTransaction(id);
            if (t == null) {
                return false;
            }
            if (t.getTxStatus() == AccountTypes.TX_STATUS_PENDING) {
                int rc = em.deleteEntity(t);
                return rc == 1;
            }

            Account from = em.getAccount(t.getFromAccountId());
            Account to = em.getAccount(t.getToAccountId());

            BigDecimal fbal = from.getCurrentBalance();
            BigDecimal tbal = to.getCurrentBalance();

            if (from.getAccountType() == AccountTypes.ACCT_TYPE_LIABILITY
                    || from.getAccountType() == AccountTypes.ACCT_TYPE_INCOME) {
                from.setCurrentBalance(fbal.subtract(t.getTxAmount()));
            }
            else {
                from.setCurrentBalance(fbal.add(t.getTxAmount()));
            }

            if (to.getAccountType() == AccountTypes.ACCT_TYPE_LIABILITY) {
                to.setCurrentBalance(tbal.add(t.getTxAmount()));
            }
            else {
                to.setCurrentBalance(tbal.subtract(t.getTxAmount()));
            }

            int rc = em.deleteEntity(t);
            if (rc == 1) {
                em.updateEntity(from);
                em.updateEntity(to);
                return true;
            }
            return false;
        }
        catch (Exception e) {
            throw e;
        }
    }
}