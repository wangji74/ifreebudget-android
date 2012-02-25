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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SearchRecentSuggestions;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.iFreeBudget;
import com.ifreebudget.fm.actions.ActionRequest;
import com.ifreebudget.fm.actions.ActionResponse;
import com.ifreebudget.fm.actions.DeleteTransactionAction;
import com.ifreebudget.fm.actions.GetCategoryChildrenAction;
import com.ifreebudget.fm.activities.utils.DialogCallback;
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

public class ListTransactionsActivity extends Activity {

    private static final String TAG = "ListTransactionsActivity";

    private static final int PAGE_SIZE = 25;

    private final FManEntityManager dbHelper = FManEntityManager.getInstance();

    private ListView listView;

    private ArrayAdapter<TxHolder> txListAdapter;

    private final Handler txListUpdateHandler = new Handler();

    final NumberFormat nf = NumberFormat.getCurrencyInstance(SessionManager
            .getCurrencyLocale());

    public static NewFilterUtils.DATE_RANGE dateRange = NewFilterUtils.DATE_RANGE.LastWeek;

    private TextView footerLbl = null;

    private Button filterButton = null;

    private TxHolder lastSelectedTx = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.tx_list_layout);

        /* Initialize state variables */
        lastSelectedTx = null;
        /* End Initialize state variables */

        txListAdapter = new MyArrayAdapter(this, R.layout.tx_list_layout);

        listView = (ListView) findViewById(R.id.list_panel);
        listView.setAdapter(txListAdapter);
        registerForContextMenu(listView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                lastSelectedTx = txListAdapter.getItem(position);
                showDialog(VIEW_TX_DIALOG);
            }
        });

        listView.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                    int visibleItemCount, int totalItemCount) {

                if (visibleItemCount == 0) {
                    return;
                }
                if (retrieveInProgress) {
                    return;
                }
                boolean loadMore = firstVisibleItem + visibleItemCount >= totalItemCount;
                if (loadMore) {
                    retrieveTxList(buildFilter());
                }
            }
        });

        footerLbl = (TextView) findViewById(R.id.tx_footer_lbl);
        filterButton = (Button) findViewById(R.id.filter_button);
    }

    @Override
    public void onResume() {
        super.onResume();
        setFilterButtonText();
        resetState();
        retrieveTxList(buildFilter());
    }

    private void resetState() {
        /* clear out the display */
        txListAdapter.clear();
        off.set(0);
        end.set(false);
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

    public void doDeleteTransaction(TxHolder holder) {
        try {
            if (holder == null) {
                return;
            }
            Long txId = holder.t.getTxId();

            ActionRequest req = new ActionRequest();
            req.setActionName("deleteTransactionAction");
            req.setProperty("TXID", txId);

            ActionResponse resp = new DeleteTransactionAction()
                    .executeAction(req);
            if (resp.getErrorCode() == ActionResponse.NOERROR) {
                txListAdapter.remove(holder);
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
            lastSelectedTx = null;
        }
    }

    private static final int VIEW_TX_DIALOG = 1;

    @Override
    public Dialog onCreateDialog(int id) {
        switch (id) {
        case VIEW_TX_DIALOG:
            return getViewTxDialog();
        }
        return null;
    }

    @Override
    public void onPrepareDialog(int id, Dialog dialog, Bundle args) {
        super.onPrepareDialog(id, dialog, args);
        switch (id) {
        case VIEW_TX_DIALOG:
            ViewTxDialog d = (ViewTxDialog) dialog;
            d.initialize(lastSelectedTx);
        }
    }

    private Dialog getViewTxDialog() {
        Context c = ListTransactionsActivity.this;
        ViewTxDialog dialog = new ViewTxDialog(c, new DialogCallback() {

            @Override
            public void onReturn(int code, Object result) {
            }

            @Override
            public void onDismiss(int code, Object context) {
                TxHolder holder = (TxHolder) context;
                switch (code) {
                case ViewTxDialog.EDIT_TX:
                    doEditAction(holder);
                    break;
                case ViewTxDialog.ADD_REMINDER:
                    doReminderAction(holder);
                    break;
                case ViewTxDialog.DELETE_TX:
                    doDeleteTransaction(holder);
                    break;
                }
            }

        });

        return dialog;
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

    private boolean retrieveInProgress = false;
    private final AtomicInteger off = new AtomicInteger(0);
    private final AtomicBoolean end = new AtomicBoolean(false);

    private void retrieveTxList(final Filter query) {
        TextView filterView = (TextView) findViewById(R.id.tx_list_filter_lbl);
        filterView.setText(query.getName());
        if (end.get()) {
            return;
        }
        Runnable r = new Runnable() {
            public void run() {
                List<FManEntity> catgs;
                retrieveInProgress = true;
                try {
                    try {
//                        Log.i(TAG,
//                                "Retr tx list: " + off.get() + "," + end.get());
                        String q = query.getQueryObject(false);
                        catgs = dbHelper.executeFilterQuery(q,
                                Transaction.class, off.get(), PAGE_SIZE);
                        off.addAndGet(catgs.size());
                        if (catgs.size() < PAGE_SIZE) {
                            end.set(true);
                        }
                    }
                    catch (Exception e1) {
                        Log.e(TAG, MiscUtils.stackTrace2String(e1));
                        catgs = dbHelper.getTransactions(0, 0);
                    }
                    for (FManEntity e : catgs) {
                        Transaction t = (Transaction) e;
                        final TxHolder tx = makeTxHolder(t);
                        txListUpdateHandler.post(new Runnable() {
                            public void run() {
                                addToUI(tx);
                            }
                        });
                    }
                }
                catch (Exception e) {
                    Log.e(TAG, MiscUtils.stackTrace2String(e));
                }
                finally {
                    retrieveInProgress = false;
                }
            }
        };

        new Thread(r).start();
    }

    private TxHolder makeTxHolder(Transaction t) throws Exception {
        TxHolder tx = new TxHolder(t);
        Account from = dbHelper.getAccount(t.getFromAccountId());
        Account to = dbHelper.getAccount(t.getToAccountId());

        int iconRes = 0;
        if (t.getTxStatus() == AccountTypes.TX_STATUS_PENDING) {
            iconRes = R.drawable.pending;
        }
        else {
            CategoryIconMap iconMap = dbHelper.getCategoryIconMap(from
                    .getCategoryId());
            if (iconMap == null) {
                iconMap = dbHelper.getCategoryIconMap(to.getCategoryId());
            }

            if (iconMap != null) {
                iconRes = getResources().getIdentifier(iconMap.getIconPath(),
                        "drawable", "com.ifreebudget.fm");
            }
            else {
                iconRes = R.drawable.blank;
            }
        }

        tx.fromAcct = from;
        tx.toAcct = to;
        tx.iconResource = iconRes;

        return tx;
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
                        resetState();
                        retrieveTxList(buildFilter());
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
            ret.append("<b>");

            if (type == TransactionType.Income) {
                ret.append(fromAcct.getAccountName());
                ret.append("</b>");
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
            View v = convertView;
            if (v == null) {
                v = getLayoutInflater().inflate(R.layout.tx_item_view, null);
            }
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
}
