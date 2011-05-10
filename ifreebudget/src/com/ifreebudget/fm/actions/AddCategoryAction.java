package com.ifreebudget.fm.actions;

import com.ifreebudget.fm.entity.DBException;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.AccountCategory;

public class AddCategoryAction {
    public AddCategoryAction() {

    }

    public ActionResponse execute(ActionRequest req) {
        ActionResponse resp = new ActionResponse();
        String name = (String) req.getProperty("CATEGORYNAME");
        AccountCategory parent = (AccountCategory) req
                .getProperty("PARENTCATEGORY");

        if (name == null || name.trim().length() == 0) {
            resp.setErrorCode(ActionResponse.EMPTY_CATEGORY);
            return resp;
        }
        if (name != null && name.length() > 30) {
            resp.setErrorCode(ActionResponse.INVALID_CATEGORY_NAME);
            return resp;
        }
        if (parent == null) {
            resp.setErrorCode(ActionResponse.INVALID_PARENT_CATEGORY);
            return resp;
        }

        AccountCategory newcategory = new AccountCategory(null,
                parent.getCategoryId());

        try {
            FManEntityManager em = FManEntityManager.getInstance();
            newcategory.setCategoryName(name);

            try {
                em.addAccountCategory(newcategory);
                resp.setErrorCode(ActionResponse.NOERROR);
                resp.addResult("NEWCATEGORY", newcategory);
            }
            catch (DBException e) {
                resp.setErrorCode(ActionResponse.GENERAL_ERROR);
                resp.setErrorMessage("Failed to add category");
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            resp.setErrorCode(ActionResponse.GENERAL_ERROR);
            resp.setErrorMessage(ex.getMessage());
        }
        return resp;
    }
}