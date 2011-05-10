package com.ifreebudget.fm.actions;

import com.ifreebudget.fm.constants.AccountTypes;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.AccountCategory;

public class DeleteCategoryAction {

    public ActionResponse executeAction(ActionRequest request) throws Exception {
        ActionResponse resp = new ActionResponse();
        AccountCategory c = (AccountCategory) request
                .getProperty("ACCOUNTCATEGORY");

        if (c.getCategoryId() == AccountTypes.ACCT_TYPE_ROOT
                || c.getCategoryId().intValue() == AccountTypes.ACCT_TYPE_INCOME
                || c.getCategoryId().intValue() == AccountTypes.ACCT_TYPE_CASH
                || c.getCategoryId().intValue() == AccountTypes.ACCT_TYPE_EXPENSE
                || c.getCategoryId().intValue() == AccountTypes.ACCT_TYPE_LIABILITY) {

            resp.setErrorCode(1004);
            resp.setErrorMessage("Cannot delete category: "
                    + c.getCategoryName());
            // return "Cannot remove category: " + c.getCategoryName();
            return resp;
        }
        try {
            if (FManEntityManager.getInstance().isCategoryPopulated(
                    c.getCategoryId())) {
                resp.setErrorCode(1005);
                resp.setErrorMessage("Can not delete category with sub categories or accounts.");
                return resp;
            }

            int num = FManEntityManager.getInstance().deleteEntity(c);
            if (num == 1) {
                resp.setErrorCode(ActionResponse.NOERROR);
                return resp;
            }
            else {
                resp.setErrorCode(ActionResponse.GENERAL_ERROR);
                resp.setErrorMessage("Failed to delete category. Unknown reason");
                return resp;
            }
        }
        catch (Exception e) {
            resp.setErrorCode(ActionResponse.GENERAL_ERROR);
            resp.setErrorMessage(e.getMessage());
            return resp;
        }
    }

}