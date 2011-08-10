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
