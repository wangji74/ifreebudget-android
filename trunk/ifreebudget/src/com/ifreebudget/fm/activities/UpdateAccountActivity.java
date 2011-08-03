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

import static com.ifreebudget.fm.utils.Messages.tr;

import java.math.BigDecimal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.iFreeBudget;
import com.ifreebudget.fm.actions.ActionRequest;
import com.ifreebudget.fm.actions.ActionResponse;
import com.ifreebudget.fm.actions.UpdateAccountAction;
import com.ifreebudget.fm.entity.DBException;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.Account;
import com.ifreebudget.fm.utils.MiscUtils;

public class UpdateAccountActivity extends Activity {
    private static final String TAG = "UpdateAccountActivity";
    private Account account = null;
    private TextView subtitleLbl = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_acct_layout);
        subtitleLbl = (TextView) findViewById(R.id.subtitle_lbl);
    }

    @Override
    public void onResume() {
        super.onResume();
        Long accountId = null;
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle.containsKey(iFreeBudget.ACCOUNTIDKEY)) {
            accountId = (Long) bundle.get(iFreeBudget.ACCOUNTIDKEY);
        }

        if (bundle.containsKey(ManageAccountsActivity.PARENTCATEGORYIDPATH)) {
            String categoryPath = (String) bundle
                    .get(ManageAccountsActivity.PARENTCATEGORYIDPATH);
            subtitleLbl.setText(categoryPath);
        }

        if (accountId != null) {
            initializeFields(accountId);
        }
    }
    
    private void startHomeActivity() {
        Intent intent = new Intent(this, iFreeBudget.class);
        startActivity(intent);
    }

    public void gotoHomeScreen(View view) {
        startHomeActivity();
    }    

    private void initializeFields(Long accountId) {
        FManEntityManager em = FManEntityManager.getInstance();
        try {
            account = em.getAccount(accountId);

            EditText nameTf = (EditText) findViewById(R.id.acct_name_tf);
            nameTf.setText(account.getAccountName());

            EditText numTf = (EditText) findViewById(R.id.acct_number_tf);
            numTf.setText(account.getAccountNumber());

            EditText notesTf = (EditText) findViewById(R.id.acct_notes_tf);
            notesTf.setText(account.getAccountNotes());

            EditText startBalTf = (EditText) findViewById(R.id.acct_start_bal_tf);
            startBalTf.setText(account.getCurrentBalance().toString());
        }
        catch (DBException e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
    }

    public void doCancelAction(View view) {
        super.finish();
    }

    public void updateAccount(View view) {
        Intent intent = this.getIntent();

        long catId = -1;
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                if (bundle.containsKey(iFreeBudget.PARENTCATEGORYIDKEY)) {
                    catId = (Long) bundle.get(iFreeBudget.PARENTCATEGORYIDKEY);
                }
            }
        }

        if (account != null) {
            if (updateAccount()) {
                intent = new Intent(this, ManageAccountsActivity.class);

                intent.putExtra(iFreeBudget.PARENTCATEGORYIDKEY, catId);

                startActivity(intent);
            }
        }
    }

    private boolean updateAccount() {
        try {
            EditText nameTf = (EditText) findViewById(R.id.acct_name_tf);
            String name = nameTf.getText().toString();

            if (name == null || name.trim().length() == 0) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        tr("Account name is required"), Toast.LENGTH_SHORT);
                toast.show();
                return false;
            }

            EditText numTf = (EditText) findViewById(R.id.acct_number_tf);
            String accountNumber = numTf.getText().toString();

            EditText notesTf = (EditText) findViewById(R.id.acct_notes_tf);
            String accountNotes = notesTf.getText().toString();

            EditText startBalTf = (EditText) findViewById(R.id.acct_start_bal_tf);
            String startBal = startBalTf.getText().toString();
            double startingBalance = 0d;
            try {
                startingBalance = Double.parseDouble(startBal);
            }
            catch (NumberFormatException e) {
                Log.e(TAG, "Invalid starting balance: " + startBal);
            }

            String nameStr = name.trim();
            Boolean validate = true;
            if (account.getAccountName().equals(nameStr)) {
                validate = false;
            }
            account.setAccountName(nameStr);
            account.setAccountNumber(accountNumber.trim());
            account.setAccountNotes(accountNotes.trim());
            account.setCurrentBalance(BigDecimal.valueOf(startingBalance));

            ActionRequest req = new ActionRequest();
            req.setProperty("ACCOUNT", account);
            req.setProperty("VALIDATENAME", validate);

            ActionResponse resp = new UpdateAccountAction().executeAction(req);

            if (resp.getErrorCode() == ActionResponse.NOERROR) {
                return true;
            }
            else {
                Toast toast = Toast.makeText(getApplicationContext(),
                        resp.getErrorMessage(), Toast.LENGTH_SHORT);
                toast.show();
                return false;
            }
        }
        catch (Exception e) {
            Log.e(getClass().getName(), MiscUtils.stackTrace2String(e));
            return false;
        }
    }
}
