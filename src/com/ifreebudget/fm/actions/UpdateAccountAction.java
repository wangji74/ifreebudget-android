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
