package com.ifreebudget.fm.activities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.widget.Toast;

import com.ifreebudget.fm.actions.ActionRequest;
import com.ifreebudget.fm.actions.ActionResponse;
import com.ifreebudget.fm.actions.GetCategoryChildrenAction;
import com.ifreebudget.fm.entity.DBException;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.Account;
import com.ifreebudget.fm.entity.beans.AccountCategory;
import com.ifreebudget.fm.entity.beans.FManEntity;
import com.ifreebudget.fm.search.newfilter.Filter;
import com.ifreebudget.fm.search.newfilter.NewFilterUtils;
import com.ifreebudget.fm.search.newfilter.NewFilterUtils.DATE_RANGE;
import com.ifreebudget.fm.search.newfilter.Order;
import com.ifreebudget.fm.services.SessionManager;
import com.ifreebudget.fm.utils.MiscUtils;

public class FilterBuilderUtils {

    private static final String TAG = "FilterBuilderUtils";
    
    private NewFilterUtils.DATE_RANGE dateRange = NewFilterUtils.DATE_RANGE.All;
    
    private Context context;
    public FilterBuilderUtils(Context context) {
        this.context = context;
    }
    
    public void setDateRange(int item) {
        switch (item) {
        case 0:
            dateRange = NewFilterUtils.DATE_RANGE.Today;
            break;
        case 1:
            dateRange = NewFilterUtils.DATE_RANGE.LastWeek;
            break;
        case 2:
            dateRange = NewFilterUtils.DATE_RANGE.LastMonth;
            break;
        case 3:
            dateRange = NewFilterUtils.DATE_RANGE.All;
        }        
    }
    
    public DATE_RANGE getDateRange() {
        return dateRange;
    }
    
    public Filter buildFilter(Intent intent) {
        Bundle bundle = intent.getExtras();
        Filter f = null;
        if (bundle != null) {
            String filter = (String) bundle.get(NewFilterUtils.FILTERKEY);

            if (filter == null) {
                if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
                    String query = intent.getStringExtra(SearchManager.QUERY);
                    f = buildSearchFilter(query);

                    /* Save to recent queries */
                    SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
                            context, TxSearchSuggestionProvider.AUTHORITY,
                            TxSearchSuggestionProvider.MODE);
                    suggestions.saveRecentQuery(query, null);
                }
            }
            else {
                if (filter
                        .equals(NewFilterUtils.ACCOUNT_FILTER_TYPE_DATERANGED)) {
                    Long accountId = bundle.getLong(NewFilterUtils.FILTERVALUE,
                            -1);
                    Long start = (Long) bundle.get(NewFilterUtils.STARTDATE);
                    Long end = (Long) bundle.get(NewFilterUtils.ENDDATE);
                    f = buildAccountFilter(accountId, start, end);
                }
                else if (filter.equals(NewFilterUtils.ACCOUNT_FILTER_TYPE)) {
                    Long accountId = bundle.getLong(NewFilterUtils.FILTERVALUE,
                            -1);
                    f = buildAccountFilter(accountId);
                }
                else if (filter.equals(NewFilterUtils.CATEGORY_FILTER_TYPE)) {
                    Long categoryId = bundle.getLong(
                            NewFilterUtils.FILTERVALUE, -1);
                    f = buildCategoryFilter(categoryId);
                }
            }
        }
        else {
            f = getDefaultFilter();
        }
        Order order = new Order("TXDATE", Order.DESC);
        f.addOrder(order);

        return f;
    }

    private Filter buildAccountFilter(Long accountId) {
        FManEntityManager dbHelper = FManEntityManager.getInstance();
        if (accountId == -1) {
            return getDefaultFilter();
        }
        Filter f = NewFilterUtils.getByAccountIdFilter(accountId);
        NewFilterUtils.addDateRangeToFilter(f, dateRange);

        try {
            Account a = dbHelper.getAccount(accountId);
            f.setName("Account: " + a.getAccountName() + " , "
                    + dateRange.toString());
        }
        catch (DBException e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
        return f;
    }

    private Filter buildSearchFilter(String query) {
        Filter f = NewFilterUtils.getBySearchQuery(query);
        NewFilterUtils.addDateRangeToFilter(f, dateRange);

        String name = query;
        if (query.length() > 15) {
            name = new StringBuilder(query.substring(0, 12)).append("...")
                    .toString();
        }
        f.setName("Search: " + name + " , " + dateRange.toString());

        return f;
    }

    private Filter buildAccountFilter(Long accountId, Long start, Long end) {
        FManEntityManager dbHelper = FManEntityManager.getInstance();
        if (accountId == -1) {
            return getDefaultFilter();
        }
        Filter f = NewFilterUtils.getByAccountIdFilter(accountId);
        NewFilterUtils.addDateRangeToFilter(f, String.valueOf(start),
                String.valueOf(end));

        try {
            Date s = new Date(start);
            Date e = new Date(end);

            SimpleDateFormat df = SessionManager.getDateFormat();
            String lbl = df.format(s) + " to " + df.format(e);
            Account a = dbHelper.getAccount(accountId);
            f.setName("Account: " + a.getAccountName() + "\n" + lbl);
        }
        catch (DBException e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
        return f;
    }

    private Filter buildCategoryFilter(Long categoryId) {
        FManEntityManager dbHelper = FManEntityManager.getInstance();
        if (categoryId == -1) {
            return getDefaultFilter();
        }

        try {
            AccountCategory a = dbHelper.getAccountCategory(categoryId);

            List<FManEntity> children = getChildren(a);
            List<String> accountIdList = new ArrayList<String>(children.size());
            if (children == null || children.size() == 0) {
                accountIdList.add(String.valueOf(Short.MIN_VALUE));
            }
            for (FManEntity e : children) {
                accountIdList.add(String.valueOf(((Account) e).getAccountId()));
            }
            Filter f = NewFilterUtils.getByAccountIdListFilter(accountIdList);
            NewFilterUtils.addDateRangeToFilter(f, dateRange);
            f.setName("Category: " + a.getCategoryName() + ", "
                    + dateRange.toString());
            return f;
        }
        catch (DBException e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
            return getDefaultFilter();
        }
    }

    private Filter getDefaultFilter() {
        Filter f = NewFilterUtils.getByDateRangeFilter(dateRange);
        f.setName(dateRange.toString());
        return f;
    }    
    
    private List<FManEntity> getChildren(AccountCategory ac) {
        ActionRequest req = new ActionRequest();
        req.setActionName("getCategoryChildren");
        req.setProperty("ACCOUNTCATEGORY", ac);

        GetCategoryChildrenAction action = new GetCategoryChildrenAction();
        try {
            ActionResponse resp = action.executeAction(req);
            if (resp.getErrorCode() == ActionResponse.NOERROR) {
                @SuppressWarnings("unchecked")
                List<FManEntity> children = (List<FManEntity>) resp
                        .getResult("CHILDREN");
                return children;
            }
            else {
                Toast toast = Toast.makeText(context,
                        resp.getErrorMessage(), Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
            Toast toast = Toast.makeText(context,
                    "Failed to get transactions", Toast.LENGTH_SHORT);
            toast.show();
        }
        return null;
    }    
}
