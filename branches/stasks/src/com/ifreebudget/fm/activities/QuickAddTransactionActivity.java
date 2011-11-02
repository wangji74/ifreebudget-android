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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.actions.ActionRequest;
import com.ifreebudget.fm.actions.ActionResponse;
import com.ifreebudget.fm.actions.AddNestedTransactions;
import com.ifreebudget.fm.entity.DBException;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.Account;
import com.ifreebudget.fm.entity.beans.TaskNotification;
import com.ifreebudget.fm.entity.beans.Transaction;
import com.ifreebudget.fm.services.SessionManager;
import com.ifreebudget.fm.utils.MiscUtils;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class QuickAddTransactionActivity extends Activity {

    public static final String TXIDKEY = "TXIDKEY";

    private static final int DATE_DIALOG_ID = 0;

    private Long txId;
    private Long notificationId;

    private Account from = null;
    private Account to = null;

    private TextView fromAcctTf, toAcctTf;
    private Button dateBtn;
    private EditText amountTf, tagsTf;

    private final String TAG = "QuickAddTransactionActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.quick_add_tx_layout);

        fromAcctTf = (TextView) findViewById(R.id.from_acct_val);
        toAcctTf = (TextView) findViewById(R.id.to_acct_val);
        amountTf = (EditText) findViewById(R.id.tx_amt_tf);
        tagsTf = (EditText) findViewById(R.id.tx_tags_tf);
        dateBtn = (Button) findViewById(R.id.tx_date_btn);
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = this.getIntent();

        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null
                    && bundle
                            .containsKey(ManageTaskNotificationActivity.NOTIFIDKEY)) {
                notificationId = (Long) bundle
                        .get(ManageTaskNotificationActivity.NOTIFIDKEY);
            }
            if (bundle != null && bundle.containsKey(TXIDKEY)) {
                txId = (Long) bundle.get(TXIDKEY);
            }
        }

        FManEntityManager em = FManEntityManager.getInstance();
        try {
            TaskNotification notif = em.getTaskNotification(notificationId);

            if (notif == null) {
                Log.e(TAG, "Missing task notification entity: "
                        + notificationId);
                Toast toast = Toast.makeText(getApplicationContext(),
                        tr("Can not find notification details!"),
                        Toast.LENGTH_SHORT);
                toast.show();
                return;
            }

            Transaction tx = em.getTransaction(txId);
            if (tx == null) {
                String msg = "Missing transaction for scheduling!";
                Log.e(TAG, msg + ", notification: " + notif.getTaskId());
                Toast toast = Toast.makeText(getApplicationContext(),
                        tr("msg"), Toast.LENGTH_SHORT);
                toast.show();
            }

            from = em.getAccount(tx.getFromAccountId());
            to = em.getAccount(tx.getToAccountId());

            fromAcctTf.setText(from.getAccountName());
            toAcctTf.setText(to.getAccountName());

            String date = SessionManager.getDateFormat().format(new Date());
            dateBtn.setGravity(Gravity.CENTER);
            dateBtn.setText(date);
        }
        catch (DBException e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
    }

    public void saveTransaction(View view) {
        String txAmtStr = amountTf.getText().toString();
        BigDecimal txAmt = null;
        try {
            txAmt = new BigDecimal(txAmtStr);
        }
        catch (NumberFormatException e) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    tr("Invalid amount."), Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        String txDateStr = dateBtn.getText().toString();
        Date txDate = null;
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(SessionManager.getDateFormat().parse(txDateStr));
            Calendar now = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, now.get(Calendar.HOUR_OF_DAY));
            cal.set(Calendar.MINUTE, now.get(Calendar.MINUTE));
            cal.set(Calendar.SECOND, now.get(Calendar.SECOND));
            txDate = cal.getTime();
        }
        catch (ParseException e) {
            Log.e(TAG, e.getMessage());
            Toast toast = Toast.makeText(getApplicationContext(),
                    tr("Invalid date."), Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        if (txDate.after(new Date())) {
            Toast toast = Toast
                    .makeText(
                            getApplicationContext(),
                            ("Date is after today's date. Scheduled transactions are not supported."),
                            Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        String txTagsStr = tagsTf.getText().toString();
        Transaction t = new Transaction();
        t.setFromAccountId(from.getAccountId());
        t.setToAccountId(to.getAccountId());
        t.setTxDate(txDate.getTime());
        t.setTxAmount(txAmt);
        t.setTxNotes(txTagsStr);

        List<Transaction> txList = new ArrayList<Transaction>(1);
        txList.add(t);

        ActionRequest req = new ActionRequest();
        req.setActionName("addTransaction");
        req.setProperty("TXLIST", txList);
        req.setProperty("UPDATETX", Boolean.FALSE);

        try {
            ActionResponse resp = new AddNestedTransactions()
                    .executeAction(req);
            if (resp.getErrorCode() != ActionResponse.NOERROR) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        resp.getErrorMessage(), Toast.LENGTH_SHORT);
                toast.show();
            }
            else {
                /* Tx was added successfully, delete the notif */
                TaskNotification tn = new TaskNotification();
                tn.setId(notificationId);
                FManEntityManager.getInstance().deleteEntity(tn);
                super.finish();
            }
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
            Toast toast = Toast.makeText(getApplicationContext(),
                    tr("Invalid date."), Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Calendar gc = Calendar.getInstance();
        switch (id) {
        case DATE_DIALOG_ID:
            return new DatePickerDialog(this, mDateSetListener,
                    gc.get(Calendar.YEAR), gc.get(Calendar.MONTH),
                    gc.get(Calendar.DATE));
        }
        return null;
    }

    public void showDatePickerDialog(View view) {
        super.showDialog(DATE_DIALOG_ID);
    }

    public void doCancelAction(View view) {
        super.finish();
    }

    public void cancelTxNotification(View view) {
        try {
            TaskNotification tn = new TaskNotification();
            tn.setId(notificationId);
            FManEntityManager.getInstance().deleteEntity(tn);
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
        finally {
            super.finish();
        }
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                int dayOfMonth) {

            Calendar gc = Calendar.getInstance();
            gc.set(Calendar.YEAR, year);
            gc.set(Calendar.MONTH, monthOfYear);
            gc.set(Calendar.DATE, dayOfMonth);

            String formattedTxt = SessionManager.getDateFormat().format(
                    gc.getTime());
            dateBtn.setText(formattedTxt);
        }
    };
}
