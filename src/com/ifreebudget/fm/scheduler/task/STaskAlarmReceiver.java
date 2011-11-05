package com.ifreebudget.fm.scheduler.task;

import static com.ifreebudget.fm.utils.Messages.tr;

import java.util.Date;
import java.util.List;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.activities.AddReminderActivity;
import com.ifreebudget.fm.activities.AddTransactionActivity;
import com.ifreebudget.fm.activities.ManageTaskNotificationActivity;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.ConstraintEntity;
import com.ifreebudget.fm.entity.beans.FManEntity;
import com.ifreebudget.fm.entity.beans.ScheduleEntity;
import com.ifreebudget.fm.entity.beans.TaskEntity;
import com.ifreebudget.fm.entity.beans.TaskNotification;
import com.ifreebudget.fm.utils.MiscUtils;

public class STaskAlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "STaskAlarmReceiver";

    private static final int NOTIFID = 100;

    @Override
    public void onReceive(Context context, Intent intent) {
        Long id = intent.getLongExtra(AddReminderActivity.TASK_ALARM_ID, -1);
        Log.d(TAG, "Alarm received: " + id + ":" + intent.getAction());
        if (id == -1) {
            Log.e(TAG, "Invalid alarm received: " + id);
            return;
        }
        try {
            FManEntityManager em = FManEntityManager.getInstance(context);
            String filter = String.format(" WHERE NEXTRT = %d", id);

            List<FManEntity> schList = em.getList(ScheduleEntity.class, filter);
            if (schList == null || schList.size() == 0) {
                Log.e(TAG, "Alarm received for next run time " + new Date(id)
                        + " which doesnot exist");
                context.startService(new Intent(context,
                        TaskRestarterService.class));
                return;
            }

            for (FManEntity e : schList) {
                ScheduleEntity se = (ScheduleEntity) e;
                updateTaskAndNotify(context, em, se.getScheduledTaskId());
            }
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
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

    private void updateTaskAndNotify(Context context, FManEntityManager em,
            Long id) throws Exception {
        TaskEntity taskEntity = em.getTask(id);

        TaskUtils.createNotificationEntity(TAG, context, taskEntity.getId(),
                System.currentTimeMillis());

        ScheduleEntity scheduleEntity = em.getScheduleByTaskId(taskEntity
                .getId());

        ConstraintEntity constraintEntity = em
                .getConstraintByScheduleId(scheduleEntity.getId());

        ScheduledTx t = new ScheduledTx(taskEntity.getName(),
                taskEntity.getBusinessObjectId());

        Schedule sch = TaskUtils.rebuildSchedule(
                new Date(scheduleEntity.getNextRunTime()),
                new Date(taskEntity.getEndTime()), scheduleEntity,
                constraintEntity);

        t.setSchedule(sch);

        scheduleEntity.setLastRunTime(new Date().getTime());
        scheduleEntity.setNextRunTime(sch.getNextRunTime().getTime());

        em.updateEntity(scheduleEntity);

        String tickerText = taskEntity.getName() + " reminder";
        sendNotification(context, ManageTaskNotificationActivity.class,
                tickerText, "iFreeBudget", tickerText, 1, true, false);
    }

    public static void sendNotification(Context caller,
            Class<?> activityToLaunch, String tickerText, String title,
            String msg, int numberOfEvents, boolean flashLed, boolean vibrate) {
        NotificationManager notifier = (NotificationManager) caller
                .getSystemService(Context.NOTIFICATION_SERVICE);

        final Notification notify = new Notification(R.drawable.icon, "",
                System.currentTimeMillis());

        notify.icon = R.drawable.icon;
        notify.tickerText = tickerText;
        notify.when = System.currentTimeMillis();
        notify.number = numberOfEvents;
        notify.flags |= Notification.FLAG_AUTO_CANCEL;

        if (flashLed) {
            notify.flags |= Notification.FLAG_SHOW_LIGHTS;
            notify.ledARGB = Color.CYAN;
            notify.ledOnMS = 500;
            notify.ledOffMS = 500;
        }

        if (vibrate) {
            notify.defaults |= Notification.DEFAULT_VIBRATE;
        }

        Intent toLaunch = new Intent(caller, activityToLaunch);
        PendingIntent intentBack = PendingIntent.getActivity(caller, 0,
                toLaunch, 0);

        notify.setLatestEventInfo(caller, title, msg, intentBack);
        notifier.notify(NOTIFID, notify);
    }

}