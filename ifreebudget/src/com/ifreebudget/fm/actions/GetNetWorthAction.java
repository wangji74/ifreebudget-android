package com.ifreebudget.fm.actions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ifreebudget.fm.constants.AccountTypes;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.Account;
import com.ifreebudget.fm.entity.beans.FManEntity;

/**
 * @author iFreeBudget ifreebudget@gmail.com
 * 
 */
public class GetNetWorthAction {
    public GetNetWorthAction() {

    }

    public ActionResponse executeAction(ActionRequest req) throws Exception {
        try {
            FManEntityManager em = FManEntityManager.getInstance();

            int[] typesFilter = { AccountTypes.ACCT_TYPE_CASH,
                    AccountTypes.ACCT_TYPE_LIABILITY };
            List<FManEntity> accounts = em.getAccountsForTypes(typesFilter);
            // List<FManEntity> accounts = em.getAllAccounts();

            BigDecimal liabs = new BigDecimal(0.0d);
            BigDecimal assets = new BigDecimal(0.0d);
            BigDecimal total = new BigDecimal(0.0d);

            for (FManEntity e : accounts) {
                Account a = (Account) e;
                if (a.getAccountType() == AccountTypes.ACCT_TYPE_LIABILITY) {
                    liabs = liabs.add(a.getCurrentBalance());
                }
                if (a.getAccountType() == AccountTypes.ACCT_TYPE_CASH) {
                    assets = assets.add(a.getCurrentBalance());
                }
            }
            total = assets.subtract(liabs);
            ActionResponse resp = new ActionResponse();
            resp.setErrorCode(ActionResponse.NOERROR);
            resp.addResult("ASSET_VALUE", assets);
            resp.addResult("LIAB_VALUE", liabs);
            resp.addResult("NET_VALUE", total);
            return resp;
        }
        catch (Exception e) {
            throw e;
        }
    }
}