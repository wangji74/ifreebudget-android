package com.ifreebudget.fm.scheduler.task;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;

import com.ifreebudget.fm.R;
import com.ifreebudget.fm.activities.AddReminderActivity;
import com.ifreebudget.fm.activities.AddTransactionActivity;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.ConstraintEntity;
import com.ifreebudget.fm.entity.beans.ScheduleEntity;
import com.ifreebudget.fm.entity.beans.TaskEntity;
import com.ifreebudget.fm.utils.MiscUtils;

public class STaskAlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "STaskAlarmReceiver";

    private static final int NOTIFID = 100;

    @Override
    public void onReceive(Context context, Intent intent) {
        Long id = intent.getLongExtra(AddReminderActivity.TASK_ALARM_ID, -1);
        Log.d(TAG, "Alarm received: " + id);
        if(id == -1) {
            Log.e(TAG, "Invalid alarm received: " + id);
            return;
        }
        try {
            FManEntityManager em = FManEntityManager.getInstance();
            TaskEntity task = em.getTask(id);
            ScheduleEntity schedule = em.getScheduleByTaskId(task.getId());
            ConstraintEntity constr = em.getConstraintByScheduleId(schedule.getId());
            String tickerText = "Scheduled transaction reminder - "
                    + task.getName();
            sendNotification(context, AddTransactionActivity.class, tickerText,
                    "Scheduled transaction", tickerText, 1, true, false);
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
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
            notify.vibrate = new long[] { 100, 200, 200, 200, 200, 200, 1000,
                    200, 200, 200, 1000, 200 };
        }

        Intent toLaunch = new Intent(caller, activityToLaunch);
        PendingIntent intentBack = PendingIntent.getActivity(caller, 0,
                toLaunch, 0);

        notify.setLatestEventInfo(caller, title, msg, intentBack);
        notifier.notify(NOTIFID, notify);
    }

}