package com.ifreebudget.fm.activities;

import static com.ifreebudget.fm.utils.Messages.tr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.Html;
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
import com.ifreebudget.fm.entity.beans.ScheduleEntity;
import com.ifreebudget.fm.entity.beans.TaskEntity;
import com.ifreebudget.fm.scheduler.task.TaskRestarterService;
import com.ifreebudget.fm.services.SessionManager;
import com.ifreebudget.fm.utils.MiscUtils;

public class ManageRemindersActivity extends ListActivity {
    private static final String TAG = "ManageRemindersActivity";
    public static final String REMINDERIDKEY = "REMINDERIDKEY";

    public static final long NUM_SECONDS_IN_DAY = 24 * 60 * 60;

    private ArrayAdapter<ListEntry> adapter = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.manage_reminders_layout);
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            loadTasks();
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
    }

    private void loadTasks() throws Exception {
        FManEntityManager em = FManEntityManager.getInstance();
        List<FManEntity> tasks = em.getList(TaskEntity.class);

        if (tasks != null && tasks.size() > 0) {
            List<ListEntry> list = new ArrayList<ListEntry>();

            for (FManEntity fe : tasks) {
                ListEntry le = new ListEntry((TaskEntity) fe);
                if (le.valid) {
                    list.add(le);
                }
            }

            Collections.sort(list, new ListEntryComparator());

            if (adapter != null) {
                adapter.clear();
                for (ListEntry entry : list) {
                    adapter.add(entry);
                }
            }
            else {
                adapter = new ArrayAdapter<ListEntry>(this,
                        R.layout.budget_list_row, list);
                this.setListAdapter(adapter);
            }
        }
        else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    tr("No reminders..."), Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private class ListEntryComparator implements Comparator<ListEntry> {

        @Override
        public int compare(ListEntry object1, ListEntry object2) {
            return object1.nextTime.compareTo(object2.nextTime);
        }
    }

    public void gotoHomeScreen(View view) {
        Intent intent = new Intent(this, iFreeBudget.class);
        startActivity(intent);
    }

    public void Add(View view) {
        Intent txIntent = new Intent(this, AddReminderActivity.class);
        startActivity(txIntent);
    }

    public void Refresh(View view) {
        Intent intent = new Intent(this, TaskRestarterService.class);
        intent.putExtra("resultreceiver", resultreceiver);
        startService(intent);
        Toast toast = Toast.makeText(getApplicationContext(),
                tr("Refreshing tasks..."), Toast.LENGTH_SHORT);
        toast.show();
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

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Object obj = this.getListAdapter().getItem(position);

        ListEntry e = (ListEntry) obj;

        Intent intent = new Intent(this, ViewReminderActivity.class);

        intent.putExtra(REMINDERIDKEY, e.entity.getId());

        startActivity(intent);
    }

    class ListEntry {
        TaskEntity entity;
        ScheduleEntity schedule;
        Date nextTime;

        boolean valid = true;

        ListEntry(TaskEntity entity) {
            this.entity = entity;
            FManEntityManager em = FManEntityManager.getInstance();
            try {
                List<FManEntity> list = em.getList(ScheduleEntity.class,
                        " WHERE SCHTASKID = " + entity.getId());
                if (list == null) {
                    valid = false;
                    return;
                }
                if (list.size() != 1) {
                    Log.e(TAG,
                            "Invalid schedule for task: " + entity.getName()
                                    + "[" + entity.getId() + "], found : "
                                    + list.size() + " schedules");
                    valid = false;
                    return;
                }
                ScheduleEntity se = (ScheduleEntity) list.get(0);
                nextTime = new Date(se.getNextRunTime());
            }
            catch (DBException e) {
                valid = false;
                Log.e(TAG, MiscUtils.stackTrace2String(e));
            }

        }

        @Override
        public String toString() {
            StringBuilder ret = new StringBuilder(entity.getName());

            Date now = new Date();
            if (nextTime.before(now)) {
                ret.append("\n\n\tTask ended");
            }
            else {
                String disp = SessionManager.getDateTimeFormat().format(
                        nextTime);
                ret.append("\n\n" + disp + "\n( " + getDateDiff(nextTime)
                        + " )");
            }
            return ret.toString();
        }

        private String getDateDiff(Date ref) {
            Date now = new Date();

            long diff = (ref.getTime() - now.getTime()) / 1000;

            return calcDiff(diff);
        }

        private String calcDiff(long timeInSeconds) {
            long days = timeInSeconds / NUM_SECONDS_IN_DAY;
            String ret = (days > 0 ? days + " days , " : "");
            ret += calcHMS(timeInSeconds % NUM_SECONDS_IN_DAY);

            return ret;
        }

        private String calcHMS(long timeInSeconds) {
            long hours, minutes, seconds;
            hours = timeInSeconds / 3600;
            timeInSeconds = timeInSeconds - (hours * 3600);
            minutes = timeInSeconds / 60;
            timeInSeconds = timeInSeconds - (minutes * 60);
            seconds = timeInSeconds;

            StringBuilder ret = new StringBuilder();
            ret.append(hours > 0 ? hours + " hours , " : "");
            ret.append(minutes > 0 ? minutes + " minutes" : "");
            if (hours == 0 && minutes == 0) {
                ret.append(seconds > 0 ? seconds + " seconds" : "");
            }
            return ret.toString();
        }
    }
}
