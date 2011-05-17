package com.ifreebudget.fm.activities;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.iFreeBudget;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class MainView extends Activity {
    GridView grid = null;

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

    private void gotoReports() {
        final CharSequence[] items = { "Weekly", "Monthly" };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cash flow report");
        builder.setSingleChoiceItems(items, -1,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        dialog.dismiss();
                        Intent intent = new Intent(MainView.this,
                                ViewReportActivity.class);

                        intent.putExtra(ViewReportActivity.REPORTTYPEKEY,
                                items[item]);

                        startActivity(intent);
                    }
                });
        builder.show();
    }

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
                TextView tv = (TextView) v.findViewById(R.id.icon_text);

                tv.setText(mTextIds[position]);

                ImageView iv = (ImageView) v.findViewById(R.id.icon_image);
                iv.setImageResource(mThumbIds[position]);
            }
            else {
                v = convertView;
            }
            return v;
        }

        private String[] mTextIds = getResources().getStringArray(
                R.array.grid_items);

        private Integer[] mThumbIds = { R.drawable.gitem_accounts,
                R.drawable.gitem_transactions, R.drawable.gitem_budgets,
                R.drawable.gitem_reports, R.drawable.gitem_backups };
    }
}
