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
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.actions.ActionRequest;
import com.ifreebudget.fm.actions.ActionResponse;
import com.ifreebudget.fm.actions.GetExpReportAction;
import com.ifreebudget.fm.actions.GetExpReportAction.ReportItem;
import com.ifreebudget.fm.entity.beans.Account;
import com.ifreebudget.fm.entity.beans.BudgetedAccount;
import com.ifreebudget.fm.entity.beans.Transaction;
import com.ifreebudget.fm.search.FilterUtils;
import com.ifreebudget.fm.services.SessionManager;
import com.ifreebudget.fm.utils.MiscUtils;

public class ViewReportActivity extends Activity {
    private TextView reportTypeTf, totalIncTf, totalSpeTf, savingsTf, reportDateLbl;

    private MyArrayAdapter listAdapter;

    private final Handler reportItemUpdateHandler = new Handler();

    private final Handler reportUpdateHandler = new Handler();

    private final static String TAG = "ViewReportActivity";

    public final static String REPORTTYPEKEY = "REPORTTYPE";

    final NumberFormat nf = NumberFormat.getCurrencyInstance(SessionManager
            .getCurrencyLocale());

    private String reportType;

    public static final String GRAPH_BASE_URL = "http://0.chart.apis.google.com/chart?";

    private final BigDecimal HUNDRED = new BigDecimal(100d);

    private long startDate;

    private Date reportStartDt;

    private Date reportEndDt;

    /* Framework overrides */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.view_report_layout);

        reportTypeTf = (TextView) findViewById(R.id.report_title_lbl);

        totalIncTf = (TextView) findViewById(R.id.total_inc_for_period_val);

        totalSpeTf = (TextView) findViewById(R.id.total_spe_for_period_val);

        savingsTf = (TextView) findViewById(R.id.report_savings_val);

        reportDateLbl = (TextView) findViewById(R.id.report_date_lbl);

        listAdapter = new MyArrayAdapter(this, R.layout.budget_list_row);

        ListView lv = (ListView) findViewById(R.id.report_item_list_view);

        super.registerForContextMenu(lv);

        lv.setAdapter(listAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = this.getIntent();

        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null && bundle.containsKey(REPORTTYPEKEY)) {
                reportType = (String) bundle.get(REPORTTYPEKEY);
            }
        }

        String title = getString(R.string.view_report_title_lbl) + " - " + reportType;
        reportTypeTf.setText(title);

        /* Init start date to now */
        startDate = new Date().getTime();

        /* Init the list */
        listAdapter.clear();

        loadReport();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.report_menu, menu);
//        return true;
//    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.report_item_ctxt_menu, menu);
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

        ReportItem obj = (ReportItem) listAdapter.getItem(info.position);
        if (item.getItemId() == R.id.list_tx_for_acct) {
            startListTxActivity(obj);
        }

        return true;
    }

    /* End Framework overrides */

    /* Non framework methods */

    private void startListTxActivity(ReportItem entity) {

        try {
            Intent intent = new Intent(this, ListTransactionsActivity.class);
            Long accountId = entity.getAccount().getAccountId();

            intent.putExtra(FilterUtils.FILTERKEY,
                    FilterUtils.ACCOUNT_FILTER_TYPE_DATERANGED);
            intent.putExtra(FilterUtils.FILTERVALUE, accountId);
            intent.putExtra(FilterUtils.STARTDATE, reportStartDt.getTime());
            intent.putExtra(FilterUtils.ENDDATE, reportEndDt.getTime());
            startActivity(intent);
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
    }

    public void doNext(View view) {
        Calendar gc = Calendar.getInstance();
        gc.setTimeInMillis(startDate);
        if (reportType.equals("Weekly")) {
            gc.add(Calendar.DATE, 7);
        }
        else {
            gc.add(Calendar.MONTH, 1);
        }

        startDate = gc.getTimeInMillis();
        loadReport();
    }

    public void doPrevious(View view) {
        Calendar gc = Calendar.getInstance();
        gc.setTimeInMillis(startDate);
        if (reportType.equals("Weekly")) {
            gc.add(Calendar.DATE, -7);
        }
        else {
            gc.add(Calendar.MONTH, -1);
        }

        startDate = gc.getTimeInMillis();
        loadReport();
    }

    private void loadReport() {
        listAdapter.clear();
        
        Runnable r = new Runnable() {
            @Override
            public void run() {
                retrieveReport();
            }
        };

        new Thread(r).start();
    }

    private List<ReportItem> retrieveReport() {
        List<ReportItem> ret = new ArrayList<ReportItem>();
        try {
            ActionRequest req = new ActionRequest();
            req.setActionName("getExpenseReport");
            req.setProperty("DATE", new Date(startDate));
            req.setProperty("REPORTTYPE", reportType);

            final ActionResponse resp = new GetExpReportAction()
                    .executeAction(req);

            if (resp.getErrorCode() != ActionResponse.NOERROR) {
                return ret;
            }

            reportUpdateHandler.post(new Runnable() {
                @Override
                public void run() {
                    updateReportData(resp);
                }
            });

            @SuppressWarnings("unchecked")
            Collection<ReportItem> coll = (Collection<ReportItem>) resp
                    .getResult("REPORTITEMS");

            ReportItem[] arr = coll.toArray(new ReportItem[coll.size()]);

            Comparator<ReportItem> cptor = new ReportItemComparator();
            cptor = Collections.reverseOrder(cptor);

            Arrays.sort(arr, cptor);

            buildGraphUrl(arr);
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
        return ret;
    }

    private void buildGraphUrl(ReportItem[] items) {
        StringBuilder ret = new StringBuilder("<center><img src=\""
                + GRAPH_BASE_URL);
        ret.append("cht=p");
        ret.append("&");
        ret.append("chs=250x150");

        BigDecimal max = new BigDecimal(0d);
        for (ReportItem item : items) {
            if (item.getTotal().compareTo(max) > 0) {
                max = new BigDecimal(item.getTotal().doubleValue());
            }
        }

        int sz = items.length;
        int i = 0;

        StringBuilder chd = new StringBuilder();
        chd.append("&chd=t:");

        StringBuilder chl = new StringBuilder();
        chl.append("&chdl=");

        MathContext mc = new MathContext(2, RoundingMode.HALF_UP);

        for (ReportItem item : items) {
            BigDecimal tmp = item.getTotal().multiply(HUNDRED);
            tmp = tmp.divide(max, mc);
            chd.append(tmp.doubleValue());
            chl.append(item.getAccount().getAccountName());
            if (i++ < sz - 1) {
                chd.append(",");
                chl.append("|");
            }
            addReportItem(item);
        }
        ret.append(chd);
        ret.append(chl);
        ret.append("&chdlp=bv");
        ret.append("\"/></center>");
        // addGraph(ret.toString());
    }

    private void addReportItem(final ReportItem item) {
        reportItemUpdateHandler.post(new Runnable() {
            @Override
            public void run() {
                listAdapter.add(item);
            }
        });
    }

    // private void addGraph(final String data) {
    // final WebView mCharView = (WebView) findViewById(R.id.graph_view);
    // graphHandler.post(new Runnable() {
    // public void run() {
    // mCharView.loadData(data, "text/html", "utf-8");
    // }
    // });
    // }

    private void updateReportData(ActionResponse resp) {
        totalIncTf.setText(nf.format(resp.getResult("TOTALINCOME")));
        totalSpeTf.setText(nf.format(resp.getResult("TOTALSPENDING")));

        StringBuilder savings = new StringBuilder(nf.format(resp
                .getResult("SAVINGS")));
        savingsTf.setText(savings.toString());

        StringBuilder rangeStr = new StringBuilder();
        reportStartDt = (Date) resp.getResult("STARTDATE");
        reportEndDt = (Date) resp.getResult("ENDDATE");
        rangeStr.append(SessionManager.getDateFormat().format(reportStartDt));
        rangeStr.append(" to ");
        rangeStr.append(SessionManager.getDateFormat().format(reportEndDt));
        reportDateLbl.setText(rangeStr);
    }

    /* End non framework methods */

    /* Inner classes */
    class MyArrayAdapter extends ArrayAdapter<ReportItem> {

        public MyArrayAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ReportItem item = super.getItem(position);
            ReportItemView view = null;
            view = new ReportItemView(getContext(), item);
            view.setPadding(2, 5, 2, 5);
            return view;
        }
    }

    class ReportItemView extends LinearLayout {
        private ReportItem item;

        public ReportItemView(Context context) {
            super(context);
            super.setOrientation(LinearLayout.HORIZONTAL);

            String[] arr = getResources().getStringArray(
                    R.array.exp_report_table_items);

            Typeface tf = Typeface.create(Typeface.DEFAULT, Typeface.BOLD);
            initLayout(context, arr[0], arr[1], arr[2], tf);
        }

        public ReportItemView(Context context, ReportItem item) {
            super(context);
            super.setOrientation(LinearLayout.HORIZONTAL);
            this.item = item;

            CharSequence s1 = Html.fromHtml(item.toString());
            CharSequence s2 = nf.format(item.getTotal());
            CharSequence s3 = nf.format(item.getMax());

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
                    w2, LayoutParams.WRAP_CONTENT);

            //
            TextView it_details_view = new TextView(context);
            it_details_view.setTextSize(13f);
            it_details_view.setTextColor(Color.BLACK);
            it_details_view.setText(s1);
            it_details_view.setPadding(10, 5, 10, 5);
            it_details_view.setTypeface(tf);
            addView(it_details_view, params);

            TextView item_num_view = new TextView(context);
            item_num_view.setTextSize(13f);
            item_num_view.setGravity(Gravity.RIGHT);
            item_num_view.setTextColor(Color.BLACK);
            item_num_view.setText(s2);
            item_num_view.setPadding(10, 5, 10, 5);
            item_num_view.setTypeface(tf);
            item_num_view.setGravity(Gravity.RIGHT);
            addView(item_num_view, params1);

//            TextView item_min_view = new TextView(context);
//            item_min_view.setText(s3);
//            item_min_view.setTextSize(13f);
//            item_min_view.setGravity(Gravity.RIGHT);
//            item_min_view.setTextColor(Color.BLACK);
//            item_min_view.setPadding(10, 5, 10, 5);
//            item_min_view.setTypeface(tf);
//            addView(item_min_view, params1);
        }
    }

    private class ReportItemComparator implements Comparator<ReportItem> {
        @Override
        public int compare(ReportItem object1, ReportItem object2) {
            return object1.getTotal().compareTo(object2.getTotal());
        }
    }
    /* End Inner classes */
}
