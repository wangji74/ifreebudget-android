package com.ifreebudget.fm.actions;

import java.util.List;

import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.Account;
import com.ifreebudget.fm.entity.beans.FManEntity;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class AddAccountAction {
    public AddAccountAction() {

    }

    public ActionResponse executeAction(FManEntity entity) throws Exception {
        try {
            Account account = (Account) entity;
            FManEntityManager em = FManEntityManager.getInstance();
            ActionResponse resp = new ActionResponse();
            if (validate(em, account, resp)) {
                em.addAccount(account);
                resp.addResult("ACCOUNT", account);
                resp.setErrorCode(ActionResponse.NOERROR);
            }
            return resp;
        }
        catch (Exception e) {
            throw e;
        }
    }

    public boolean validate(FManEntityManager em, Account a, ActionResponse resp)
            throws Exception {
        if (a.getAccountName() == null || a.getAccountName().length() > 30) {
            resp.setErrorCode(ActionResponse.GENERAL_ERROR);
            resp.setErrorMessage("Invalid account name. Maximum 30 characters");
            return false;
        }
        List<FManEntity> aList = em.getAccount(a.getAccountType(),
                a.getAccountName());
        if (aList != null && aList.size() > 0) {
            resp.setErrorCode(ActionResponse.ACCOUNT_EXISTS_ADD_FAIL);
            resp.setErrorMessage("Account with same name or number "
                    + "already exists in the category");
            return false;
        }
        return true;
    }
}