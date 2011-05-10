package com.ifreebudget.fm.actions;

import java.util.List;

import com.ifreebudget.fm.constants.AccountTypes;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.AccountCategory;
import com.ifreebudget.fm.entity.beans.FManEntity;

public class RenameCategoryAction {
    public ActionResponse executeAction(ActionRequest req) {
        AccountCategory c = (AccountCategory) req
                .getProperty("ACCOUNTCATEGORY");
        String name = (String) req.getProperty("CATEGORYNAME");

        ActionResponse resp = new ActionResponse();
        if (c == null) {
            resp.setErrorCode(ActionResponse.GENERAL_ERROR);
            resp.setErrorMessage("Account category is null");
            return resp;
        }

        if (name == null || name.length() == 0) {
            resp.setErrorCode(ActionResponse.GENERAL_ERROR);
            resp.setErrorMessage("Invalid name");
            return resp;
        }
        if (c.getCategoryId() == AccountTypes.ACCT_TYPE_ROOT
                || c.getCategoryId().intValue() == AccountTypes.ACCT_TYPE_INCOME
                || c.getCategoryId().intValue() == AccountTypes.ACCT_TYPE_CASH
                || c.getCategoryId().intValue() == AccountTypes.ACCT_TYPE_EXPENSE
                || c.getCategoryId().intValue() == AccountTypes.ACCT_TYPE_LIABILITY) {

            resp.setErrorCode(ActionResponse.GENERAL_ERROR);
            resp.setErrorMessage("Cannot rename category" + " : "
                    + c.getCategoryName());
            return resp;
        }

        try {
            /* If parent contains two nodes with same name, invalid */
            long parentId = c.getParentCategoryId();
            FManEntityManager em = FManEntityManager.getInstance();
            List<FManEntity> siblings = em.getAccountCategories(parentId);

            for (Object s : siblings) {
                AccountCategory tmp = (AccountCategory) s;
                if (tmp.getCategoryName().equals(name)) {
                    resp.setErrorCode(ActionResponse.GENERAL_ERROR);
                    resp.setErrorMessage("A category with same name already exists");
                    return resp;
                }
            }

            c.setCategoryName(name);
            em.updateEntity(c);

            resp.setErrorCode(ActionResponse.NOERROR);

            return resp;
        }
        catch (Exception e) {
            resp.setErrorCode(ActionResponse.GENERAL_ERROR);
            resp.setErrorMessage(e.getMessage());
            return resp;
        }
    }
}