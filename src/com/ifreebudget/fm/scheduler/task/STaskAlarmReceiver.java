package com.ifreebudget.fm.scheduler.task;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class STaskAlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "STaskAlarmReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String id = intent.getStringExtra("id");
        Log.d(TAG, "Alarm received: " + id);
    }

}
