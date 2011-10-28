package com.ifreebudget.fm.scheduler.task;

import com.ifreebudget.fm.entity.FManEntityManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SystemEventReceiver extends BroadcastReceiver {

    private static final String TAG = "SystemEventReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals("android.intent.action.BOOT_COMPLETED")) {
            FManEntityManager.getInstance(context);
        }
        Log.i(TAG, "Event received: " + action);
        context.startService(new Intent(context, TaskRestarterService.class));
    }
}
