package com.ifreebudget.fm.activities;

import static com.ifreebudget.fm.utils.Messages.tr;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.iFreeBudget;
import com.ifreebudget.fm.entity.DBException;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.FManEntity;
import com.ifreebudget.fm.entity.beans.TaskEntity;
import com.ifreebudget.fm.entity.beans.TaskNotification;
import com.ifreebudget.fm.services.SessionManager;
import com.ifreebudget.fm.utils.MiscUtils;

public class ManageTaskNotificationActivity extends ListActivity {
    private static final String TAG = "ManageTaskNotificationActivity";

    public static final String NOTIFIDKEY = "NOTIFIDKEY";
    
    private static final String EMPTY_LIST_MSG = "No pending notifications...";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.manage_task_notifs_layout);
    }

    @Override
    public void onResume() {
        super.onResume();
        FManEntityManager em = FManEntityManager.getInstance();
        if (em == null) {
            Log.w(TAG, "Null FManEntityManager, re-initializing");
            em = FManEntityManager.getInstance(getApplicationContext());
        }

        ListEntry[] arr = null;
        try {
            List<FManEntity> notifs = em.getList(TaskNotification.class);
            if (notifs != null && notifs.size() > 0) {
                List<ListEntry> list = new ArrayList<ListEntry>();

                for (FManEntity fe : notifs) {
                    ListEntry le = new ListEntry((TaskNotification) fe);
                    if (le.valid) {
                        list.add(le);
                    }
                }

                if (list.size() == 0) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            tr(EMPTY_LIST_MSG), Toast.LENGTH_LONG);
                    toast.show();
                }
                arr = new ListEntry[list.size()];
                list.toArray(arr);
            }
            else {
                Toast toast = Toast.makeText(getApplicationContext(),
                        tr(EMPTY_LIST_MSG), Toast.LENGTH_LONG);
                toast.show();
                arr = new ListEntry[0];
            }
            this.setListAdapter(new ArrayAdapter<ListEntry>(this,
                    R.layout.budget_list_row, arr));
        }
        catch (DBException e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
    }

    public void gotoHomeScreen(View view) {
        Intent intent = new Intent(this, iFreeBudget.class);
        startActivity(intent);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Object obj = this.getListAdapter().getItem(position);

        ListEntry e = (ListEntry) obj;

        Intent intent = new Intent(this, QuickAddTransactionActivity.class);

        intent.putExtra(NOTIFIDKEY, e.notif.getId());

        intent.putExtra(QuickAddTransactionActivity.TXIDKEY,
                e.taskEntity.getBusinessObjectId());

        startActivity(intent);
    }

    /* Inner class for list entry */
    class ListEntry {
        TaskNotification notif;
        TaskEntity taskEntity;
        boolean valid = true;
        String time = null;

        ListEntry(TaskNotification notif) {
            this.notif = notif;
            FManEntityManager em = FManEntityManager.getInstance();
            try {
                taskEntity = em.getTask(notif.getTaskId());
                if (taskEntity == null) {
                    Log.e(TAG,
                            "Orphan notification found...deleting "
                                    + notif.getId());
                    em.deleteEntity(notif);
                    valid = false;
                    return;
                }
                time = SessionManager.getDateTimeFormat().format(
                        new Date(notif.getTimestamp()));
            }
            catch (DBException e) {
                valid = false;
                Log.e(TAG, MiscUtils.stackTrace2String(e));
            }

        }

        @Override
        public String toString() {
            StringBuilder ret = new StringBuilder(taskEntity.getName());
            ret.append("\n\t @ ");
            ret.append(time);
            return ret.toString();
        }
    }
    /* End inner class */
}
