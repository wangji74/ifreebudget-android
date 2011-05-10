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
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.iFreeBudget;
import com.ifreebudget.fm.actions.ActionResponse;
import com.ifreebudget.fm.actions.AddAccountAction;
import com.ifreebudget.fm.constants.AccountTypes;
import com.ifreebudget.fm.entity.DBException;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.Account;
import com.ifreebudget.fm.entity.beans.AccountCategory;
import com.ifreebudget.fm.utils.MiscUtils;

import static com.ifreebudget.fm.utils.Messages.tr;

public class AddAccountActivity extends Activity {
    private static final String TAG = "AddAccountActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addacct_layout);
    }

    public void doCancelAction(View view) {
        super.finish();
    }

    public void saveAccount(View view) {
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

        if (catId != -1) {
            if (createAccount(catId)) {
                intent = new Intent(this, ManageAccountsActivity.class);

                intent.putExtra(iFreeBudget.PARENTCATEGORYIDKEY, catId);

                startActivity(intent);
                finish();
            }
        }
        else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Please select a category.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private boolean createAccount(long categoryId) {
        try {
            Date now = new Date();

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
            BigDecimal startBalVal = BigDecimal.valueOf(startingBalance);

            Account a = new Account();
            a.setAccountName(name.trim());
            a.setAccountNumber(accountNumber.trim());
            a.setAccountNotes(accountNotes.trim());
            a.setStartingBalance(startBalVal);
            a.setCurrentBalance(startBalVal);

            Long type = getAccountType(categoryId);
            if (type < 0) {
                return false;
            }

            a.setAccountType(type.intValue());

            a.setStatus(AccountTypes.ACCOUNT_ACTIVE);
            a.setCategoryId(categoryId);
            a.setAccountParentType(a.getAccountType());

            a.setStartDate(now.getTime());

            ActionResponse resp = new AddAccountAction().executeAction(a);

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

    private Long getAccountType(Long categoryId) throws DBException {
        FManEntityManager em = FManEntityManager.getInstance();

        int guard = 100;
        AccountCategory base = null;
        base = em.getAccountCategory(categoryId);
        while (--guard > 0) {
            if (base.getParentCategoryId() == -1) {
                break;
            }
            base = em.getAccountCategory(base.getParentCategoryId());
            if (base == null) {
                break;
            }
            categoryId = base.getCategoryId();
        }
        if (base != null) {
            return base.getCategoryId();
        }
        else {
            return -1L;
        }
    }
}
