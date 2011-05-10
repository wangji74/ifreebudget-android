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
package com.ifreebudget.fm;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ifreebudget.fm.actions.ActionRequest;
import com.ifreebudget.fm.actions.ActionResponse;
import com.ifreebudget.fm.actions.GetNetWorthAction;
import com.ifreebudget.fm.activities.ListTransactionsActivity;
import com.ifreebudget.fm.activities.ManageAccountsActivity;
import com.ifreebudget.fm.activities.ManageBudgetsActivity;
import com.ifreebudget.fm.activities.ManageDBActivity;
import com.ifreebudget.fm.activities.QuickAddTransactionActivity;
import com.ifreebudget.fm.activities.ViewReportActivity;
import com.ifreebudget.fm.constants.AccountTypes;
import com.ifreebudget.fm.entity.DBException;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.Account;
import com.ifreebudget.fm.entity.beans.FManEntity;
import com.ifreebudget.fm.entity.beans.TxHistory;
import com.ifreebudget.fm.services.SessionManager;
import com.ifreebudget.fm.utils.MiscUtils;

public class iFreeBudget extends Activity {
    public static final String PARENTCATEGORYIDKEY = "PARENTCATEGORYID";
    public static final String ACCOUNTIDKEY = "ACCOUNTID";
    public static final String CATEGORYIDKEY = "CATEGORYID";

    private static final String TAG = "iFreeBudget";

    private MyArrayAdapter listAdapter;

    private String MARKET_URI = "market://details?id=com.ifreebudget.fm";

    private ProgressDialog dialog = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
        case R.id.mItemReports:
            gotoReports();
            return true;
        case R.id.mItemDbManager:
            gotoDBManager();
            return true;
        case R.id.mItemClear:
            clearAll();
            return true;
            // case R.id.mItemRate:
            // gotoMarket();
            // return true;
            // case R.id.mItemAbout:
            // return true;
        default:
            return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.main);

        listAdapter = new MyArrayAdapter(this, R.layout.shortcut_item_layout);

        ListView lv = (ListView) findViewById(R.id.sc_list);
        lv.setAdapter(listAdapter);
        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view,
                    int position, long id) {

                Shortcut item = (Shortcut) adapter.getItemAtPosition(position);
                startAddTxActivity(item.item);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        FManEntityManager.getInstance(this);
        Log.i(TAG, "DBHelper created.");

        ActionRequest req = new ActionRequest();

        try {
            ActionResponse resp = new GetNetWorthAction().executeAction(req);
            if (resp.getErrorCode() == ActionResponse.NOERROR) {
                initializeFields(resp);
            }
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
    }

    private void initializeFields(ActionResponse resp) {
        NumberFormat numberFormat = NumberFormat
                .getCurrencyInstance(SessionManager.getCurrencyLocale());

        TextView nameTf = (TextView) findViewById(R.id.net_assets_val_lbl);
        BigDecimal assetVal = (BigDecimal) resp.getResult("ASSET_VALUE");
        nameTf.setText(numberFormat.format(assetVal));

        TextView liabsTf = (TextView) findViewById(R.id.net_liabs_val_lbl);
        BigDecimal liabsVal = (BigDecimal) resp.getResult("LIAB_VALUE");
        liabsTf.setText(numberFormat.format(liabsVal));

        TextView nwTf = (TextView) findViewById(R.id.net_worth_val_lbl);
        BigDecimal nwVal = (BigDecimal) resp.getResult("NET_VALUE");
        nwTf.setText(numberFormat.format(nwVal));

        initializeShortcuts();
    }

    private void initializeShortcuts() {
        listAdapter.clear();

        FManEntityManager em = FManEntityManager.getInstance();
        try {
            List<FManEntity> sList = em.getTxHistoryShortcutList();
//            Log.i(TAG, String.valueOf(sList.size()));

            TextView lbl = (TextView) findViewById(R.id.sc_title);
            if (sList == null || sList.size() == 0) {
                lbl.setVisibility(View.INVISIBLE);
            }
            else {
                lbl.setVisibility(View.VISIBLE);
            }

            for (FManEntity e : sList) {
                addShortcut((TxHistory) e, em);
            }
        }
        catch (DBException e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
    }

    private void addShortcut(TxHistory entity, FManEntityManager em) {
        try {
            Account f = em.getAccount(entity.getFromAccountId());
            Account t = em.getAccount(entity.getToAccountId());
            Shortcut sc = new Shortcut(entity, f, t);
            listAdapter.add(sc);
        }
        catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void manageAccounts(View view) {
        Intent intent = new Intent(this, ManageAccountsActivity.class);
        startActivity(intent);
    }

    public void manageTransactions(View view) {
        Intent intent = new Intent(this, ListTransactionsActivity.class);
        startActivity(intent);
    }

    public void manageBudgets(View view) {
        Intent intent = new Intent(this, ManageBudgetsActivity.class);
        startActivity(intent);
    }

    private void startAddTxActivity(TxHistory item) {
        Intent intent = new Intent(this, QuickAddTransactionActivity.class);

        intent.putExtra(QuickAddTransactionActivity.ACCOUNTKEY,
                item.getFromAccountId());
        intent.putExtra(QuickAddTransactionActivity.PAYEEKEY,
                item.getToAccountId());

        startActivity(intent);
    }

    class MyArrayAdapter extends ArrayAdapter<Shortcut> {

        public MyArrayAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Shortcut item = super.getItem(position);
            ShortcutView view = new ShortcutView(getContext(), item);
            view.setPadding(2, 5, 2, 5);
            return view;
        }
    }

    class Shortcut {
        private TxHistory item;
        private Account from;
        private Account to;

        public Shortcut(TxHistory item, Account from, Account to) {
            super();
            this.item = item;
            this.from = from;
            this.to = to;
        }
    }

    class ShortcutView extends LinearLayout {
        private Shortcut shortcut;

        public ShortcutView(Context context, Shortcut item) {
            super(context);
            super.setOrientation(LinearLayout.VERTICAL);
            this.shortcut = item;

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);

            //
            TextView type_view = new TextView(context);
            type_view.setPadding(10, 2, 10, 0);
            type_view.setTextSize(15f);
            type_view.setTextColor(Color.BLACK);
            addView(type_view, params);

            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);

            TextView account_view = new TextView(context);
            account_view.setTextSize(13f);
            account_view.setTextColor(Color.BLACK);
            account_view.setGravity(Gravity.LEFT);
            account_view.setPadding(10, 2, 10, 0);
            addView(account_view, params1);

            LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                    LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);

            TextView payee_view = new TextView(context);
            payee_view.setTextSize(13f);
            payee_view.setGravity(Gravity.LEFT);
            payee_view.setPadding(10, 2, 10, 0);
            addView(payee_view, params2);

            setData(type_view, account_view, payee_view);
        }

        private void setData(TextView typeView, TextView accountView,
                TextView payeeView) {

            try {
                int fromType = shortcut.from.getAccountType();
                int toType = shortcut.to.getAccountType();

                StringBuilder txt = new StringBuilder();
                if (fromType == AccountTypes.ACCT_TYPE_INCOME) {
                    txt.append("Add income, " + shortcut.from.getAccountName());
                    accountView.setText("To: " + shortcut.to.getAccountName());
                }
                else if (toType == AccountTypes.ACCT_TYPE_EXPENSE) {
                    txt.append("Add expense, " + shortcut.to.getAccountName());
                    accountView.setText("Using: "
                            + shortcut.from.getAccountName());
                }
                else {
                    txt.append("Add transaction");
                    accountView.setText("Account: "
                            + shortcut.from.getAccountName());
                    payeeView.setText("Payee: " + shortcut.to.getAccountName());
                }

                typeView.setText(txt.toString());

            }
            catch (Exception e) {
                Log.e(TAG, MiscUtils.stackTrace2String(e));
            }
        }
    }

    /* Options menu handlers */
    private void gotoMarket() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(MARKET_URI));
        startActivity(intent);
    }

    private void gotoReports() {
        final CharSequence[] items = { "Weekly", "Monthly" };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cash flow report");
        builder.setSingleChoiceItems(items, -1,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        dialog.dismiss();
                        Intent intent = new Intent(iFreeBudget.this,
                                ViewReportActivity.class);

                        intent.putExtra(ViewReportActivity.REPORTTYPEKEY,
                                items[item]);

                        startActivity(intent);
                    }
                });
        builder.show();
    }
    
    private void gotoDBManager() {
        Intent intent = new Intent(iFreeBudget.this,
                ManageDBActivity.class);

        startActivity(intent);        
    }

    private void clearAll() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(
                "This will delete all data in this application. Are you sure?")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                reCreateDb();
                            }
                        })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        builder.show();
    }

    private void reCreateDb() {
        dialog = ProgressDialog.show(iFreeBudget.this, "", "Please wait...",
                true);

        Runnable r = new Runnable() {
            @Override
            public void run() {
                FManEntityManager.getInstance().reInitializeDb();
                updateHandler.sendEmptyMessage(0);
            }
        };
        new Thread(r).start();
    }

    private final Handler updateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            dialog.dismiss();
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    };

    /* End options menu handlers */
}
