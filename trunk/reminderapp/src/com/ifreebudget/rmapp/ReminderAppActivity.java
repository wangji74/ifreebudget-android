package com.ifreebudget.rmapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.actions.ActionRequest;
import com.ifreebudget.fm.actions.ActionResponse;
import com.ifreebudget.fm.entity.beans.FManEntity;
import com.ifreebudget.fm.entity.beans.TaskEntity;
import com.ifreebudget.fm.utils.MiscUtils;
import com.ifreebudget.rmapp.actions.DeleteReminderAction;
import com.ifreebudget.rmapp.activities.AddReminderActivity;
import com.ifreebudget.rmapp.activities.ManageTaskNotifsActivity;
import com.ifreebudget.rmapp.activities.utils.MyExpandableListAdapter;
import com.ifreebudget.rmapp.activities.utils.ReminderListEntry;
import com.ifreebudget.rmapp.activities.utils.ReminderListEntryComparator;
import com.ifreebudget.rmapp.entity.RMAppEntityManager;
import com.ifreebudget.rmapp.task.TaskRestarterService;

public class ReminderAppActivity extends Activity {

    private static final String TAG = "RMApp.ReminderAppActivity";

    public static final long NUM_SECONDS_IN_DAY = 24 * 60 * 60;

    private MyExpandableListAdapter listAdapter = null;

    private String MARKET_URI = "market://details?id=com.ifreebudget.rmapp";

    private ExpandableListView listView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.manage_reminders_layout);

        listView = (ExpandableListView) findViewById(R.id.reminder_list);
        listAdapter = new MyExpandableListAdapter(this);

        listView.setAdapter(listAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            loadTasks();
            int count = listAdapter.getGroupCount();
            if(count > 0) {
                listView.expandGroup(0);
            }
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
        case R.id.mItemNotifs:
            manageNotifications();
            return true;
        case R.id.mItemRate:
            gotoMarket();
            return true;
        default:
            return super.onOptionsItemSelected(item);

        }
    }

    /* End platform overrides */

    /* Menu item handlers */
    private void gotoMarket() {
        Intent goToMarket = null;
        goToMarket = new Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_URI));
        startActivity(goToMarket);
    }

    public void manageNotifications() {
        Intent txIntent = new Intent(this, ManageTaskNotifsActivity.class);
        startActivity(txIntent);
    }

    /* End menu item handlers */

    /* Button click handlers */

    public void doRefresh(View view) {
        Intent intent = new Intent(this, TaskRestarterService.class);
        intent.putExtra("resultreceiver", resultreceiver);
        startService(intent);
        Toast toast = Toast.makeText(getApplicationContext(),
                ("Refreshing tasks..."), Toast.LENGTH_SHORT);
        toast.show();
    }

    public void doAddReminder(View view) {
        Intent txIntent = new Intent(this, AddReminderActivity.class);
        startActivity(txIntent);
    }

    public void deleteReminder(View view) {
        ReminderListEntry lastSelected = listAdapter.getLastSelected();
        if (lastSelected == null) {
            return;
        }

        try {
            ActionRequest req = new ActionRequest();
            req.setActionName("deleteReminderAction");
            req.setProperty("TASKID", lastSelected.getEntity().getId());
            ActionResponse resp = new DeleteReminderAction().execute(req);
            if (resp.getErrorCode() == ActionResponse.NOERROR) {
                // listAdapter.remove(lastSelected);
                listAdapter.removeEntry(lastSelected);
                AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                AddReminderActivity.reRegisterAlarm(TAG, am,
                        getApplicationContext());
                lastSelected = null;
            }
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Error while delete task - " + e.getMessage(),
                    Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /* End button handlers */

    private void loadTasks() throws Exception {
        RMAppEntityManager em = RMAppEntityManager
                .getInstance(getApplicationContext());
        List<FManEntity> tasks = em.getList(TaskEntity.class);

        if (tasks != null && tasks.size() > 0) {
            List<ReminderListEntry> list = new ArrayList<ReminderListEntry>();

            for (FManEntity fe : tasks) {
                ReminderListEntry le = new ReminderListEntry((TaskEntity) fe);
                if (le.isValid()) {
                    list.add(le);
                }
            }

            Collections.sort(list, new ReminderListEntryComparator());

            listAdapter.setData(list);
        }
        else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No reminders...", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private ResultReceiver resultreceiver = new ResultReceiver(null) {
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.i(TAG, "Tasks refreshed... reloading list");
            runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        loadTasks();
                    }
                    catch (Exception e) {
                        Log.e(TAG, MiscUtils.stackTrace2String(e));
                    }
                }
            });
        }
    };

    class MyArrayAdapter extends ArrayAdapter<ReminderListEntry> {

        public MyArrayAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ReminderListEntry item = super.getItem(position);
            View v = getLayoutInflater().inflate(R.layout.reminder_list_row,
                    null);
            initializeListEntryLayout(item, v);
            return v;
        }
    }

    private void initializeListEntryLayout(ReminderListEntry item, View v) {
        Spanned details = Html.fromHtml(item.toString());

        TextView txDetailsView = (TextView) v
                .findViewById(R.id.reminder_details_lbl);
        txDetailsView.setText(details);
    }
}