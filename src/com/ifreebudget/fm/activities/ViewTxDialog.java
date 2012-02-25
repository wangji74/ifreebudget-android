package com.ifreebudget.fm.activities;

import java.text.NumberFormat;
import java.util.Date;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.actions.ActionRequest;
import com.ifreebudget.fm.actions.ActionResponse;
import com.ifreebudget.fm.actions.DeleteTransactionAction;
import com.ifreebudget.fm.activities.ListTransactionsActivity.TxHolder;
import com.ifreebudget.fm.activities.utils.DialogCallback;
import com.ifreebudget.fm.entity.beans.Account;
import com.ifreebudget.fm.services.SessionManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class ViewTxDialog extends Dialog {
    private TxHolder holder;
    private DialogCallback callback;

    public static final int ADD_REMINDER = 1;
    public static final int EDIT_TX = 2;
    public static final int DELETE_TX = 3;
    public static final int NO_ACTION = 4;

    public ViewTxDialog(Context context, DialogCallback callback) {
        super(context);
        super.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.setContentView(R.layout.view_tx_layout);
        this.callback = callback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ImageButton editBtn = (ImageButton) findViewById(R.id.edit_tx_btn);
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTransaction(v);
            }
        });

        ImageButton remBtn = (ImageButton) findViewById(R.id.reminder_tx_btn);
        remBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                addReminder(v);
            }
        });

        ImageButton delBtn = (ImageButton) findViewById(R.id.del_tx_btn);
        delBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                nagAndDelete(v);
            }
        });
    }

    private boolean nagAndDelete(final View view) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    deleteTransaction(view);
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("Delete transaction");
        builder.setMessage("Are you sure?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();

        return false;
    }

    public void initialize(TxHolder holder) {
        if (holder == null) {
            return;
        }
        this.holder = holder;
        Account from = holder.fromAcct;
        Account to = holder.toAcct;

        TextView fromTf = (TextView) findViewById(R.id.from_acct_val);
        fromTf.setText(from.getAccountName());

        TextView toTf = (TextView) findViewById(R.id.to_acct_val);
        toTf.setText(to.getAccountName());

        String date = SessionManager.getDateFormat().format(
                new Date(holder.t.getTxDate()));
        TextView dateTf = (TextView) findViewById(R.id.tx_date_val);
        dateTf.setText(date);

        NumberFormat nf = NumberFormat.getInstance(SessionManager
                .getCurrencyLocale());
        String amt = nf.format(holder.t.getTxAmount());
        TextView amtTf = (TextView) findViewById(R.id.tx_amt_val);
        amtTf.setText(amt);

        TextView tagsTf = (TextView) findViewById(R.id.tx_tags_tf);
        tagsTf.setText(holder.t.getTxNotes());
    }

    private void editTransaction(View view) {
        dismiss();
        callback.onDismiss(EDIT_TX, holder);
    }

    private void addReminder(View view) {
        dismiss();
        callback.onDismiss(ADD_REMINDER, holder);
    }

    private void deleteTransaction(View view) {
        dismiss();
        callback.onDismiss(DELETE_TX, holder);
    }
}
