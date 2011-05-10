package com.ifreebudget.fm.activities;

import static com.ifreebudget.fm.utils.Messages.tr;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.constants.AccountTypes;
import com.ifreebudget.fm.entity.DBException;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.Account;
import com.ifreebudget.fm.entity.beans.Budget;
import com.ifreebudget.fm.entity.beans.BudgetedAccount;
import com.ifreebudget.fm.entity.beans.FManEntity;
import com.ifreebudget.fm.utils.MiscUtils;

public class AddBudgetActivity extends Activity {
    private final static String TAG = "AddBudgetActivity";

    private List<FManEntity> budgetedAccounts = null;

    private EditText bNameTf;

    private Spinner bTypeSpinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.add_budget_layout);

        bNameTf = (EditText) findViewById(R.id.budget_name_tf);

        bTypeSpinner = (Spinner) findViewById(R.id.budget_type_spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.budget_types,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        bTypeSpinner.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        FManEntityManager em = FManEntityManager.getInstance();
        int[] expenseTypes = { AccountTypes.ACCT_TYPE_EXPENSE };
        try {
            TableLayout table = (TableLayout) findViewById(R.id.accounts_tbl);
            budgetedAccounts = em.getAccountsForTypes(expenseTypes);
            int counter = 0;
            for (FManEntity e : budgetedAccounts) {
                Account a = (Account) e;
                TableRow tr = new TableRow(this);
                tr.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
                        LayoutParams.WRAP_CONTENT));

                LayoutParams lp1 = new LayoutParams(LayoutParams.FILL_PARENT,
                        LayoutParams.WRAP_CONTENT);
                lp1.weight = 1f;

                TextView tv = new TextView(this);
                tv.setPadding(10, 5, 10, 0);
                tv.setTextSize(15f);
                tv.setTextColor(Color.BLACK);
                tv.setText(a.getAccountName());
                tv.setLayoutParams(lp1);
                tr.addView(tv);

                LayoutParams lp2 = new LayoutParams(LayoutParams.FILL_PARENT,
                        LayoutParams.WRAP_CONTENT);
                lp2.weight = 0f;

                EditText ev = new EditText(this);
                ev.setId(counter++);
                ev.setSingleLine(true);
                ev.setInputType(InputType.TYPE_CLASS_NUMBER
                        | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                tv.setLayoutParams(lp2);
                tr.addView(ev);

                table.addView(tr, new TableLayout.LayoutParams(
                        LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            }
        }
        catch (DBException e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
    }

    public void doCancelAction(View view) {
        super.finish();
    }

    public void saveBudget(View view) {
        Budget b = new Budget();

        String name = bNameTf.getText().toString();
        if (!MiscUtils.isValidString(name)) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    tr("Name is required"), Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        b.setName(bNameTf.getText().toString());

        int pos = bTypeSpinner.getSelectedItemPosition();
        if (pos < 0) {
            pos = 0;
        }
        String typeStr = bTypeSpinner.getAdapter().getItem(pos).toString();
        int type = Budget.getTypeFromString(typeStr);
        b.setType(type);

        int numAccts = budgetedAccounts.size();

        List<BudgetedAccount> bAccounts = new ArrayList<BudgetedAccount>();
        for (int i = 0; i < numAccts; i++) {
            EditText et = (EditText) findViewById(i);
            if (et != null) {
                String amt = et.getText().toString();
                if (amt == null || amt.length() == 0) {
                    continue;
                }
                try {
                    Account a = (Account) budgetedAccounts.get(i);
                    BigDecimal bd = new BigDecimal(amt);
                    BudgetedAccount ba = new BudgetedAccount();
                    ba.setAllocatedAmount(bd);
                    ba.setAccountId(a.getAccountId());
                    bAccounts.add(ba);
                }
                catch (Exception e) {
                    Log.e(TAG, MiscUtils.stackTrace2String(e));
                    continue;
                }
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
            super.finish();
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
}
