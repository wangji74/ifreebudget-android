package com.ifreebudget.fm.activities.wizards.budget;

import static com.ifreebudget.fm.utils.Messages.tr;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.entity.DBException;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.Account;
import com.ifreebudget.fm.utils.MiscUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class BudgetAccountAmountEntryActivity extends Activity {
    private static final String TAG = "BudgetAccountAmountEntryActivity";
    private Account account = null;
    private TextView acctName = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.budgetitem_amt_entry);
        acctName = (TextView) findViewById(R.id.account_name_lbl);
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = this.getIntent();
        long accountId = -1;
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null && bundle.containsKey("ACCOUNTID")) {
                accountId = (Integer) bundle.get("ACCOUNTID");
            }
        }

        try {
            account = FManEntityManager.getInstance().getAccount(accountId);
            acctName.setText(account.getAccountName());
        }
        catch (DBException e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
            Toast toast = Toast.makeText(getApplicationContext(),
                    tr("Unable to find account to allocate budget amount."),
                    Toast.LENGTH_SHORT);
            toast.show();
            super.finish();
        }
    }
}
