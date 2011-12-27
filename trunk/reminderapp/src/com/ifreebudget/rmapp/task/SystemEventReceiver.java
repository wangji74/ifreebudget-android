package com.ifreebudget.rmapp.task;

import com.ifreebudget.rmapp.entity.RMAppEntityManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SystemEventReceiver extends BroadcastReceiver {

    private static final String TAG = "RMApp.SystemEventReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals("android.intent.action.BOOT_COMPLETED")) {
            RMAppEntityManager.getInstance(context);
        }
        Log.i(TAG, "Event received: " + action);
        context.startService(new Intent(context, TaskRestarterService.class));
    }
}
