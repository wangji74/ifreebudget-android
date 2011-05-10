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
