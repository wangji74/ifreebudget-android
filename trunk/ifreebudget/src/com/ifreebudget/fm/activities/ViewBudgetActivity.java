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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.iFreeBudget;
import com.ifreebudget.fm.actions.ActionRequest;
import com.ifreebudget.fm.actions.ActionResponse;
import com.ifreebudget.fm.actions.GetBudgetSummaryAction;
import com.ifreebudget.fm.entity.DBException;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.Budget;
import com.ifreebudget.fm.entity.beans.BudgetedAccount;
import com.ifreebudget.fm.search.newfilter.NewFilterUtils;
import com.ifreebudget.fm.services.SessionManager;
import com.ifreebudget.fm.utils.MiscUtils;

public class ViewBudgetActivity extends ListActivity {
    private TextView budgetNameTf;
    private TextView budgetAllocTf;
    private TextView budgetActualTf;
    private TextView budgetSavingsTf;
    private TextView budgetReportDateLbl;

    private static final String TAG = "ViewBudgetActivity";

    public static final String BUDGETIDKEY = "BUDGETIDKEY";

    final NumberFormat nf = NumberFormat.getCurrencyInstance(SessionManager
            .getCurrencyLocale());

    private long startDate;

    private Budget budget;

    private MyArrayAdapter listAdapter;
    
    private Date budgetStartDt;

    private Date budgetEndDt;    

    public void doNext(View view) {
        Calendar gc = Calendar.getInstance();
        gc.setTimeInMillis(startDate);
        if (budget.getType() == Budget.WEEKLY) {
            gc.add(Calendar.DATE, 7);
        }
        else if (budget.getType() == Budget.BIWEEKLY) {
            gc.add(Calendar.DATE, 14);
        }
        else {
            gc.add(Calendar.MONTH, 1);
        }

        startDate = gc.getTimeInMillis();
        loadBudgetSummary();
    }

    public void doPrevious(View view) {
        Calendar gc = Calendar.getInstance();
        gc.setTimeInMillis(startDate);
        if (budget.getType() == Budget.WEEKLY) {
            gc.add(Calendar.DATE, -7);
        }
        else if (budget.getType() == Budget.BIWEEKLY) {
            gc.add(Calendar.DATE, -14);
        }
        else {
            gc.add(Calendar.MONTH, -1);
        }

        startDate = gc.getTimeInMillis();
        loadBudgetSummary();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.budget_viewer_menu, menu);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.budget_ctxt_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
        case R.id.del_budget:
            deleteBudget();
            return true;
        case R.id.mItemHome:
            startHomeActivity();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    private void startHomeActivity() {
        Intent intent = new Intent(this, iFreeBudget.class);
        startActivity(intent);
    }

    private void deleteBudget() {
        FManEntityManager em = FManEntityManager.getInstance();
        em.beginTransaction();
        try {
            em.setTransactionSuccessful();
            em.deleteBudget(budget.getId());
            super.finish();
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
        finally {
            em.endTransaction();
        }
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

        BudgetedAccount obj = (BudgetedAccount) getListAdapter().getItem(
                info.position);
        if (item.getItemId() == R.id.list_tx_for_acct) {
            startListTxActivity(obj);
        }
        else if (item.getItemId() == R.id.remove_acct_from_budget) {
            removeBudgetedAccount(obj);
        }

        return true;
    }

    private void startListTxActivity(BudgetedAccount entity) {

        try {
            Intent intent = new Intent(this, ListTransactionsActivity.class);
            Long accountId = entity.getAccountId();

            intent.putExtra(NewFilterUtils.FILTERKEY,
                    NewFilterUtils.ACCOUNT_FILTER_TYPE_DATERANGED);
            intent.putExtra(NewFilterUtils.FILTERVALUE, accountId);
            intent.putExtra(NewFilterUtils.STARTDATE, budgetStartDt.getTime());
            intent.putExtra(NewFilterUtils.ENDDATE, budgetEndDt.getTime());
            startActivity(intent);            
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.view_budget_layout);
        super.registerForContextMenu(getListView());

        budgetNameTf = (TextView) findViewById(R.id.title_lbl);

        budgetAllocTf = (TextView) findViewById(R.id.budget_alloc_val);

        budgetActualTf = (TextView) findViewById(R.id.budget_actual_val);

        budgetSavingsTf = (TextView) findViewById(R.id.budget_savings_val);

        budgetReportDateLbl = (TextView) findViewById(R.id.breport_date_lbl);

        listAdapter = new MyArrayAdapter(this, R.layout.budget_list_row);
        setListAdapter(listAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        FManEntityManager em = FManEntityManager.getInstance();

        Intent intent = getIntent();

        long budgetId = 0;
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null && bundle.containsKey(BUDGETIDKEY)) {
                budgetId = (Long) bundle.get(BUDGETIDKEY);
            }
            startDate = new Date().getTime();
        }
        if (budgetId == 0) {
            return;
        }

        try {
            budget = em.getBudget(budgetId);
            loadBudgetSummary();
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Error getting budget details", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
    }

    private void loadBudgetSummary() {
        try {
            ActionResponse resp = getBudgetSummary(budget);
            if (resp != null && resp.getErrorCode() == ActionResponse.NOERROR) {
                initializeFields(resp);
            }
            else {
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Error getting budget details", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
        }
        catch (Exception e) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Error getting budget details", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private ActionResponse getBudgetSummary(Budget b) throws Exception {
        ActionRequest req = new ActionRequest();
        req.setActionName("getBudgetSummary");
        req.setProperty("BUDGET", b);
        req.setProperty("DATE", new Date(startDate));

        GetBudgetSummaryAction action = new GetBudgetSummaryAction();
        ActionResponse resp = action.executeAction(req);
        return resp;
    }

    @SuppressWarnings("unchecked")
    private void initializeFields(ActionResponse resp) {
        listAdapter.clear();
        BudgetedAccount dummy = new BudgetedAccount();
        listAdapter.add(dummy);
        
        Budget b = (Budget) resp.getResult("BUDGET");
        List<BudgetedAccount> baList = (List<BudgetedAccount>) resp
                .getResult("BUDGETEDACCOUNTLIST");

        budgetNameTf.setText(b.getName() + " ("
                + Budget.getTypeAsString(b.getType()) + ")");

        budgetStartDt = (Date) resp.getResult("STARTDATE");
        budgetEndDt = (Date) resp.getResult("ENDDATE");

        SimpleDateFormat df = SessionManager.getDateFormat();
        String msg = df.format(budgetStartDt) + " to " + df.format(budgetEndDt);
        budgetReportDateLbl.setText(msg);

        BigDecimal totalAlloc = new BigDecimal(0d);
        BigDecimal totalActual = new BigDecimal(0d);

        for (BudgetedAccount ba : baList) {
            listAdapter.add(ba);
            totalAlloc = totalAlloc.add(ba.getAllocatedAmount());
            totalActual = totalActual.add(ba.getActualAmount());
        }

        BigDecimal savings = totalAlloc.subtract(totalActual);

        budgetAllocTf.setText(nf.format(totalAlloc));

        budgetActualTf.setText(nf.format(totalActual));

        budgetSavingsTf.setText(nf.format(savings));

        if (savings.doubleValue() < 0d) {
            budgetSavingsTf.setTextColor(Color.RED);
        }
        else {
            budgetSavingsTf.setTextColor(Color.BLACK);
        }
    }

    public void doCancelAction(View view) {
        super.finish();
    }

    private void removeBudgetedAccount(BudgetedAccount acct) {
        FManEntityManager em = FManEntityManager.getInstance();
        try {
            em.deleteEntity(acct);
            loadBudgetSummary();
        }
        catch (DBException e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
    }

    class MyArrayAdapter extends ArrayAdapter<BudgetedAccount> {

        public MyArrayAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            BudgetedAccount item = super.getItem(position);
            BudgetItemView view = null;
            if (position == 0) {
                view = new BudgetItemView(getContext());
            }
            else {
                view = new BudgetItemView(getContext(), item);
            }
            view.setPadding(2, 5, 2, 5);
            return view;
        }
    }

    class BudgetItemView extends LinearLayout {
        private BudgetedAccount item;

        public BudgetItemView(Context context) {
            super(context);
            super.setOrientation(LinearLayout.HORIZONTAL);

            String[] arr = getResources().getStringArray(
                    R.array.budget_report_table_items);

            Typeface tf = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
            initLayout(context, arr[0], arr[1], arr[2], tf);
        }

        public BudgetItemView(Context context, BudgetedAccount item) {
            super(context);
            super.setOrientation(LinearLayout.HORIZONTAL);
            this.item = item;

            CharSequence s1 = Html.fromHtml(item.toString());
            CharSequence s2 = nf.format(item.getAllocatedAmount());
            CharSequence s3 = nf.format(item.getActualAmount());

            Typeface tf = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);
            initLayout(context, s1, s2, s3, tf);
        }

        private void initLayout(Context context, CharSequence s1,
                CharSequence s2, CharSequence s3, Typeface tf) {

            Display display = getWindowManager().getDefaultDisplay();
            int width = display.getWidth();

            int w1 = width / 2;
            int w2 = width - w1;

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    w1, LayoutParams.WRAP_CONTENT);

            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                    w2 / 2, LayoutParams.WRAP_CONTENT);

            //
            TextView item_name_view = new TextView(context);
            item_name_view.setTextSize(13f);
            item_name_view.setTextColor(Color.BLACK);
            item_name_view.setText(s1);
            item_name_view.setPadding(10, 5, 10, 5);
            item_name_view.setTypeface(tf);
            addView(item_name_view, params);

            TextView item_alloc_view = new TextView(context);
            item_alloc_view.setTextSize(13f);
            item_alloc_view.setGravity(Gravity.RIGHT);
            item_alloc_view.setTextColor(Color.BLACK);
            item_alloc_view.setText(s2);
            item_alloc_view.setPadding(10, 5, 10, 5);
            item_alloc_view.setTypeface(tf);
            addView(item_alloc_view, params1);

            TextView item_actual_view = new TextView(context);
            int color = Color.BLACK;
            if (item != null
                    && item.getActualAmount().compareTo(
                            item.getAllocatedAmount()) > 0) {
                color = Color.RED;
            }
            item_actual_view.setText(s3);
            item_actual_view.setTextSize(13f);
            item_actual_view.setGravity(Gravity.RIGHT);
            item_actual_view.setTextColor(color);
            item_actual_view.setPadding(10, 5, 10, 5);
            item_actual_view.setTypeface(tf);
            addView(item_actual_view, params1);
        }
    }
}
