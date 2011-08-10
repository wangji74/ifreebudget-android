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

import java.util.ArrayList;
import java.util.List;

import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.Account;
import com.ifreebudget.fm.entity.beans.AccountCategory;
import com.ifreebudget.fm.entity.beans.FManEntity;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class GetCategoryChildrenAction {

    public ActionResponse executeAction(ActionRequest req) throws Exception {
        ActionResponse resp = new ActionResponse();

        try {
            AccountCategory ac = (AccountCategory) req
                    .getProperty("ACCOUNTCATEGORY");

            FManEntityManager em = FManEntityManager.getInstance();

            List<FManEntity> children = new ArrayList<FManEntity>();

            traverse(ac.getCategoryId(), children, em, 0);

            resp.addResult("CHILDREN", children);
            return resp;
        }
        catch (Exception e) {
            throw e;
        }
    }

    private void traverse(long id, List<FManEntity> accounts,
            FManEntityManager em, int guard) throws Exception {
        List<FManEntity> children = em.getChildren(id);
        if (children == null) {
            return;
        }
        for (FManEntity e : children) {
            if (e instanceof Account) {
                accounts.add(e);
            }
            else {
                AccountCategory ac = (AccountCategory) e;
                traverse(ac.getCategoryId(), accounts, em, ++guard);
                if (guard >= 1000) {
                    throw new RuntimeException("Too many levels of categories");
                }
            }
        }
    }
}
