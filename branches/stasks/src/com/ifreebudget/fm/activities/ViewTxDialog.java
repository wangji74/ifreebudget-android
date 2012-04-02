package com.ifreebudget.fm.activities;

import java.io.File;
import java.text.NumberFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.activities.ListTransactionsActivity.TxHolder;
import com.ifreebudget.fm.activities.utils.DialogCallback;
import com.ifreebudget.fm.entity.beans.Account;
import com.ifreebudget.fm.services.SessionManager;

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

        ImageButton attchmntBtn = (ImageButton) findViewById(R.id.attachmnt_btn);
        attchmntBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showAttachment(v);
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

        // TextView attachmentTf = (TextView) findViewById(R.id.attchmnt_val);
        // attachmentTf.setMovementMethod(LinkMovementMethod.getInstance());
        // Spannable txt = new SpannableString(getContext().getResources()
        // .getString(R.string.attachment));
        // txt.setSpan(new UnderlineSpan(), 0, txt.length(), 0);
        // attachmentTf.setText(txt);

        // Log.i("ViewTxDialog", "Attachment" + holder.t.getAttachmentPath());
        // if (holder.t.getAttachmentPath() != null) {
        // attachmentTf.setVisibility(View.VISIBLE);
        // }
        // else {
        // attachmentTf.setVisibility(View.GONE);
        // }
        // Log.i("ViewTxDialog", "Text field is " +
        // attachmentTf.getVisibility());
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
    
    private void showAttachment(View view) {
        if(holder == null) {
            Toast toast = Toast.makeText(getContext(),
                    "Unable to show attachment, try again", Toast.LENGTH_SHORT);
            
            return;
        }
        
        String file = holder.t.getAttachmentPath();
        if(file == null || file.length() == 0) {
            Toast toast = Toast.makeText(getContext(),
                    "Attachment not found", Toast.LENGTH_SHORT);
            
            return;
        }
        
        File f = new File(file);
        if(!f.exists()) {
            Toast toast = Toast.makeText(getContext(),
                    "Attachment not found", Toast.LENGTH_SHORT);
            
            return;
        }
        
        Bitmap bm = BitmapFactory.decodeFile(f.getAbsolutePath());
        
        LayoutInflater inflater = this.getLayoutInflater();
        View layout = inflater.inflate(R.layout.image_viewer_layout, null);
        
        ImageView iv = (ImageView) layout.findViewById(R.id.img_holder);
        iv.setImageBitmap(bm);
        AlertDialog.Builder adb = new AlertDialog.Builder(this.getContext());
        adb.setView(layout);
        adb.show();
    }    
}
