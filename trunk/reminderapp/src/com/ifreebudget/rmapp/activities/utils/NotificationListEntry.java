package com.ifreebudget.rmapp.activities.utils;

import java.util.Date;

import android.util.Log;

import com.ifreebudget.fm.entity.DBException;
import com.ifreebudget.fm.entity.beans.TaskEntity;
import com.ifreebudget.fm.entity.beans.TaskNotification;
import com.ifreebudget.fm.services.SessionManager;
import com.ifreebudget.fm.utils.MiscUtils;
import com.ifreebudget.rmapp.entity.RMAppEntityManager;

public class NotificationListEntry {
    private TaskNotification notif;
    private TaskEntity taskEntity;
    private boolean valid = true;
    private String time = null;
    
    private static final String TAG = "RMApp.NotificationListEntry";

    public NotificationListEntry(TaskNotification notif) {
        this.notif = notif;
        RMAppEntityManager em = RMAppEntityManager.getInstance();
        try {
            taskEntity = em.getTask(notif.getTaskId());
            if (taskEntity == null) {
                Log.e(TAG,
                        "Orphan notification found...deleting "
                                + notif.getId());
                em.deleteEntity(notif);
                valid = false;
                return;
            }
            time = SessionManager.getDateTimeFormat().format(
                    new Date(notif.getTimestamp()));
        }
        catch (DBException e) {
            valid = false;
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }

    }
    
    public TaskNotification getNotif() {
        return notif;
    }


    public boolean isValid() {
        return valid;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("<b>");
        ret.append(taskEntity.getName());
        ret.append("</b><br>@ ");
        ret.append(time);
        return ret.toString();
    }
}
