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
package com.ifreebudget.fm.activities;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SearchRecentSuggestions;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.iFreeBudget;
import com.ifreebudget.fm.actions.ActionRequest;
import com.ifreebudget.fm.actions.ActionResponse;
import com.ifreebudget.fm.actions.DeleteTransactionAction;
import com.ifreebudget.fm.actions.GetCategoryChildrenAction;
import com.ifreebudget.fm.constants.AccountTypes;
import com.ifreebudget.fm.constants.AccountTypes.TransactionType;
import com.ifreebudget.fm.entity.DBException;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.Account;
import com.ifreebudget.fm.entity.beans.AccountCategory;
import com.ifreebudget.fm.entity.beans.CategoryIconMap;
import com.ifreebudget.fm.entity.beans.FManEntity;
import com.ifreebudget.fm.entity.beans.Transaction;
import com.ifreebudget.fm.search.newfilter.Filter;
import com.ifreebudget.fm.search.newfilter.NewFilterUtils;
import com.ifreebudget.fm.search.newfilter.Order;
import com.ifreebudget.fm.services.SessionManager;
import com.ifreebudget.fm.utils.MiscUtils;

public class ListTransactionsActivity extends ListActivity {

    private static final String TAG = "ListTransactionsActivity";

    private final FManEntityManager dbHelper = FManEntityManager.getInstance();

    private ArrayAdapter<TxHolder> txListAdapter;

    private final Handler txListUpdateHandler = new Handler();

    final NumberFormat nf = NumberFormat.getCurrencyInstance(SessionManager
            .getCurrencyLocale());

    public static NewFilterUtils.DATE_RANGE dateRange = NewFilterUtils.DATE_RANGE.LastWeek;

    private TextView footerLbl = null;

    private BigDecimal totalValue = null;

    private Button filterButton = null;

    private View lastEditCtrlPanel = null;
    
    private TxHolder lastSelectedTx = null;

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Object obj = this.getListAdapter().getItem(position);
        if (obj instanceof TxHolder) {
            if (lastEditCtrlPanel != null) {
                lastEditCtrlPanel.setVisibility(View.GONE);
            }
            lastEditCtrlPanel = (View) v.findViewById(R.id.edit_tx_ctrl_panel);
            lastEditCtrlPanel.setVisibility(View.VISIBLE);
            lastSelectedTx = (TxHolder) obj;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.tx_list_layout);
        super.registerForContextMenu(getListView());

        /* Initialize state variables */
        if (lastEditCtrlPanel != null) {
            lastEditCtrlPanel.setVisibility(View.GONE);
        }
        lastSelectedTx = null;
        /* End Initialize state variables */
        
        txListAdapter = new MyArrayAdapter(this, R.layout.tx_list_layout);
        this.setListAdapter(txListAdapter);

        footerLbl = (TextView) findViewById(R.id.tx_footer_lbl);
        filterButton = (Button) findViewById(R.id.filter_button);
    }

    @Override
    public void onResume() {
        super.onResume();
        totalValue = new BigDecimal(0d);
        retrieveTxList(1, 25, buildFilter());
        setFilterButtonText();
    }

    /* Menu handler functions */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tx_ctxt_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        }
        catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return true;
        }

        TxHolder obj = (TxHolder) getListAdapter().getItem(info.position);
        if (item.getItemId() == R.id.edit_item) {
            doEditAction(obj);
        }
        else if (item.getItemId() == R.id.delete_item) {
            doDeleteAction(obj);
        }
        else if (item.getItemId() == R.id.reminder_item) {
            doReminderAction(obj);
        }
        return true;
    }

    private void doEditAction(TxHolder entity) {
        Transaction a = entity.t;
        Intent intent = new Intent(this, UpdateTransactionActivity.class);
        intent.putExtra(UpdateTransactionActivity.TXID, a.getTxId());
        startActivity(intent);
    }

    private void doReminderAction(TxHolder entity) {
        Transaction a = entity.t;
        Intent intent = new Intent(this, AddReminderActivity.class);
        intent.putExtra(UpdateTransactionActivity.TXID, a.getTxId());
        startActivity(intent);
    }

    private void doDeleteAction(TxHolder entity) {
        Transaction a = entity.t;
        try {
            ActionRequest req = new ActionRequest();
            req.setActionName("deleteTransactionAction");
            req.setProperty("TXID", a.getTxId());

            ActionResponse resp = new DeleteTransactionAction()
                    .executeAction(req);
            if (resp.getErrorCode() == ActionResponse.NOERROR) {
                Intent intent = getIntent();
                overridePendingTransition(0, 0);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                finish();

                overridePendingTransition(0, 0);
                startActivity(intent);
            }
            else {
                Toast toast = Toast.makeText(getApplicationContext(),
                        resp.getErrorMessage(), Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
        }
        catch (Exception e) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Unable to delete transaction", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tx_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.home_menu:
            gotoHomeScreen();
            break;
        case R.id.add_tx_menu:
            addTransaction();
            break;
        default:
            return true;
        }

        return true;
    }

    /* End Menu handler functions */

    private void addToUI(TxHolder tx) {
        txListAdapter.add(tx);
        setTotalValue(totalValue);
    }

    private Filter buildFilter() {
        Intent intent = getIntent();
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
                            this, TxSearchSuggestionProvider.AUTHORITY,
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
                Toast toast = Toast.makeText(getApplicationContext(),
                        resp.getErrorMessage(), Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Failed to get transactions", Toast.LENGTH_SHORT);
            toast.show();
        }
        return null;
    }

    private void retrieveTxList(int offset, int limit, final Filter query) {
        TextView filterView = (TextView) findViewById(R.id.tx_list_filter_lbl);
        filterView.setText(query.getName());

        /* clear out the display */
        txListAdapter.clear();
        totalValue = new BigDecimal(0);
        setTotalValue(totalValue);

        Runnable r = new Runnable() {
            public void run() {
                List<FManEntity> catgs;
                try {
                    try {
                        String q = query.getQueryObject(false);
                        catgs = dbHelper.executeFilterQuery(q,
                                Transaction.class);
                    }
                    catch (Exception e1) {
                        Log.e(TAG, MiscUtils.stackTrace2String(e1));
                        catgs = dbHelper.getTransactions(0, 0);
                    }

                    for (FManEntity e : catgs) {
                        Transaction t = (Transaction) e;

                        final TxHolder tx = new TxHolder(t);
                        Account from = dbHelper
                                .getAccount(t.getFromAccountId());
                        Account to = dbHelper.getAccount(t.getToAccountId());

                        int iconRes = 0;
                        if (t.getTxStatus() == AccountTypes.TX_STATUS_PENDING) {
                            iconRes = R.drawable.pending;
                        }
                        else {
                            CategoryIconMap iconMap = dbHelper
                                    .getCategoryIconMap(from.getCategoryId());
                            if (iconMap == null) {
                                iconMap = dbHelper.getCategoryIconMap(to
                                        .getCategoryId());
                            }

                            if (iconMap != null) {
                                iconRes = getResources().getIdentifier(
                                        iconMap.getIconPath(), "drawable",
                                        "com.ifreebudget.fm");
                            }
                            else {
                                iconRes = R.drawable.blank;
                            }
                        }

                        tx.fromAcct = from;
                        tx.toAcct = to;
                        tx.iconResource = iconRes;

                        totalValue = totalValue.add(tx.t.getTxAmount());

                        txListUpdateHandler.post(new Runnable() {
                            public void run() {
                                addToUI(tx);
                            }
                        });
                    }
                }
                catch (DBException e) {
                    Log.e(TAG, MiscUtils.stackTrace2String(e));
                }
            }
        };

        new Thread(r).start();
    }

    public void gotoHomeScreen() {
        Intent intent = new Intent(this, iFreeBudget.class);
        startActivity(intent);
    }

    public void gotoHomeScreen(View view) {
        gotoHomeScreen();
    }

    public void addTransaction(View view) {
        addTransaction();
    }

    public void addTransaction() {
        Intent txIntent = new Intent(this, AddTransactionActivity.class);
        startActivity(txIntent);
    }

    private void setTotalValue(BigDecimal value) {
        footerLbl.setText("Total: " + nf.format(value));
    }

    public void showFilterSelector(View view) {
        Resources res = getResources();
        final CharSequence[] items = { res.getString(R.string.today_filter),
                res.getString(R.string.last_week_filter),
                res.getString(R.string.last_month_filter),
                res.getString(R.string.all_fltr) };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select filter");
        builder.setSingleChoiceItems(items, -1,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
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

                        filterButton.setText(items[item]);
                        retrieveTxList(1, 25, buildFilter());
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    private void setFilterButtonText() {
        Resources res = getResources();

        String text = "Select";
        if (dateRange != null) {
            switch (dateRange) {
            case Today:
                text = res.getString(R.string.today_filter);
                break;
            case LastWeek:
                text = res.getString(R.string.last_week_filter);
                break;
            case LastMonth:
                text = res.getString(R.string.last_month_filter);
                break;
            case All:
                text = res.getString(R.string.all_fltr);
            }
        }
        filterButton.setText(text);
    }

    class TxHolder {
        Transaction t;
        int iconResource;
        Account fromAcct;
        Account toAcct;
        String displayTxt = null;
        String date = null;

        TxHolder(Transaction t) {
            this.t = t;
            date = SessionManager.getDateFormat().format(
                    new Date(t.getTxDate()));
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Long.valueOf(t.getTxId()).hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            TxHolder other = (TxHolder) obj;
            return t.getTxId() == other.t.getTxId();
        }

        @Override
        public String toString() {
            if (displayTxt != null) {
                return displayTxt;
            }

            TransactionType type = AccountTypes.getTransactionType(
                    fromAcct.getAccountType(), toAcct.getAccountType());

            StringBuilder ret = new StringBuilder();
            ret.append("<b><i>");

            if (type == TransactionType.Income) {
                ret.append(fromAcct.getAccountName());
                ret.append("</i></b>");
                ret.append("<br>");
                ret.append(toAcct.getAccountName());
            }
            else if (type == TransactionType.Expense) {
                ret.append(toAcct.getAccountName());
                ret.append("</i></b>");
                ret.append("<br>");
                ret.append(fromAcct.getAccountName());
            }
            else {
                ret.append(fromAcct.getAccountName());
                ret.append("</i></b>");
                ret.append("<br>");
                ret.append(toAcct.getAccountName());
            }
            ret.append("<br><i>");
            ret.append(date);
            ret.append("</i>");
            return ret.toString();
        }
    }

    class MyArrayAdapter extends ArrayAdapter<TxHolder> {

        public MyArrayAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TxHolder item = super.getItem(position);
            // TxItemView view = new TxItemView(getContext(), item);
            View v = getLayoutInflater().inflate(R.layout.tx_item_view, null);
            initializeTxItemLayout(item, v);
            return v;
        }
    }

    private void initializeTxItemLayout(TxHolder item, View v) {
        if (item.iconResource != 0) {
            ImageView iv = (ImageView) v.findViewById(R.id.tx_icon);
            iv.setImageResource(item.iconResource);
            iv.setAdjustViewBounds(true);
        }
        Spanned txDetails = Html.fromHtml(item.toString());

        TextView txDetailsView = (TextView) v.findViewById(R.id.tx_details_lbl);
        txDetailsView.setText(txDetails);

        String txAmt = nf.format(item.t.getTxAmount());
        TextView txAmtView = (TextView) v.findViewById(R.id.tx_amt_lbl);
        txAmtView.setText(txAmt);
    }

    private BigDecimal calculateTotalAmount() {
        int sz = txListAdapter.getCount();
        BigDecimal ret = new BigDecimal(0d);
        for (int i = 0; i < sz; i++) {
            ret = ret.add(txListAdapter.getItem(i).t.getTxAmount());
        }
        return ret;
    }

    /* Button click handlers */
    public void editTransaction(View view) {
        if(lastSelectedTx == null) {
            return;
        }
        doEditAction(lastSelectedTx);        
    }
    
    public void deleteTransaction(View view) {
        try {
            if(lastSelectedTx == null) {
                return;
            }
            Long txId = lastSelectedTx.t.getTxId();

            ActionRequest req = new ActionRequest();
            req.setActionName("deleteTransactionAction");
            req.setProperty("TXID", txId);

            ActionResponse resp = new DeleteTransactionAction()
                    .executeAction(req);
            if (resp.getErrorCode() == ActionResponse.NOERROR) {
                txListAdapter.remove(lastSelectedTx);
            }
            else {
                Toast toast = Toast.makeText(getApplicationContext(),
                        resp.getErrorMessage(), Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
        }
        catch (Exception e) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Unable to delete transaction", Toast.LENGTH_SHORT);
            Log.e(TAG, MiscUtils.stackTrace2String(e));
            toast.show();
            return;
        }
        finally {
            lastEditCtrlPanel = null;
            lastSelectedTx = null;
            setTotalValue(calculateTotalAmount());
        }
    }
    /* End Button click handlers */
}
