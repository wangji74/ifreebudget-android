package com.ifreebudget.fm.activities;

import java.text.NumberFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.actions.ActionRequest;
import com.ifreebudget.fm.actions.ActionResponse;
import com.ifreebudget.fm.actions.DeleteTransactionAction;
import com.ifreebudget.fm.entity.DBException;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.Account;
import com.ifreebudget.fm.entity.beans.Transaction;
import com.ifreebudget.fm.services.SessionManager;
import com.ifreebudget.fm.utils.MiscUtils;

public class ViewTransactionActivity extends Activity {
    public static final String TXIDKEY = "TXID";
    private static final String TAG = "ViewTransactionActivity";
    private Long txId = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_tx_layout);

        Long txId = null;
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null && bundle.containsKey(TXIDKEY)) {
                txId = (Long) bundle.get(TXIDKEY);
            }
        }
        if (txId == null) {
            return;
        }
        this.txId = txId;

        try {
            FManEntityManager em = FManEntityManager.getInstance();
            Transaction t = em.getTransaction(txId);

            Account from = em.getAccount(t.getFromAccountId());
            Account to = em.getAccount(t.getToAccountId());

            TextView fromTf = (TextView) findViewById(R.id.from_acct_val);
            fromTf.setText(from.getAccountName());

            TextView toTf = (TextView) findViewById(R.id.to_acct_val);
            toTf.setText(to.getAccountName());

            String date = SessionManager.getDateFormat().format(
                    new Date(t.getTxDate()));
            TextView dateTf = (TextView) findViewById(R.id.tx_date_val);
            dateTf.setText(date);

            NumberFormat nf = NumberFormat.getInstance(SessionManager
                    .getCurrencyLocale());
            String amt = nf.format(t.getTxAmount());
            TextView amtTf = (TextView) findViewById(R.id.tx_amt_val);
            amtTf.setText(amt);

            TextView tagsTf = (TextView) findViewById(R.id.tx_tags_tf);
            tagsTf.setText(t.getTxNotes());
        }
        catch (DBException e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
    }

    public void deleteTransaction(View view) {
        try {
            ActionRequest req = new ActionRequest();
            req.setActionName("deleteTransactionAction");
            req.setProperty("TXID", txId);

            ActionResponse resp = new DeleteTransactionAction()
                    .executeAction(req);
            if (resp.getErrorCode() == ActionResponse.NOERROR) {
                super.finish();
            }
            else {
                Toast toast = Toast.makeText(getApplicationContext(),
                        resp.getErrorMessage(), Toast.LENGTH_SHORT);
                toast.show();
                return;
            }
        }
        catch (Exception e) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Unable to delete transaction", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
    }

    public void doCancelAction(View view) {
        super.finish();
    }
}
