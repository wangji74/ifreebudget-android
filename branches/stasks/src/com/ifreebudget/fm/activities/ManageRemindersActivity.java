package com.ifreebudget.fm.activities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.iFreeBudget;
import com.ifreebudget.fm.activities.wizards.budget.BudgetDetailsActivity;
import com.ifreebudget.fm.entity.DBException;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.Budget;
import com.ifreebudget.fm.entity.beans.FManEntity;
import com.ifreebudget.fm.entity.beans.ScheduleEntity;
import com.ifreebudget.fm.entity.beans.TaskEntity;
import com.ifreebudget.fm.services.SessionManager;
import com.ifreebudget.fm.utils.MiscUtils;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

public class ManageRemindersActivity extends ListActivity {
    private static final String TAG = "ManageRemindersActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.manage_reminders_layout);
    }

    @Override
    public void onResume() {
        super.onResume();
        FManEntityManager em = FManEntityManager.getInstance();
        try {
            List<FManEntity> tasks = em.getList(TaskEntity.class);
            if (tasks != null) {
                List<ListEntry> list = new ArrayList<ListEntry>();

                for (FManEntity fe : tasks) {
                    ListEntry le = new ListEntry((TaskEntity) fe);
                    if (le.valid) {
                        list.add(le);
                    }
                }
                ListEntry[] arr = new ListEntry[list.size()];
                list.toArray(arr);
                this.setListAdapter(new ArrayAdapter<ListEntry>(this,
                        R.layout.budget_list_row,
                        // android.R.layout.simple_list_item_1,
                        arr));
            }
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

    public void Add(View view) {
        Intent txIntent = new Intent(this, AddReminderActivity.class);
        startActivity(txIntent);
    }

    class ListEntry {
        TaskEntity entity;
        ScheduleEntity schedule;
        String nextTime;

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
                nextTime = SessionManager.getDateTimeFormat().format(
                        new Date(se.getNextRunTime()));
            }
            catch (DBException e) {
                valid = false;
                Log.e(TAG, MiscUtils.stackTrace2String(e));
            }

        }

        @Override
        public String toString() {
            StringBuilder ret = new StringBuilder(entity.getName());
            ret.append("\nNext: ");
            ret.append(nextTime);
            return ret.toString();
        }
    }
}
