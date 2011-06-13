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
package com.ifreebudget.fm.activities.wizards.budget;

import static com.ifreebudget.fm.utils.Messages.tr;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.activities.ManageBudgetsActivity;
import com.ifreebudget.fm.constants.AccountTypes;
import com.ifreebudget.fm.entity.DBException;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.Account;
import com.ifreebudget.fm.entity.beans.Budget;
import com.ifreebudget.fm.entity.beans.BudgetedAccount;
import com.ifreebudget.fm.entity.beans.FManEntity;
import com.ifreebudget.fm.services.SessionManager;
import com.ifreebudget.fm.utils.MiscUtils;

public class AddBudgetActivity extends Activity {
    private final static String TAG = "AddBudgetActivity";

    private FManEntity[] budgetedAccounts = null;

    private GridView grid = null;

    private String name = null;

    private int type = 0;

    private TextView title = null;

    private TextView subtitle = null;

    private BigDecimal runningTotal = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.add_budget_layout);

        grid = (GridView) findViewById(R.id.budget_accts_grid);
        title = (TextView) findViewById(R.id.budget_name_lbl);
        subtitle = (TextView) findViewById(R.id.subtitle_lbl);
        runningTotal = new BigDecimal(0d);
        
        grid.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                    int position, long id) {

                handleGridItemClick(parent, v, position, id);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = this.getIntent();

        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null && bundle.containsKey("BUDGETTYPE")) {
                type = (Integer) bundle.get("BUDGETTYPE");
            }
            if (bundle != null && bundle.containsKey("BUDGETNAME")) {
                name = (String) bundle.get("BUDGETNAME");
            }
        }

        String typeStr = Budget.getTypeAsString(type);
        title.setText(name + "\t( " + typeStr + " )");

        FManEntityManager em = FManEntityManager.getInstance();
        int[] expenseTypes = { AccountTypes.ACCT_TYPE_EXPENSE };
        try {
            List<FManEntity> list = em.getAccountsForTypes(expenseTypes);
            budgetedAccounts = new FManEntity[list.size()];
            int sz = list.size();
            for (int i = 0; i < sz; i++) {
                Account a = (Account) list.get(i);
                BudgetedAccount ba = new BudgetedAccount();
                ba.setAccountName(a.getAccountName());
                ba.setAllocatedAmount(new BigDecimal(0d));
                ba.setAccountId(a.getAccountId());
                budgetedAccounts[i] = ba;
            }
            grid.setAdapter(new BudgetAccountAdapter(this,
                    R.layout.budget_acct_layout, R.id.label, budgetedAccounts));
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
    }

    protected void handleGridItemClick(AdapterView<?> l, final View v,
            int position, long id) {
        final BudgetedAccount obj = (BudgetedAccount) grid.getAdapter()
                .getItem(position);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(getString(R.string.alloc));
        alert.setMessage(getString(R.string.amt));

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String val = input.getText().toString();

                BigDecimal bd = new BigDecimal(val);
                obj.setAllocatedAmount(bd);

                TextView tv = (TextView) v
                        .findViewById(R.id.budget_acct_name_tf);
                tv.setText(getDisplayString(obj));
                
                setRunningTotal();
            }
        });

        alert.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });

        alert.show();
    }

    private void setRunningTotal() {
        runningTotal = new BigDecimal(0d);
        for(FManEntity a : budgetedAccounts) {
            BudgetedAccount ba = (BudgetedAccount) a;
            BigDecimal alloc = ba.getAllocatedAmount();
            if(alloc == null || alloc.doubleValue() == 0d) {
                continue;                
            }
            runningTotal = runningTotal.add(alloc);
        }
        String totalStr = NumberFormat.getCurrencyInstance(
                SessionManager.getCurrencyLocale()).format(runningTotal);
        
        subtitle.setText("Total " + totalStr);
    }
    
    public void doCancelAction(View view) {
        super.finish();
    }

    public void saveBudget(View view) {
        Budget b = new Budget();
        b.setName(name);
        b.setType(type);

        int numAccts = budgetedAccounts.length;

        List<BudgetedAccount> bAccounts = new ArrayList<BudgetedAccount>();
        for (int i = 0; i < numAccts; i++) {
            BudgetedAccount ba = (BudgetedAccount) budgetedAccounts[i];
            BigDecimal amt = ba.getAllocatedAmount();
            if (amt == null || amt.doubleValue() == 0d) {
                continue;
            }
            else {
                bAccounts.add(ba);
            }
        }

        FManEntityManager em = FManEntityManager.getInstance();
        em.beginTransaction();

        try {
            em.createEntity(b);

            Long id = b.getId();
            for (BudgetedAccount ba : bAccounts) {
                ba.setBudgetId(id);
                em.createEntity(ba);
            }
            em.setTransactionSuccessful();
            gotoManageBudgetsActivity();
        }
        catch (DBException e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
            Toast toast = Toast.makeText(getApplicationContext(),
                    tr("Failed to save budget."), Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        finally {
            em.endTransaction();
        }
    }

    private void gotoManageBudgetsActivity() {
        Intent intent = new Intent(this, ManageBudgetsActivity.class);
        startActivity(intent);
    }

    private String getDisplayString(BudgetedAccount a) {
        StringBuilder ret = new StringBuilder(a.getAccountName());
        BigDecimal allocatedAmount = a.getAllocatedAmount();
        if (allocatedAmount != null && allocatedAmount.doubleValue() > 0d) {
            ret.append("\n");
            ret.append(NumberFormat.getCurrencyInstance(
                    SessionManager.getCurrencyLocale()).format(allocatedAmount));
            ret.append("");
        }

        return ret.toString();
    }

    /* Row adapter for budgeted account grid */
    class BudgetAccountAdapter extends ArrayAdapter<FManEntity> {
        private FManEntity[] items;

        BudgetAccountAdapter(Context context, int resource, int label,
                FManEntity[] items) {
            super(AddBudgetActivity.this, R.layout.budget_acct_layout,
                    R.id.label, items);
            this.items = items;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View v;
            if (convertView == null) {
                LayoutInflater li = getLayoutInflater();
                v = li.inflate(R.layout.budget_acct_layout, null);
            }
            else {
                v = convertView;
            }
            ImageView icon = (ImageView) v.findViewById(R.id.budget_acct_icon);
            FManEntity entity = items[position];
            icon.setImageResource(R.drawable.account);
            TextView tv = (TextView) v.findViewById(R.id.budget_acct_name_tf);
            tv.setText(getDisplayString((BudgetedAccount) entity));
            return v;
        }
    }
    /* End row adapter inner class */
}
