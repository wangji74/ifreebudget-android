package com.ifreebudget.fm.actions;

import static com.ifreebudget.fm.utils.Messages.tr;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import android.util.Log;

import com.ifreebudget.fm.constants.AccountTypes;
import com.ifreebudget.fm.entity.DBException;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.Account;
import com.ifreebudget.fm.entity.beans.AccountCategory;
import com.ifreebudget.fm.entity.beans.CategoryIconMap;
import com.ifreebudget.fm.entity.beans.FManEntity;
import com.ifreebudget.fm.utils.MiscUtils;

public class CreateInitialAccounts {
    private static final String TAG = "CreateInitialAccounts";

    public ActionResponse executeAction(ActionRequest request) throws Exception {
        ActionResponse resp = new ActionResponse();
        try {
            FManEntityManager em = FManEntityManager.getInstance();

            createBaseCategories(em);

            List<FManEntity> l = em.getAccountCategories();

            for (FManEntity e : l) {
                AccountCategory c = (AccountCategory) e;
                if (c.getCategoryName().equals(
                        AccountTypes
                                .getAccountType(AccountTypes.ACCT_TYPE_CASH))) {
                    createCashSubCategories(c);
                }
                if (c.getCategoryName()
                        .equals(AccountTypes
                                .getAccountType(AccountTypes.ACCT_TYPE_EXPENSE))) {
                    createExpenseSubCategories(c);
                }
                if (c.getCategoryName().equals(
                        AccountTypes
                                .getAccountType(AccountTypes.ACCT_TYPE_INCOME))) {
                    createIncomeAccount(c);
                }
                if (c.getCategoryName()
                        .equals(AccountTypes
                                .getAccountType(AccountTypes.ACCT_TYPE_LIABILITY))) {
                    createLiabilitySubCategories(c);
                }
            }
            return resp;

        }
        catch (Exception e) {
            Log.e(getClass().getName(), MiscUtils.stackTrace2String(e));
        }
        return resp;
    }

    private void createIncomeAccount(AccountCategory c) {
        try {
            Date now = new Date();
            Account a = new Account();
            a.setAccountName(tr("Primary Job"));
            a.setAccountNotes(tr("This is place holder for primary income account. You can rename/edit/delete this account"));
            a.setAccountType(AccountTypes.ACCT_TYPE_INCOME);
            a.setCurrentBalance(new BigDecimal(0d));
            a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
            a.setCategoryId(c.getCategoryId());
            a.setAccountParentType(a.getAccountType());
            a.setStartDate(now.getTime());
            new AddAccountAction().executeAction(a);
        }
        catch (Exception e) {
            Log.e(getClass().getName(), MiscUtils.stackTrace2String(e));
        }
    }

    private void createBaseCategories(FManEntityManager em) throws Exception {
        AccountCategory i = new AccountCategory(
                Long.valueOf(AccountTypes.ACCT_TYPE_INCOME),
                Long.valueOf(AccountTypes.ACCT_TYPE_ROOT));
        i.setCategoryName(AccountTypes
                .getAccountType(AccountTypes.ACCT_TYPE_INCOME));

        AccountCategory c = new AccountCategory(
                Long.valueOf(AccountTypes.ACCT_TYPE_CASH),
                Long.valueOf(AccountTypes.ACCT_TYPE_ROOT));
        c.setCategoryName(AccountTypes
                .getAccountType(AccountTypes.ACCT_TYPE_CASH));

        AccountCategory e = new AccountCategory(
                Long.valueOf(AccountTypes.ACCT_TYPE_EXPENSE),
                Long.valueOf(AccountTypes.ACCT_TYPE_ROOT));
        e.setCategoryName(AccountTypes
                .getAccountType(AccountTypes.ACCT_TYPE_EXPENSE));

        AccountCategory l = new AccountCategory(
                Long.valueOf(AccountTypes.ACCT_TYPE_LIABILITY),
                Long.valueOf(AccountTypes.ACCT_TYPE_ROOT));

        l.setCategoryName(AccountTypes
                .getAccountType(AccountTypes.ACCT_TYPE_LIABILITY));

        em.addAccountCategory(i);
        em.addAccountCategory(c);
        em.addAccountCategory(e);
        em.addAccountCategory(l);
    }

    private void createExpenseSubCategories(AccountCategory c) {
        ActionRequest req = new ActionRequest();
        req.setActionName("addCategory");
        req.setProperty("CATEGORYNAME", tr("Shopping"));
        req.setProperty("PARENTCATEGORY", c);

        ActionResponse addResponse = new AddCategoryAction().execute(req);

        if (addResponse.getErrorCode() == ActionResponse.NOERROR) {
            Date now = new Date();

            AccountCategory nc = (AccountCategory) addResponse
                    .getResult("NEWCATEGORY");

            createCategoryIconMap("groceries", nc.getCategoryId());

            try {
                Account a = new Account();
                a.setAccountName(tr("Groceries"));
                a.setAccountNotes("");
                a.setAccountType(AccountTypes.ACCT_TYPE_EXPENSE);
                a.setCurrentBalance(new BigDecimal(0d));
                a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
                a.setCategoryId(nc.getCategoryId());
                a.setAccountParentType(a.getAccountType());
                a.setStartDate(now.getTime());
                new AddAccountAction().executeAction(a);
            }
            catch (Exception e) {
                Log.e(getClass().getName(), MiscUtils.stackTrace2String(e));
            }

            try {
                Account a = new Account();
                a.setAccountName(tr("Clothes"));
                a.setAccountNotes("");
                a.setAccountType(AccountTypes.ACCT_TYPE_EXPENSE);
                a.setCurrentBalance(new BigDecimal(0d));
                a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
                a.setCategoryId(nc.getCategoryId());
                a.setAccountParentType(a.getAccountType());
                a.setStartDate(now.getTime());
                new AddAccountAction().executeAction(a);
            }
            catch (Exception e) {
                Log.e(getClass().getName(), MiscUtils.stackTrace2String(e));
            }
        }

        req = new ActionRequest();
        req.setActionName("addCategory");
        req.setProperty("CATEGORYNAME", tr("Dining"));
        req.setProperty("PARENTCATEGORY", c);

        addResponse = new AddCategoryAction().execute(req);
        if (addResponse.getErrorCode() == ActionResponse.NOERROR) {

            AccountCategory nc = (AccountCategory) addResponse
                    .getResult("NEWCATEGORY");

            createCategoryIconMap("restaurant", nc.getCategoryId());

            Date now = new Date();
            try {
                Account a = new Account();
                a.setAccountName(tr("Restaurant"));
                a.setAccountNotes("");
                a.setAccountType(AccountTypes.ACCT_TYPE_EXPENSE);
                a.setCurrentBalance(new BigDecimal(0d));
                a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
                a.setCategoryId(nc.getCategoryId());
                a.setAccountParentType(a.getAccountType());
                a.setStartDate(now.getTime());
                new AddAccountAction().executeAction(a);
            }
            catch (Exception e) {
                Log.e(getClass().getName(), MiscUtils.stackTrace2String(e));
            }

            try {
                Account a = new Account();
                a.setAccountName(tr("Cafeteria at work"));
                a.setAccountNotes("");
                a.setAccountType(AccountTypes.ACCT_TYPE_EXPENSE);
                a.setCurrentBalance(new BigDecimal(0d));
                a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
                a.setCategoryId(nc.getCategoryId());
                a.setAccountParentType(a.getAccountType());
                a.setStartDate(now.getTime());
                new AddAccountAction().executeAction(a);
            }
            catch (Exception e) {
                Log.e(getClass().getName(), MiscUtils.stackTrace2String(e));
            }

        }

        req = new ActionRequest();
        req.setActionName("addCategory");
        req.setProperty("CATEGORYNAME", tr("Bills"));
        req.setProperty("PARENTCATEGORY", c);

        addResponse = new AddCategoryAction().execute(req);
        if (addResponse.getErrorCode() == ActionResponse.NOERROR) {
            AccountCategory nc = (AccountCategory) addResponse
                    .getResult("NEWCATEGORY");

            createCategoryIconMap("bills", nc.getCategoryId());

            Date now = new Date();
            try {
                Account a = new Account();
                a.setAccountName(tr("Rent"));
                a.setAccountNotes("");
                a.setAccountType(AccountTypes.ACCT_TYPE_EXPENSE);
                a.setCurrentBalance(new BigDecimal(0d));
                a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
                a.setCategoryId(nc.getCategoryId());
                a.setAccountParentType(a.getAccountType());
                a.setStartDate(now.getTime());
                new AddAccountAction().executeAction(a);
            }
            catch (Exception e) {
                Log.e(getClass().getName(), MiscUtils.stackTrace2String(e));
            }

            try {
                Account a = new Account();
                a.setAccountName(tr("Wireless bill"));
                a.setAccountNotes("");
                a.setAccountType(AccountTypes.ACCT_TYPE_EXPENSE);
                a.setCurrentBalance(new BigDecimal(0d));
                a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
                a.setCategoryId(nc.getCategoryId());
                a.setAccountParentType(a.getAccountType());
                a.setStartDate(now.getTime());
                new AddAccountAction().executeAction(a);
            }
            catch (Exception e) {
                Log.e(getClass().getName(), MiscUtils.stackTrace2String(e));
            }

            try {
                Account a = new Account();
                a.setAccountName(tr("Internet and Tv"));
                a.setAccountNotes("");
                a.setAccountType(AccountTypes.ACCT_TYPE_EXPENSE);
                a.setCurrentBalance(new BigDecimal(0d));
                a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
                a.setCategoryId(nc.getCategoryId());
                a.setAccountParentType(a.getAccountType());
                a.setStartDate(now.getTime());
                new AddAccountAction().executeAction(a);
            }
            catch (Exception e) {
                Log.e(getClass().getName(), MiscUtils.stackTrace2String(e));
            }

            try {
                Account a = new Account();
                a.setAccountName(tr("Electricity"));
                a.setAccountNotes("");
                a.setAccountType(AccountTypes.ACCT_TYPE_EXPENSE);
                a.setCurrentBalance(new BigDecimal(0d));
                a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
                a.setCategoryId(nc.getCategoryId());
                a.setAccountParentType(a.getAccountType());
                a.setStartDate(now.getTime());
                new AddAccountAction().executeAction(a);
            }
            catch (Exception e) {
                Log.e(getClass().getName(), MiscUtils.stackTrace2String(e));
            }
            try {
                Account a = new Account();
                a.setAccountName(tr("Heating"));
                a.setAccountNotes("");
                a.setAccountType(AccountTypes.ACCT_TYPE_EXPENSE);
                a.setCurrentBalance(new BigDecimal(0d));
                a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
                a.setCategoryId(nc.getCategoryId());
                a.setAccountParentType(a.getAccountType());
                a.setStartDate(now.getTime());
                new AddAccountAction().executeAction(a);
            }
            catch (Exception e) {
                Log.e(getClass().getName(), MiscUtils.stackTrace2String(e));
            }

        }

        req = new ActionRequest();
        req.setActionName("addCategory");
        req.setProperty("CATEGORYNAME", tr("Travel"));
        req.setProperty("PARENTCATEGORY", c);

        addResponse = new AddCategoryAction().execute(req);
        if (addResponse.getErrorCode() == ActionResponse.NOERROR) {

            AccountCategory nc = (AccountCategory) addResponse
                    .getResult("NEWCATEGORY");

            createCategoryIconMap("globe", nc.getCategoryId());

            Date now = new Date();
            try {
                Account a = new Account();
                a.setAccountName(tr("Gas"));
                a.setAccountNotes("");
                a.setAccountType(AccountTypes.ACCT_TYPE_EXPENSE);
                a.setCurrentBalance(new BigDecimal(0d));
                a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
                a.setCategoryId(nc.getCategoryId());
                a.setAccountParentType(a.getAccountType());
                a.setStartDate(now.getTime());
                new AddAccountAction().executeAction(a);
            }
            catch (Exception e) {
                Log.e(getClass().getName(), MiscUtils.stackTrace2String(e));
            }

            try {
                Account a = new Account();
                a.setAccountName(tr("Insurance"));
                a.setAccountNotes("");
                a.setAccountType(AccountTypes.ACCT_TYPE_EXPENSE);
                a.setCurrentBalance(new BigDecimal(0d));
                a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
                a.setCategoryId(nc.getCategoryId());
                a.setAccountParentType(a.getAccountType());
                a.setStartDate(now.getTime());
                new AddAccountAction().executeAction(a);
            }
            catch (Exception e) {
                Log.e(getClass().getName(), MiscUtils.stackTrace2String(e));
            }

            try {
                Account a = new Account();
                a.setAccountName(tr("Maintenance"));
                a.setAccountNotes("");
                a.setAccountType(AccountTypes.ACCT_TYPE_EXPENSE);
                a.setCurrentBalance(new BigDecimal(0d));
                a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
                a.setCategoryId(nc.getCategoryId());
                a.setAccountParentType(a.getAccountType());
                a.setStartDate(now.getTime());
                new AddAccountAction().executeAction(a);
            }
            catch (Exception e) {
                Log.e(getClass().getName(), MiscUtils.stackTrace2String(e));
            }

        }

        req = new ActionRequest();
        req.setActionName("addCategory");
        req.setProperty("CATEGORYNAME", tr("Misc Purchases"));
        req.setProperty("PARENTCATEGORY", c);

        addResponse = new AddCategoryAction().execute(req);
        if (addResponse.getErrorCode() == ActionResponse.NOERROR) {
            AccountCategory nc = (AccountCategory) addResponse
                    .getResult("NEWCATEGORY");
            createCategoryIconMap("misc", nc.getCategoryId());
        }

        req = new ActionRequest();
        req.setActionName("addCategory");
        req.setProperty("CATEGORYNAME", tr("Entertainment"));
        req.setProperty("PARENTCATEGORY", c);

        addResponse = new AddCategoryAction().execute(req);
        if (addResponse.getErrorCode() == ActionResponse.NOERROR) {
            AccountCategory nc = (AccountCategory) addResponse
                    .getResult("NEWCATEGORY");

            createCategoryIconMap("entertainment", nc.getCategoryId());

            Date now = new Date();
            try {
                Account a = new Account();
                a.setAccountName(tr("Movies"));
                a.setAccountNotes("");
                a.setAccountType(AccountTypes.ACCT_TYPE_EXPENSE);
                a.setCurrentBalance(new BigDecimal(0d));
                a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
                a.setCategoryId(nc.getCategoryId());
                a.setAccountParentType(a.getAccountType());
                a.setStartDate(now.getTime());
                new AddAccountAction().executeAction(a);
            }
            catch (Exception e) {
                Log.e(getClass().getName(), MiscUtils.stackTrace2String(e));
            }
            try {
                Account a = new Account();
                a.setAccountName(tr("Music and games"));
                a.setAccountNotes("");
                a.setAccountType(AccountTypes.ACCT_TYPE_EXPENSE);
                a.setCurrentBalance(new BigDecimal(0d));
                a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
                a.setCategoryId(nc.getCategoryId());
                a.setAccountParentType(a.getAccountType());
                a.setStartDate(now.getTime());
                new AddAccountAction().executeAction(a);
            }
            catch (Exception e) {
                Log.e(getClass().getName(), MiscUtils.stackTrace2String(e));
            }

        }

        req = new ActionRequest();
        req.setActionName("addCategory");
        req.setProperty("CATEGORYNAME", tr("School"));
        req.setProperty("PARENTCATEGORY", c);

        addResponse = new AddCategoryAction().execute(req);
        if (addResponse.getErrorCode() == ActionResponse.NOERROR) {
            AccountCategory nc = (AccountCategory) addResponse
                    .getResult("NEWCATEGORY");

            createCategoryIconMap("school", nc.getCategoryId());

            Date now = new Date();
            try {
                Account a = new Account();
                a.setAccountName(tr("Books"));
                a.setAccountNotes("");
                a.setAccountType(AccountTypes.ACCT_TYPE_EXPENSE);
                a.setCurrentBalance(new BigDecimal(0d));
                a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
                a.setCategoryId(nc.getCategoryId());
                a.setAccountParentType(a.getAccountType());
                a.setStartDate(now.getTime());
                new AddAccountAction().executeAction(a);
            }
            catch (Exception e) {
                Log.e(getClass().getName(), MiscUtils.stackTrace2String(e));
            }

            try {
                Account a = new Account();
                a.setAccountName(tr("Supplies"));
                a.setAccountNotes("");
                a.setAccountType(AccountTypes.ACCT_TYPE_EXPENSE);
                a.setCurrentBalance(new BigDecimal(0d));
                a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
                a.setCategoryId(nc.getCategoryId());
                a.setAccountParentType(a.getAccountType());
                a.setStartDate(now.getTime());
                new AddAccountAction().executeAction(a);
            }
            catch (Exception e) {
                Log.e(getClass().getName(), MiscUtils.stackTrace2String(e));
            }

        }
    }

    private void createLiabilitySubCategories(AccountCategory c) {
        ActionRequest req = new ActionRequest();
        req.setActionName("addCategory");
        req.setProperty("CATEGORYNAME", tr("Credit cards"));
        req.setProperty("PARENTCATEGORY", c);

        new AddCategoryAction().execute(req);

        req = new ActionRequest();
        req.setActionName("addCategory");
        req.setProperty("CATEGORYNAME", tr("Loans"));
        req.setProperty("PARENTCATEGORY", c);

        new AddCategoryAction().execute(req);
    }

    private void createCashSubCategories(AccountCategory c) throws Exception {
        Date now = new Date();

        ActionRequest req = new ActionRequest();
        req.setActionName("addCategory");
        req.setProperty("CATEGORYNAME", tr("Checking"));
        req.setProperty("PARENTCATEGORY", c);

        ActionResponse addResponse = new AddCategoryAction().execute(req);
        if (addResponse.getErrorCode() == ActionResponse.NOERROR) {
            AccountCategory nc = (AccountCategory) addResponse
                    .getResult("NEWCATEGORY");
            Account a = new Account();
            a.setAccountName(tr("Primary checking"));
            a.setAccountNotes(tr("This is place holder for primary checking account. You can rename/edit/delete this account"));
            a.setAccountType(AccountTypes.ACCT_TYPE_CASH);
            a.setCurrentBalance(new BigDecimal(0d));
            a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
            a.setCategoryId(nc.getCategoryId());
            a.setAccountParentType(a.getAccountType());
            a.setStartDate(now.getTime());
            new AddAccountAction().executeAction(a);
        }

        req = new ActionRequest();
        req.setActionName("addCategory");
        req.setProperty("CATEGORYNAME", tr("Savings"));
        req.setProperty("PARENTCATEGORY", c);

        addResponse = new AddCategoryAction().execute(req);
        if (addResponse.getErrorCode() == ActionResponse.NOERROR) {
            AccountCategory nc = (AccountCategory) addResponse
                    .getResult("NEWCATEGORY");
            Account a = new Account();
            a.setAccountName(tr("Primary savings"));
            a.setAccountNotes(tr("This is place holder for primary savings account. You can rename/edit/delete this account"));
            a.setAccountType(AccountTypes.ACCT_TYPE_CASH);
            a.setCurrentBalance(new BigDecimal(0d));
            a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
            a.setCategoryId(nc.getCategoryId());
            a.setAccountParentType(a.getAccountType());
            a.setStartDate(now.getTime());
            new AddAccountAction().executeAction(a);
        }

        req = new ActionRequest();
        req.setActionName("addCategory");
        req.setProperty("CATEGORYNAME", tr("Cash in hand"));
        req.setProperty("PARENTCATEGORY", c);

        addResponse = new AddCategoryAction().execute(req);
        if (addResponse.getErrorCode() == ActionResponse.NOERROR) {
            AccountCategory nc = (AccountCategory) addResponse
                    .getResult("NEWCATEGORY");
            Account a = new Account();
            a.setAccountName(tr("Wallet"));
            a.setAccountNotes(tr("This is place holder for cash in hand accounts. You can rename/edit/delete this account"));
            a.setAccountType(AccountTypes.ACCT_TYPE_CASH);
            a.setCurrentBalance(new BigDecimal(0d));
            a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
            a.setCategoryId(nc.getCategoryId());
            a.setAccountParentType(a.getAccountType());
            a.setStartDate(now.getTime());
            new AddAccountAction().executeAction(a);
        }
    }

    private void createCategoryIconMap(String path, Long categoryId) {
        try {
            CategoryIconMap cim = new CategoryIconMap();
            cim.setCategoryId(categoryId);
            cim.setIconPath(path);

            FManEntityManager.getInstance().createEntity(cim);
        }
        catch (DBException e1) {
            Log.e(TAG, "Failed to create icon - caegory map for category: "
                    + path);
        }
    }
}