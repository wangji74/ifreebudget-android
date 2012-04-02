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

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.iFreeBudget;
import com.ifreebudget.fm.actions.ActionRequest;
import com.ifreebudget.fm.actions.ActionResponse;
import com.ifreebudget.fm.actions.AddNestedTransactions;
import com.ifreebudget.fm.constants.AccountTypes;
import com.ifreebudget.fm.entity.DBException;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.Account;
import com.ifreebudget.fm.entity.beans.FManEntity;
import com.ifreebudget.fm.entity.beans.Transaction;
import com.ifreebudget.fm.services.SessionManager;
import com.ifreebudget.fm.utils.MiscUtils;

public class AddTransactionActivity extends Activity {
    private static final int DATE_DIALOG_ID = 0;
    private Button dateBtn;
    private ImageButton cameraBtn;
    private Spinner txTypeSpinner;

    private Spinner fromAcctSpinner;
    private ArrayAdapter<FManEntity> fromSpinnerAdapter;

    private Spinner toAcctSpinner;
    private ArrayAdapter<FManEntity> toSpinnerAdapter;

    private TextView currencyTf;

    private TextView attchmntTf;

    private EditText amountTf;

    private EditText tagsTf;

    private static final String TAG = "AddTransactionActivity";

    private static final String LastTxDate = "LastTxDate";

    private static final int PIC_REQUEST_CODE = 1010;

    private static final String ATTACHMENTS_DIR = "ifb_attachments";

    private static final String DATE_FORMAT = "yyMMddHHmmssS";

    private String attachmentPath = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_tx_layout);

        currencyTf = (TextView) findViewById(R.id.currency_tf);
        Locale l = SessionManager.getCurrencyLocale();
        Currency c = Currency.getInstance(l);
        currencyTf.setText(c.getSymbol());

        amountTf = (EditText) findViewById(R.id.tx_amt_tf);
        tagsTf = (EditText) findViewById(R.id.tx_tags_tf);

        fromSpinnerAdapter = new ArrayAdapter<FManEntity>(this,
                R.layout.my_spinner_txt_layout);
        fromAcctSpinner = (Spinner) findViewById(R.id.from_acct_spinner);
        fromSpinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromAcctSpinner.setAdapter(fromSpinnerAdapter);
        fromAcctSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {

                if (position >= 0) {
                    Object obj = fromAcctSpinner.getAdapter().getItem(position);
                    if (obj != null) {
                        loadToAccounts((FManEntity) obj);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }

        });

        toSpinnerAdapter = new ArrayAdapter<FManEntity>(this,
                R.layout.my_spinner_txt_layout);
        toAcctSpinner = (Spinner) findViewById(R.id.to_acct_spinner);
        toSpinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toAcctSpinner.setAdapter(toSpinnerAdapter);

        txTypeSpinner = (Spinner) findViewById(R.id.tx_type_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.tx_types, R.layout.my_spinner_txt_layout);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        txTypeSpinner.setAdapter(adapter);
        txTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                    int position, long id) {

                if (position >= 0) {
                    Object obj = txTypeSpinner.getAdapter().getItem(position);
                    if (obj != null) {
                        loadFromAccounts(obj.toString());
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }

        });

        dateBtn = (Button) findViewById(R.id.tx_date_btn);
        dateBtn.setGravity(Gravity.CENTER);

        cameraBtn = (ImageButton) findViewById(R.id.add_img_btn);
        cameraBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(
                        android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, PIC_REQUEST_CODE);
            }
        });

        attchmntTf = (TextView) findViewById(R.id.attchmnt_lbl);
        attchmntTf.setMovementMethod(LinkMovementMethod.getInstance());
        Spannable txt = new SpannableString(getResources().getString(
                R.string.remove_attachment));
        txt.setSpan(new UnderlineSpan(), 0, txt.length(), 0);
        attchmntTf.setText(txt);
        attchmntTf.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (attachmentPath != null) {
                    deleteAttachmentFile(attachmentPath);
                    attchmntTf.setVisibility(View.INVISIBLE);
                }
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PIC_REQUEST_CODE) {
            if (data == null || !data.getExtras().containsKey("data")) {
                return;
            }
            try {
                Bitmap pic = (Bitmap) data.getExtras().get("data");
                saveAttachment(pic);
                attchmntTf.setVisibility(View.VISIBLE);
            }
            catch (Exception e) {
                Log.e(TAG, MiscUtils.stackTrace2String(e));
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Unable to save attachment", Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences appPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        String txDateStr = appPrefs.getString("LastTxDate", SessionManager
                .getDateFormat().format(new Date()));

        dateBtn.setText(txDateStr);
    }

    private void loadFromAccounts(String txType) {
        int[] acct = null;
        if (txType.equals(getString(R.string.tx_type_inc))) {
            acct = new int[] { AccountTypes.ACCT_TYPE_INCOME };
        }
        else if (txType.equals(getString(R.string.tx_type_exp))) {
            acct = new int[] { AccountTypes.ACCT_TYPE_CASH,
                    AccountTypes.ACCT_TYPE_LIABILITY };
        }
        try {
            loadSpinner(acct, fromSpinnerAdapter);

            if (fromSpinnerAdapter.getCount() > 0) {
                FManEntity first = fromSpinnerAdapter.getItem(0);
                loadToAccounts(first);
            }
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
    }

    protected void loadToAccounts(FManEntity fromAccount) {
        int[] payee = null;
        String txType = (String) txTypeSpinner.getSelectedItem();
        if (txType.equals(getString(R.string.tx_type_inc))) {
            payee = new int[] { AccountTypes.ACCT_TYPE_CASH };
        }
        else if (txType.equals(getString(R.string.tx_type_exp))) {
            payee = new int[] { AccountTypes.ACCT_TYPE_EXPENSE };
        }
        List<Long> matches = null;
        try {
            matches = FManEntityManager.getInstance().getBestmatchesForAccount(
                    (Long) fromAccount.getPK());
        }
        catch (DBException e1) {
            Log.e(TAG, MiscUtils.stackTrace2String(e1));
        }
        try {
            loadSpinner(payee, toSpinnerAdapter, matches);
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
    }

    protected void loadSpinner(int[] types, ArrayAdapter<FManEntity> adapter)
            throws Exception {

        adapter.clear();

        FManEntityManager em = FManEntityManager.getInstance();

        List<FManEntity> eList = em.getAccountsForTypes(types);
        if (types == null) {
            eList = em.getAccountsForTypes(types);
        }
        else {
            eList = em.getAccountsForTypesOrdered(types);
        }

        for (FManEntity object : eList) {
            adapter.add(object);
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

    public void gotoHomeScreen(View view) {
        Intent intent = new Intent(this, iFreeBudget.class);
        startActivity(intent);
    }

    public void showDatePickerDialog(View view) {
        super.showDialog(DATE_DIALOG_ID);
    }

    public void doCancelAction(View view) {
        super.finish();
    }

    public void saveTransaction(View view) {
        Account from = (Account) fromAcctSpinner.getSelectedItem();
        Account to = (Account) toAcctSpinner.getSelectedItem();

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

        // Save txDateStr to prefs
        savePreference(LastTxDate, txDateStr);
        //

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
        t.setAttachmentPath(this.attachmentPath);

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
                // Intent intent = new Intent(this,
                // ListTransactionsActivity.class);
                // startActivity(intent);
                // super.finish();
                nagAndClose();
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

    private void savePreference(String key, String val) {
        SharedPreferences appPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = appPrefs.edit();
        editor.putString(key, val);
        editor.commit();
        //
    }

    private boolean nagAndClose() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    amountTf.setText("");
                    tagsTf.setText("");
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    AddTransactionActivity.this.finish();
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(
                AddTransactionActivity.this);
        builder.setTitle("Add more transactions");
        builder.setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

        return false;
    }

    private void saveAttachment(Bitmap data) throws Exception {
        File sd = validateSDStorage();
        if (sd == null) {
            return;
        }

        File dirPath = new File(sd, ATTACHMENTS_DIR);

        if (!dirPath.exists()) {
            if (!dirPath.mkdir()) {
                return;
            }
        }

        File file = new File(dirPath, buildFileName());
        Log.i(TAG, "Saving attachment file: " + file.getAbsolutePath());
        FileOutputStream out = new FileOutputStream(file);
        data.compress(Bitmap.CompressFormat.PNG, 90, out);

        attachmentPath = file.getAbsolutePath();
        attchmntTf.setVisibility(View.VISIBLE);
        attchmntTf.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private File validateSDStorage() {
        File sd = Environment.getExternalStorageDirectory();

        if (sd == null) {
            return null;
        }

        String state = Environment.getExternalStorageState();

        if (!Environment.MEDIA_MOUNTED.equals(state)) {
            Toast.makeText(getApplicationContext(), "SD card is not available",
                    Toast.LENGTH_SHORT).show();
            return null;
        }

        if (!sd.canWrite()) {
            Toast.makeText(getApplicationContext(), "SD card is not writeable",
                    Toast.LENGTH_SHORT).show();
            return null;
        }
        return sd;
    }

    private String buildFileName() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        String fn = sdf.format(new Date());
        fn += ".png";
        return fn;
    }

    private void deleteAttachmentFile(String filePath) {
        File f = new File(filePath);
        if (f.exists()) {
            f.delete();
            Log.i(TAG, "Deleted file: " + f.getAbsolutePath());
        }
        else {
            Log.i(TAG, "File does not exist " + f.getAbsolutePath());
        }
    }
}
