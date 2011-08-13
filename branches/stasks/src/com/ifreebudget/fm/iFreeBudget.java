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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.ifreebudget.fm.actions.ActionRequest;
import com.ifreebudget.fm.actions.ActionResponse;
import com.ifreebudget.fm.actions.GetNetWorthAction;
import com.ifreebudget.fm.activities.AddReminderActivity;
import com.ifreebudget.fm.activities.AddTransactionActivity;
import com.ifreebudget.fm.activities.DocActivity;
import com.ifreebudget.fm.activities.ListTransactionsActivity;
import com.ifreebudget.fm.activities.ManageAccountsActivity;
import com.ifreebudget.fm.activities.ManageBudgetsActivity;
import com.ifreebudget.fm.activities.ManageDBActivity;
import com.ifreebudget.fm.activities.ViewReportActivity;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.services.SessionManager;
import com.ifreebudget.fm.utils.MiscUtils;

public class iFreeBudget extends Activity {
    public static final String PARENTCATEGORYIDKEY = "PARENTCATEGORYID";
    public static final String ACCOUNTIDKEY = "ACCOUNTID";
    public static final String CATEGORYIDKEY = "CATEGORYID";

    private static final String TAG = "iFreeBudget";

    private String MARKET_URI = "market://details?id=com.ifreebudget.fm";

    private GridView grid = null;

    private ProgressDialog dialog = null;

    /* Platform overrides */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.main1);

        grid = (GridView) findViewById(R.id.main_grid);
        grid.setAdapter(new ImageAdapter(this));

        grid.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                    int position, long id) {
                handleItemClick(v, position);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        FManEntityManager.getInstance(this);
        Log.i(TAG, "DBHelper created.");
        getNetworth();
    }

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
        case R.id.mItemClear:
            clearAll();
            return true;
        case R.id.mItemRate:
            gotoMarket();
            return true;
        case R.id.mItemHelp:
            gotoHelp();
            return true;
        default:
            return super.onOptionsItemSelected(item);

        }
    }

    /* End platform overrides */

    /* Private helper methods */
    private void handleItemClick(View view, int position) {
        switch (position) {
        case 0:
            manageAccounts(view);
            return;
        case 1:
            manageTransactions(view);
            return;
        case 2:
            manageBudgets(view);
            return;
        case 3:
            gotoReports();
            return;
        case 4:
            manageBackups(view);
            return;
        case 5:
            addTransaction(view);
            return;
        case 6:
            addReminder(view);
            return;
        default:
            return;
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

    private void manageBackups(View view) {
        Intent intent = new Intent(this, ManageDBActivity.class);

        startActivity(intent);
    }

    private void gotoHelp() {
        Intent doc = null;
        doc = new Intent(this, DocActivity.class);
        startActivity(doc);        
    }
    
    private void gotoMarket() {
        Intent goToMarket = null;
        goToMarket = new Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_URI));
        startActivity(goToMarket);
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

    public void addTransaction(View view) {
        Intent txIntent = new Intent(this, AddTransactionActivity.class);
        startActivity(txIntent);
    }

    public void addReminder(View view) {
        Intent txIntent = new Intent(this, AddReminderActivity.class);
        startActivity(txIntent);
    }

    private void getNetworth() {
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

        TextView nwTf = (TextView) findViewById(R.id.nw_lbl);
        TextView asTf = (TextView) findViewById(R.id.asset_lbl);
        TextView lbTf = (TextView) findViewById(R.id.liab_lbl);

        BigDecimal assetVal = (BigDecimal) resp.getResult("ASSET_VALUE");
        BigDecimal liabsVal = (BigDecimal) resp.getResult("LIAB_VALUE");
        BigDecimal nwVal = (BigDecimal) resp.getResult("NET_VALUE");

        asTf.setText("Assets " + numberFormat.format(assetVal));
        lbTf.setText("Liabilities " + numberFormat.format(liabsVal));
        nwTf.setText("Net worth " + numberFormat.format(nwVal));
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

    /* End private helper methods */

    /* Inner class */
    public class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return mThumbIds.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View v;
            if (convertView == null) {
                LayoutInflater li = getLayoutInflater();
                v = li.inflate(R.layout.grid_item_layout, null);
            }
            else {
                v = convertView;
            }
            TextView tv = (TextView) v.findViewById(R.id.icon_text);
            tv.setText(mTextIds[position]);
            ImageView iv = (ImageView) v.findViewById(R.id.icon_image);
            iv.setImageResource(mThumbIds[position]);
            return v;
        }

        private String[] mTextIds = getResources().getStringArray(
                R.array.grid_items);

        private Integer[] mThumbIds = { R.drawable.gitem_accounts,
                R.drawable.gitem_transactions, R.drawable.gitem_budgets,
                R.drawable.gitem_reports, R.drawable.gitem_backups,
                R.drawable.gitem_newtx, R.drawable.reminder };
    }
}
