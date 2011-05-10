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
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.actions.ActionRequest;
import com.ifreebudget.fm.actions.ActionResponse;
import com.ifreebudget.fm.actions.AddNestedTransactions;
import com.ifreebudget.fm.actions.DeleteTransactionAction;
import com.ifreebudget.fm.constants.AccountTypes;
import com.ifreebudget.fm.entity.DBException;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.Account;
import com.ifreebudget.fm.entity.beans.FManEntity;
import com.ifreebudget.fm.entity.beans.Transaction;
import com.ifreebudget.fm.services.SessionManager;
import com.ifreebudget.fm.utils.MiscUtils;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;

public class UpdateTransactionActivity extends Activity {
    private static final int DATE_DIALOG_ID = 0;
    private Long txId;

    private Button dateBtn;
    private Spinner txTypeSpinner;

    private TextView fromAcctVal;

    private Spinner toAcctSpinner;
    private ArrayAdapter<FManEntity> toSpinnerAdapter;

    private EditText amountTf;

    private EditText tagsTf;

    private static final String TAG = "UpdateTransactionActivity";

    final NumberFormat nf = NumberFormat.getCurrencyInstance(SessionManager
            .getCurrencyLocale());

    private Transaction tx = null;

    private Account from = null;

    private Account to = null;

    public static final String TXID = "TXID";

    /* Platform overrides */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_tx_layout);

        amountTf = (EditText) findViewById(R.id.tx_amt_tf);
        tagsTf = (EditText) findViewById(R.id.tx_tags_tf);

        fromAcctVal = (TextView) findViewById(R.id.from_acct_val);

        toSpinnerAdapter = new ArrayAdapter<FManEntity>(this,
                android.R.layout.simple_spinner_item);
        toAcctSpinner = (Spinner) findViewById(R.id.to_acct_spinner);
        toSpinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toAcctSpinner.setAdapter(toSpinnerAdapter);

        dateBtn = (Button) findViewById(R.id.tx_date_btn);
        dateBtn.setGravity(Gravity.CENTER);
    }

    @Override
    public void onResume() {
        super.onResume();

        Intent intent = this.getIntent();

        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null && bundle.containsKey(TXID)) {
                txId = (Long) bundle.get(TXID);
            }
        }

        FManEntityManager em = FManEntityManager.getInstance();

        try {
            tx = em.getTransaction(txId);

            from = em.getAccount(tx.getFromAccountId());

            to = em.getAccount(tx.getToAccountId());

            fromAcctVal.setText(from.getAccountName());

            String txDateStr = SessionManager.getDateFormat().format(
                    new Date(tx.getTxDate()));

            amountTf.setText(nf.format(tx.getTxAmount()));

            dateBtn.setText(txDateStr);

            tagsTf.setText(tx.getTxNotes());

            loadToAccounts(from, to);
        }
        catch (DBException e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
            Toast toast = Toast.makeText(getApplicationContext(),
                    tr("Failed to edit transaction, " + e.getMessage()),
                    Toast.LENGTH_SHORT);
            toast.show();
            super.finish();
        }
    }

    /* End platform overrides */

    protected void loadToAccounts(FManEntity fromAccount, FManEntity toAccount) {
        List<Long> matches = null;
        try {
            matches = FManEntityManager.getInstance().getBestmatchesForAccount(
                    (Long) fromAccount.getPK());
        }
        catch (DBException e1) {
            Log.e(TAG, MiscUtils.stackTrace2String(e1));
        }
        try {
            loadSpinner(null, toSpinnerAdapter, matches);

            int pos = toSpinnerAdapter.getPosition(toAccount);
            if (pos >= 0) {
                toAcctSpinner.setSelection(pos);
            }
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
    }

    protected void loadSpinner(int[] types, ArrayAdapter<FManEntity> adapter,
            List<Long> bestMatches) throws Exception {
        adapter.clear();
        FManEntityManager em = FManEntityManager.getInstance();
        List<FManEntity> eList = em.getAccountsForTypes(types);

        for (FManEntity object : eList) {
            boolean added = false;
            if (bestMatches != null) {
                Iterator<Long> it = bestMatches.iterator();
                while (it.hasNext()) {
                    Long matchId = it.next();
                    Long id = (Long) object.getPK();
                    if (id.longValue() == matchId.longValue()) {
                        adapter.insert(object, 0);
                        added = true;
                        break;
                    }
                }
            }
            if (!added) {
                adapter.add(object);
            }
        }
    }

    public void showDatePickerDialog(View view) {
        super.showDialog(DATE_DIALOG_ID);
    }

    public void doCancelAction(View view) {
        super.finish();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Calendar gc = Calendar.getInstance();

        SharedPreferences appPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        String txDateStr = appPrefs.getString("LastTxDate", null);

        if (txDateStr != null) {
            try {
                Date dt = SessionManager.getDateFormat().parse(txDateStr);
                gc.setTime(dt);
            }
            catch (Exception e) {
                Log.e(TAG, "Invalid LastTxDate : " + txDateStr);
            }
        }

        switch (id) {
        case DATE_DIALOG_ID:
            return new DatePickerDialog(this, mDateSetListener,
                    gc.get(Calendar.YEAR), gc.get(Calendar.MONTH),
                    gc.get(Calendar.DATE));
        }
        return null;
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

    public void updateTransaction(View view) {
//        boolean del = deleteTransaction(tx.getTxId());
//        if (del) {
//            saveTransaction(view);
//        }
        saveTransaction(view);
    }

    private BigDecimal parseAmount(String txt) {
        try {
            return parseAmountAsNumber(txt);
        }
        catch (Exception e) {
            try {
                return parseAmountAsBigDecimal(txt);
            }
            catch (Exception ex) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        tr("Invalid amount."), Toast.LENGTH_SHORT);
                toast.show();
                return null;
            }
        }
    }
    private BigDecimal parseAmountAsNumber(String txt) throws Exception {
        BigDecimal txAmt = null;
        txAmt = new BigDecimal(nf.parse(txt).doubleValue());
        return txAmt;
    }

    private BigDecimal parseAmountAsBigDecimal(String txt) throws Exception {
        BigDecimal txAmt = null;
        txAmt = new BigDecimal(txt);
        return txAmt;
    }
    
    public void saveTransaction(View view) {
        Account to = (Account) toAcctSpinner.getSelectedItem();

        String txAmtStr = amountTf.getText().toString();
        BigDecimal txAmt = parseAmount(txAmtStr);
        
        if(txAmt == null) {
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
        
        /*
         * Set the tx id of the existing transction, the action will properly
         * delete the old and insert new based on this id in a single db
         * transaction
         */
        t.setTxId(tx.getTxId());
        
        List<Transaction> txList = new ArrayList<Transaction>(1);
        txList.add(t);

        ActionRequest req = new ActionRequest();
        req.setActionName("addTransaction");
        req.setProperty("TXLIST", txList);
        req.setProperty("UPDATETX", Boolean.TRUE);

        try {
            ActionResponse resp = new AddNestedTransactions()
                    .executeAction(req);
            if (resp.getErrorCode() != ActionResponse.NOERROR) {
                Toast toast = Toast.makeText(getApplicationContext(),
                        resp.getErrorMessage(), Toast.LENGTH_SHORT);
                toast.show();
            }
            else {
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

    public boolean deleteTransaction(long txId) {
        try {
            ActionRequest req = new ActionRequest();
            req.setActionName("deleteTransactionAction");
            req.setProperty("TXID", txId);

            ActionResponse resp = new DeleteTransactionAction()
                    .executeAction(req);
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
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Unable to delete transaction", Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }
    }

}
