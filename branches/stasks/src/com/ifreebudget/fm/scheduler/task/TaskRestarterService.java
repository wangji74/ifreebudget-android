package com.ifreebudget.fm.scheduler.task;

import static com.ifreebudget.fm.utils.Messages.tr;

import java.util.Date;
import java.util.List;

import android.app.AlarmManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.ifreebudget.fm.activities.AddReminderActivity;
import com.ifreebudget.fm.entity.DBException;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.ConstraintEntity;
import com.ifreebudget.fm.entity.beans.FManEntity;
import com.ifreebudget.fm.entity.beans.ScheduleEntity;
import com.ifreebudget.fm.entity.beans.TaskEntity;
import com.ifreebudget.fm.utils.MiscUtils;

public class TaskRestarterService extends IntentService {
    private static final String TAG = "AlarmManagerHelper";

    public TaskRestarterService() {
        super("TaskRestarterService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        reRegisterTasks(getApplicationContext());
    }

    public void reRegisterTasks(Context context) {
        FManEntityManager em = FManEntityManager.getInstance();
        try {
            List<FManEntity> list = em.getList(TaskEntity.class);
            for (FManEntity e : list) {
                TaskEntity te = (TaskEntity) e;

                try {
                    ScheduleEntity se = em.getScheduleByTaskId(te.getId());

                    Date next = new Date(se.getNextRunTime());
                    Date now = new Date();

                    if (now.after(next)) {
                        ConstraintEntity ce = em.getConstraintByScheduleId(se
                                .getId());

                        ScheduledTx t = new ScheduledTx(te.getName(),
                                te.getBusinessObjectId());

                        Schedule sch = TaskUtils.rebuildSchedule(
                                new Date(se.getNextRunTime()),
                                new Date(te.getEndTime()), se, ce);

                        t.setSchedule(sch);

                        se.setLastRunTime(new Date().getTime());

                        next = sch.getNextRunTimeAfter(now);

                        se.setNextRunTime(next.getTime());

                        em.updateEntity(se);

                    }

                    reSchedule(context, te.getId(), next);
                }
                catch (Exception e1) {
                    Log.e(TAG, MiscUtils.stackTrace2String(e1));
                }
            }
        }
        catch (DBException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private static void reSchedule(Context context, Long taskDbId,
            Date nextRunTime) {
        try {
            AlarmManager am = (AlarmManager) context
                    .getSystemService(Context.ALARM_SERVICE);
            AddReminderActivity.scheduleEvent(am, context, taskDbId,
                    nextRunTime);
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
            Toast toast = Toast.makeText(context, tr("Task schedule failed - "
                    + e.getMessage()), Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}