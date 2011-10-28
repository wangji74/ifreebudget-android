package com.ifreebudget.fm.scheduler.task;

import java.util.List;

import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.TaskEntity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SystemEventReceiver extends BroadcastReceiver {

    private static final String TAG = "SystemEventReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG, "Event received: " + action);
    }
}
