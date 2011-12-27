package com.ifreebudget.rmapp.task;

import android.content.Context;
import android.util.Log;

import com.ifreebudget.fm.entity.beans.TaskNotification;
import com.ifreebudget.fm.utils.MiscUtils;
import com.ifreebudget.rmapp.entity.RMAppEntityManager;

public class RMAppTaskUtils {
    public static void createNotificationEntity(String TAG, Context context,
            Long taskId, Long timeStamp) {
        try {
            TaskNotification tn = new TaskNotification();
            tn.setTaskId(taskId);
            tn.setTimestamp(timeStamp);

            RMAppEntityManager em = RMAppEntityManager.getInstance();
            em.createEntity(tn);

//            Log.i(TAG, "Created notification entity id: " + tn.getId());
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
    }
}
