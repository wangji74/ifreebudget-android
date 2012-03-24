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
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.iFreeBudget;
import com.ifreebudget.fm.activities.ManageBudgetsActivity;
import com.ifreebudget.fm.activities.utils.AccountsExpandableListAdapter;
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

    private ExpandableListView list = null;

    private AccountsExpandableListAdapter listAdapter;

    private String name = null;

    private int type = 0;

    private TextView title = null;

    private TextView totalLbl = null;

    private BigDecimal runningTotal = null;

    public void gotoHomeScreen(View view) {
        Intent intent = new Intent(this, iFreeBudget.class);
        startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.add_budget_layout);

        Display newDisplay = getWindowManager().getDefaultDisplay();
        int width = newDisplay.getWidth();

        list = (ExpandableListView) findViewById(R.id.budget_accts_list);
        list.setIndicatorBounds(width - 50, width);
        list.setOnChildClickListener(new OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                    int groupPosition, int childPosition, long id) {

                handleListItemClick(parent, v, groupPosition, childPosition, id);

                return true;
            }
        });

        listAdapter = new AccountsExpandableListAdapter(this);
        list.setAdapter(listAdapter);

        title = (TextView) findViewById(R.id.budget_name_lbl);
        totalLbl = (TextView) findViewById(R.id.budget_amt_lbl);

        runningTotal = new BigDecimal(0d);
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
        title.setText(name);

        FManEntityManager em = FManEntityManager.getInstance(this);
        int[] expenseTypes = { AccountTypes.ACCT_TYPE_EXPENSE };
        try {
            List<FManEntity> list = em.getAccountsForTypes(expenseTypes);
            budgetedAccounts = listAdapter.setData(list);
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
    }

    protected void handleListItemClick(ExpandableListView l, final View v,
            int groupPosition, int childPosition, long id) {

        
        final BudgetedAccount obj = (BudgetedAccount) listAdapter.getChild(
                groupPosition, childPosition);

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(obj.getAccountName());
        alert.setMessage(getString(R.string.alloc));

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String val = input.getText().toString();

                BigDecimal bd = new BigDecimal(val);
                obj.setAllocatedAmount(bd);

                listAdapter.notifyDataSetChanged();
                
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
        for (FManEntity a : budgetedAccounts) {
            BudgetedAccount ba = (BudgetedAccount) a;
            BigDecimal alloc = ba.getAllocatedAmount();
            if (alloc == null || alloc.doubleValue() == 0d) {
                continue;
            }
            runningTotal = runningTotal.add(alloc);
        }
        String totalStr = NumberFormat.getCurrencyInstance(
                SessionManager.getCurrencyLocale()).format(runningTotal);

        StringBuilder typeStr = new StringBuilder().append("Total: ").append(
                totalStr);
        totalLbl.setText(typeStr);
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
}
