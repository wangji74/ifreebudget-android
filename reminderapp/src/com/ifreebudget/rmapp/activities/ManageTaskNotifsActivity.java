package com.ifreebudget.rmapp.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ifreebudget.fm.entity.DBException;
import com.ifreebudget.fm.entity.beans.FManEntity;
import com.ifreebudget.fm.entity.beans.TaskNotification;
import com.ifreebudget.fm.utils.MiscUtils;
import com.ifreebudget.rmapp.R;
import com.ifreebudget.rmapp.ReminderAppActivity;
import com.ifreebudget.rmapp.activities.utils.NotificationListEntry;
import com.ifreebudget.rmapp.entity.RMAppEntityManager;

public class ManageTaskNotifsActivity extends ListActivity {
    private static final String EMPTY_LIST_MSG = "No pending notifications...";

    private static final String TAG = "RMApp.ManageTaskNotifsActivity";

    public static final String NOTIFIDKEY = "NOTIFIDKEY";

    private ArrayAdapter<NotificationListEntry> adapter = null;

    private View lastEditCtrlPanel = null;

    private NotificationListEntry lastSelected = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.manage_task_notifs_layout);
    }

    @Override
    public void onResume() {
        super.onResume();
        RMAppEntityManager em = RMAppEntityManager.getInstance();
        if (em == null) {
            Log.w(TAG, "Null FManEntityManager, re-initializing");
            em = RMAppEntityManager.getInstance(getApplicationContext());
        }

        try {
            List<FManEntity> notifs = em.getList(TaskNotification.class);

            if (notifs != null && notifs.size() > 0) {
                List<NotificationListEntry> list = new ArrayList<NotificationListEntry>();

                for (FManEntity fe : notifs) {
                    NotificationListEntry le = new NotificationListEntry(
                            (TaskNotification) fe);
                    if (le.isValid()) {
                        list.add(le);
                    }
                }

                if (list.size() == 0) {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            (EMPTY_LIST_MSG), Toast.LENGTH_LONG);
                    toast.show();
                }

                if (adapter == null) {
                    adapter = new MyArrayAdapter(this,
                            R.layout.reminder_notif_list_row);
                    this.setListAdapter(adapter);
                }
                addItems(list);
            }
            else {
                Toast toast = Toast.makeText(getApplicationContext(),
                        (EMPTY_LIST_MSG), Toast.LENGTH_LONG);
                toast.show();
            }
        }
        catch (DBException e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
    }

    private void addItems(List<NotificationListEntry> list) {
        adapter.clear();
        for (NotificationListEntry entry : list) {
            adapter.add(entry);
        }
    }

    public void gotoHomeScreen(View view) {
        Intent intent = new Intent(this, ReminderAppActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Object obj = this.getListAdapter().getItem(position);
        if (obj instanceof NotificationListEntry) {
            if (lastEditCtrlPanel != null) {
                TranslateAnimation slide = new TranslateAnimation(0, 0, 0, 100);
                slide.setDuration(300);
                slide.setFillAfter(true);
                slide.setInterpolator(new AccelerateInterpolator());
                lastEditCtrlPanel.startAnimation(slide);
                lastEditCtrlPanel.setVisibility(View.GONE);
            }
            lastSelected = (NotificationListEntry) obj;
            lastEditCtrlPanel = (View) v.findViewById(R.id.notif_ctrl_panel);

            TranslateAnimation slide = new TranslateAnimation(0, 0, 100, 0);
            slide.setDuration(300);
            slide.setFillAfter(true);
            slide.setInterpolator(new AccelerateInterpolator());
            lastEditCtrlPanel.startAnimation(slide);
            lastEditCtrlPanel.setVisibility(View.VISIBLE);
        }
    }

    class MyArrayAdapter extends ArrayAdapter<NotificationListEntry> {

        public MyArrayAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            NotificationListEntry item = super.getItem(position);
            View v = getLayoutInflater().inflate(
                    R.layout.reminder_notif_list_row, null);
            initializeListEntryLayout(item, v);
            return v;
        }
    }

    private void initializeListEntryLayout(NotificationListEntry item, View v) {
        Spanned details = Html.fromHtml(item.toString());

        TextView txDetailsView = (TextView) v
                .findViewById(R.id.reminder_notif_details_lbl);
        txDetailsView.setText(details);
    }

    public void dismissNotification(View view) {
        if (lastSelected == null) {
            return;
        }
        TaskNotification n = lastSelected.getNotif();
        try {
            int numDel = RMAppEntityManager.getInstance().deleteEntity(n);
            if (numDel == 1) {
                adapter.remove(lastSelected);
            }
            else {
                Log.e(TAG,
                        "Error while deleting task notification, expected 1 got "
                                + numDel);
            }
        }
        catch (DBException e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
    }
}
