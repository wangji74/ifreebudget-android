package com.ifreebudget.rmapp.task;

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
import com.ifreebudget.fm.entity.beans.ConstraintEntity;
import com.ifreebudget.fm.entity.beans.FManEntity;
import com.ifreebudget.fm.entity.beans.ScheduleEntity;
import com.ifreebudget.fm.entity.beans.TaskEntity;
import com.ifreebudget.fm.entity.beans.TaskNotification;
import com.ifreebudget.fm.scheduler.task.BasicTask;
import com.ifreebudget.fm.scheduler.task.Schedule;
import com.ifreebudget.fm.scheduler.task.TaskUtils;
import com.ifreebudget.fm.utils.MiscUtils;
import com.ifreebudget.rmapp.ReminderAppActivity;
import com.ifreebudget.rmapp.activities.AddReminderActivity;
import com.ifreebudget.rmapp.activities.ManageTaskNotifsActivity;
import com.ifreebudget.rmapp.entity.RMAppEntityManager;

public class STaskAlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "RMApp.STaskAlarmReceiver";

    private static final int NOTIFID = 100;

    @Override
    public void onReceive(Context context, Intent intent) {
        Long id = intent.getLongExtra(AddReminderActivity.TASK_ALARM_ID, -1);
        Log.d(TAG, "Alarm received: " + new Date(id) + ":" + intent.getAction());
        if (id == -1) {
            Log.e(TAG, "Invalid alarm received: " + id);
            return;
        }
        try {
            RMAppEntityManager em = RMAppEntityManager.getInstance(context);
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

    private void updateTaskAndNotify(Context context, RMAppEntityManager em,
            Long id) throws Exception {
        TaskEntity taskEntity = em.getTask(id);

        RMAppTaskUtils.createNotificationEntity(TAG, context,
                taskEntity.getId(), System.currentTimeMillis());

        ScheduleEntity scheduleEntity = em.getScheduleByTaskId(taskEntity
                .getId());

        Log.d(TAG,
                "Creating notification for taskId: " + id + ", name: "
                        + taskEntity.getName() + ", rt: "
                        + new Date(scheduleEntity.getNextRunTime()));

        ConstraintEntity constraintEntity = em
                .getConstraintByScheduleId(scheduleEntity.getId());

        BasicTask t = new BasicTask(taskEntity.getName());

        Schedule sch = TaskUtils.rebuildSchedule(
                new Date(scheduleEntity.getNextRunTime()),
                new Date(taskEntity.getEndTime()), scheduleEntity,
                constraintEntity);

        t.setSchedule(sch);

        scheduleEntity.setLastRunTime(new Date().getTime());
        scheduleEntity.setNextRunTime(sch.getNextRunTime().getTime());

        em.updateEntity(scheduleEntity);

        String appName = context.getResources().getString(R.string.app_name);
        String tickerText = taskEntity.getName() + " reminder";
        sendNotification(context, ManageTaskNotifsActivity.class, tickerText,
                appName, tickerText, 1, true, true);
    }

    public static void sendNotification(Context caller,
            Class<?> activityToLaunch, String tickerText, String title,
            String msg, int numberOfEvents, boolean flashLed, boolean vibrate) {
        NotificationManager notifier = (NotificationManager) caller
                .getSystemService(Context.NOTIFICATION_SERVICE);

        final Notification notify = new Notification(R.drawable.reminder_icon,
                "", System.currentTimeMillis());

        notify.icon = R.drawable.reminder_icon;
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