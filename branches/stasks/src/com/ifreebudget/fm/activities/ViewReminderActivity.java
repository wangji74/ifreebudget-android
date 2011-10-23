package com.ifreebudget.fm.activities;

import static com.ifreebudget.fm.utils.Messages.tr;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
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
import com.ifreebudget.fm.entity.beans.TaskEntity;
import com.ifreebudget.fm.entity.beans.Transaction;
import com.ifreebudget.fm.services.SessionManager;
import com.ifreebudget.fm.utils.MiscUtils;

public class ViewReminderActivity extends Activity {
    private static final String TAG = "ViewTransactionActivity";
    private Long txId = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_reminder_layout);

        Long txId = null;
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null
                    && bundle
                            .containsKey(ManageRemindersActivity.REMINDERIDKEY)) {
                txId = (Long) bundle.get(ManageRemindersActivity.REMINDERIDKEY);
            }
        }
        if (txId == null) {
            return;
        }
        this.txId = txId;

        try {
            SimpleDateFormat sdf = SessionManager.getDateFormat();
            FManEntityManager em = FManEntityManager.getInstance();
            TaskEntity te = em.getTask(txId);

            TextView fromTf = (TextView) findViewById(R.id.name_val);
            fromTf.setText(te.getName());

            TextView toTf = (TextView) findViewById(R.id.start_val);
            toTf.setText(sdf.format(new Date(te.getStartTime())));

            TextView dateTf = (TextView) findViewById(R.id.end_val);
            dateTf.setText(sdf.format(new Date(te.getEndTime())));

            Transaction tx = getBusinessObject(te.getBusinessObjectId());

            if (tx != null) {
                Account f = em.getAccount(tx.getFromAccountId());
                Account t = em.getAccount(tx.getToAccountId());

                StringBuilder notes = new StringBuilder();
                notes.append("Scheduled transaction: for: ")
                        .append(t.getAccountName()).append(" payee: ")
                        .append(f.getAccountName());
                notes.append("\n\n");

                NumberFormat nf = NumberFormat.getInstance(SessionManager
                        .getCurrencyLocale());

                notes.append(nf.format(tx.getTxAmount()));
                TextView remNotesTf = (TextView) findViewById(R.id.rem_notes_tf);
                remNotesTf.setText(notes.toString());
            }
        }
        catch (DBException e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
    }

    private Transaction getBusinessObject(Long busObjId) {
        if (busObjId == null || busObjId == 0l) {
            return null;
        }
        try {
            Transaction t = FManEntityManager.getInstance().getTransaction(
                    busObjId);

            return t;
        }
        catch (DBException e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
            Toast toast = Toast.makeText(getApplicationContext(),
                    tr("Transaction not found - " + e.getMessage()),
                    Toast.LENGTH_SHORT);
            toast.show();
            return null;
        }
    }

    public void deleteReminder(View view) {
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
