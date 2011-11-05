package com.ifreebudget.fm.scheduler.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.app.AlarmManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ifreebudget.fm.activities.AddReminderActivity;
import com.ifreebudget.fm.entity.DBException;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.ConstraintEntity;
import com.ifreebudget.fm.entity.beans.FManEntity;
import com.ifreebudget.fm.entity.beans.ScheduleEntity;
import com.ifreebudget.fm.entity.beans.TaskEntity;
import com.ifreebudget.fm.utils.MiscUtils;

public class TaskRestarterService extends IntentService {
    private static final String TAG = "TaskRestarterService";

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

                    Log.i(TAG,
                            te.getName() + " last:"
                                    + new Date(se.getLastRunTime()) + " next:"
                                    + new Date(se.getNextRunTime()));

                    ConstraintEntity ce = em.getConstraintByScheduleId(se
                            .getId());

                    Schedule sch = TaskUtils.rebuildSchedule(
                            new Date(se.getNextRunTime()),
                            new Date(te.getEndTime()), se, ce);

                    Date next = new Date(se.getNextRunTime());
                    Date now = new Date();
                    Date end = new Date(te.getEndTime());

                    // Task has ended
                    if (now.after(end)) {
                        continue;
                    }

                    // Some schedules were missed
                    if (now.after(next)) {
                        Log.i(TAG,
                                "Some schedules were missed for task: "
                                        + te.getName());
                        ScheduledTx t = new ScheduledTx(te.getName(),
                                te.getBusinessObjectId());

                        t.setSchedule(sch);

                        List<Date> all = sch.getRunTimesBetween(
                                sch.getStartTime(), sch.getEndTime());
                        List<Date> missed = new ArrayList<Date>();

                        Iterator<Date> iter = all.iterator();

                        Date last = new Date(se.getLastRunTime());

                        /*
                         * Special case 1 : Task never ran. Add the task start
                         * time as first missed
                         */
                        if (se.getLastRunTime() == null
                                || se.getLastRunTime().longValue() == 0l) {
                            missed.add(new Date(te.getStartTime()));
                        }
                        while (iter.hasNext()) {
                            Date d = iter.next();
                            if (d.before(last)) {
                                continue;
                            }
                            else {
                                if (d.before(now)) {
                                    // createMissedNotif(d);
                                    missed.add(d);
                                }
                                else {
                                    next = d;
                                    break;
                                }
                            }
                        }

                        if (missed != null && missed.size() > 0) {
                            Log.i(TAG, te.getName() + ", num missed notifs: "
                                    + missed.size() + ":" + missed);
                            for (Date d : missed) {
                                TaskUtils.createNotificationEntity(TAG,
                                        context, te.getId(), d.getTime());
                            }
                        }

                        se.setNextRunTime(next.getTime());

                        em.updateEntity(se);
                    }
                }
                catch (Exception e1) {
                    Log.e(TAG, MiscUtils.stackTrace2String(e1));
                }
            }
        }
        catch (DBException e) {
            Log.e(TAG, e.getMessage());
        }
        finally {
            AlarmManager am = (AlarmManager) context
                    .getSystemService(Context.ALARM_SERVICE);
            try {
                AddReminderActivity.reRegisterAlarm(TAG, am, context);
            }
            catch (Exception e) {
                Log.e(TAG, MiscUtils.stackTrace2String(e));
            }
        }
    }
}
