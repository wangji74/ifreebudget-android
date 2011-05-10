package com.ifreebudget.fm.actions;

import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.Account;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class DeleteAccountAction {

    public ActionResponse executeAction(ActionRequest req) throws Exception {
        ActionResponse resp = new ActionResponse();
        FManEntityManager em = FManEntityManager.getInstance();
        try {
            long acctId = (Long) req.getProperty("ACCOUNTID");

            if (!canDelete(em, acctId)) {
                resp.setErrorCode(ActionResponse.ACCOUNT_DELETE_ERROR);
                resp.setErrorMessage("Account has transactions, "
                        + "please delete them first.");
                return resp;
            }

            Account a = new Account();
            a.setAccountId(acctId);

            em.beginTransaction();

            /* Delete account from budget */
            em.deleteAccountFromBudget(acctId);

            /* Delete the account */
            int r = em.deleteEntity(a);
            if (r < 0) {
                resp.setErrorCode(ActionResponse.ACCOUNT_DELETE_ERROR);
                resp.setErrorMessage("Unable to delete account");
                return resp;
            }
            em.setTransactionSuccessful();
            return resp;
        }
        catch (Exception e) {
            throw e;
        }
        finally {
            em.endTransaction();
        }
    }

    private boolean canDelete(FManEntityManager em, long accountId)
            throws Exception {

        int count = em.getTransactionsCount(accountId);

        return count == 0;
    }
}