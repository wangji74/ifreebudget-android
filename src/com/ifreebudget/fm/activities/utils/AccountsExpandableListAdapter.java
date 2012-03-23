package com.ifreebudget.fm.activities.utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.entity.DBException;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.Account;
import com.ifreebudget.fm.entity.beans.AccountCategory;
import com.ifreebudget.fm.entity.beans.BudgetedAccount;
import com.ifreebudget.fm.entity.beans.FManEntity;
import com.ifreebudget.fm.services.SessionManager;
import com.ifreebudget.fm.utils.MiscUtils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class AccountsExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<AccountCategory> groups;
    private Map<AccountCategory, List<BudgetedAccount>> entries;

    public AccountsExpandableListAdapter(Context context) {
        this.context = context;
        groups = new ArrayList<AccountCategory>();
        entries = new LinkedHashMap<AccountCategory, List<BudgetedAccount>>();
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        if (groupPosition >= groups.size()) {
            return null;
        }
        AccountCategory cal = groups.get(groupPosition);
        List<BudgetedAccount> list = entries.get(cal);
        if (list == null || childPosition >= list.size()) {
            return null;
        }
        return list.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        BudgetedAccount entry = (BudgetedAccount) getChild(groupPosition, childPosition);
        if (entry == null) {
            return 0l;
        }
        return entry.getAccountId();
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
        final BudgetedAccount item = (BudgetedAccount) getChild(groupPosition,
                childPosition);

        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.add_budget_list_item_layout, null);
        }
        initializeItemLayout(item, v);
        return v;
    }

    private void initializeItemLayout(BudgetedAccount item, View v) {
        String txt = item.getAccountName();
        TextView nameTf = (TextView) v.findViewById(R.id.acct_details_lbl);
        TextView amtTf = (TextView) v.findViewById(R.id.budget_amt_lbl);

        String val = NumberFormat.getCurrencyInstance(
                SessionManager.getCurrencyLocale()).format(
                item.getAllocatedAmount());

        nameTf.setText(txt);
        amtTf.setText(val);
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        List<BudgetedAccount> entries = getChildObjects(groupPosition);
        if (entries == null) {
            return 0;
        }
        return entries.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        if (groupPosition >= groups.size()) {
            return null;
        }
        return groups.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        if (groupPosition >= groups.size()) {
            return 0;
        }
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT, 64);

        TextView tv = new TextView(this.context);
        tv.setLayoutParams(lp);

        tv.setGravity(Gravity.CENTER_VERTICAL);
        tv.setPadding(15, 2, 5, 2);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setTextColor(Color.BLACK);
        if (groupPosition < groups.size()) {
            AccountCategory ac = groups.get(groupPosition);
            tv.setText(ac.getCategoryName());
        }
        // int color = context.getResources().getColor(R.colo);
        // tv.setBackgroundColor(color);
        return tv;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private List<BudgetedAccount> getChildObjects(int groupPosition) {
        if (groupPosition >= groups.size()) {
            return null;
        }
        AccountCategory ac = groups.get(groupPosition);
        return entries.get(ac);
    }

    public void setData(List<FManEntity> list) {
        entries.clear();
        groups.clear();

        for (FManEntity e : list) {
            Account a = (Account) e;
            AccountCategory ac = getCategory(a.getCategoryId());
            if (ac == null) {
                continue;
            }
            List<BudgetedAccount> values = entries.get(ac);
            if (values == null) {
                values = new ArrayList<BudgetedAccount>();
                entries.put(ac, values);
                groups.add(ac);
            }
            BudgetedAccount ba = new BudgetedAccount();
            ba.setAccountName(a.getAccountName());
            ba.setAllocatedAmount(new BigDecimal(0d));
            ba.setAccountId(a.getAccountId());

            values.add(ba);
        }
    }

    private AccountCategory getCategory(long catId) {
        FManEntityManager em = FManEntityManager.getInstance();
        try {
            return em.getAccountCategory(catId);
        }
        catch (DBException e) {
            Log.i(TAG, MiscUtils.stackTrace2String(e));
            return null;
        }
    }

    private static final String TAG = "AccountsExpandableListAdapter";
}
