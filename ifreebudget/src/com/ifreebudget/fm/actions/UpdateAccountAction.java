package com.ifreebudget.fm.actions;

import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.Account;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class UpdateAccountAction {
    public UpdateAccountAction() {

    }

    public ActionResponse executeAction(ActionRequest req) throws Exception {
        ActionResponse resp = new ActionResponse();
        try {
            Account account = (Account) req.getProperty("ACCOUNT");
            Boolean validate = (Boolean) req.getProperty("VALIDATENAME");

            if (validate == null) {
                validate = Boolean.FALSE;
            }

            FManEntityManager em = FManEntityManager.getInstance();
            if (validate) {
                if (account.getAccountName().length() >= 30) {
                    resp.setErrorCode(ActionResponse.GENERAL_ERROR);
                    resp.setErrorMessage("Invalid account name. Maximum 30 characters");
                    return resp;
                }
                if (em.accountExists(account.getAccountType(),
                        account.getAccountName())) {
                    resp.setErrorCode(ActionResponse.ACCOUNT_EXISTS_ADD_FAIL);
                    resp.setErrorMessage("Account with same name already exists in the category");
                    return resp;
                }
            }
            FManEntityManager.getInstance().updateEntity(account);
            resp.setErrorCode(ActionResponse.NOERROR);
            return resp;
        }
        catch (Exception e) {
            resp.setErrorCode(ActionResponse.GENERAL_ERROR);
            resp.setErrorMessage("Failed to update database.");
            throw e;
        }
    }

    public boolean validate() throws Exception {
        return false;
    }
}