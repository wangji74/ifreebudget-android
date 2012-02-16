package com.ifreebudget.rmapp.activities.utils;

import java.util.Date;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.ifreebudget.fm.entity.DBException;
import com.ifreebudget.fm.entity.beans.FManEntity;
import com.ifreebudget.fm.entity.beans.ScheduleEntity;
import com.ifreebudget.fm.entity.beans.TaskEntity;
import com.ifreebudget.fm.services.SessionManager;
import com.ifreebudget.fm.utils.MiscUtils;
import com.ifreebudget.rmapp.R;
import com.ifreebudget.rmapp.entity.RMAppEntityManager;

public class ReminderListEntry {
    private TaskEntity entity;
    private ScheduleEntity schedule;
    private Date nextTime;
    private boolean recurring;
    private Context context;
    private String recurranceString;
    private String description;

    boolean valid = true;

    private static final String TAG = "RMApp.ReminderListEntry";

    public static final long NUM_SECONDS_IN_DAY = 24 * 60 * 60;

    public ReminderListEntry(Context context, TaskEntity entity) {
        this.entity = entity;
        this.context = context;
        RMAppEntityManager em = RMAppEntityManager.getInstance();
        try {
            List<FManEntity> list = em.getList(ScheduleEntity.class,
                    " WHERE SCHTASKID = " + entity.getId());
            if (list == null) {
                valid = false;
                return;
            }
            if (list.size() != 1) {
                Log.e(TAG, "Invalid schedule for task: " + entity.getName()
                        + "[" + entity.getId() + "], found : " + list.size()
                        + " schedules");
                valid = false;
                return;
            }
            schedule = (ScheduleEntity) list.get(0);
            nextTime = new Date(schedule.getNextRunTime());
            setRecurring();
            setRecurranceAndDescription();
        }
        catch (DBException e) {
            valid = false;
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }

    }

    public TaskEntity getEntity() {
        return entity;
    }

    public Date getNextTime() {
        return nextTime;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    private void setRecurring() {
        recurring = false;
        if (schedule == null) {
            return;
        }
        int type = schedule.getRepeatType();
        switch (type) {
        case 0:
            recurring = false;
            break;
        case 3:
            if (entity.getStartTime().equals(entity.getEndTime())) {
                Log.i(TAG, "Once... i am here..." + entity.getName());
                recurring = false;
            }
            else {
                recurring = true;
            }
            break;
        default:
            recurring = true;
            break;
        }
    }

    private void setRecurranceAndDescription() {
        if (isRecurring()) {
            Date end = new Date(entity.getEndTime());
            String disp = SessionManager.getDateFormat().format(end);
            
            StringBuilder descr = new StringBuilder("Ends on ");
            descr.append(disp);
            
            this.description = descr.toString();
            
            int type = schedule.getRepeatType();
            switch (type) {
            case 3:
                recurranceString = "hourly";
                break;
            case 4:
                recurranceString = "daily";
                break;
            case 5:
                recurranceString = "weekly";
                break;
            case 6:
            case 7:
                recurranceString = "monthly";
                break;
            default:
                recurranceString = "";
                break;
            }
            //description += " ( " + recurranceString + " )";
        }
        else {
            Resources r = context.getResources();
            recurranceString = r.getString(R.string.does_not_repeat);
            description = recurranceString;
        }
    }

    public boolean isRecurring() {
        return recurring;
    }

    public String getDescription() {
        return description;
    }
    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder("<b>");
        ret.append(entity.getName());
        ret.append("</b>");

        Date now = new Date();

        Date endDate = new Date(entity.getEndTime());

        if (nextTime.after(endDate)) {
            ret.append("<br><i>Task complete</i>");
        }
        else if (now.after(new Date(entity.getEndTime()))) {
            ret.append("<br><i>Task complete</i>");
        }
        else {
            ret.append("<br>");
            // String disp =
            // SessionManager.getDateTimeFormat().format(nextTime);
            String disp = SessionManager.getTimeFormat().format(nextTime);
            ret.append(disp);
            ret.append("<br>");
            ret.append("<i>due in ");
            ret.append(getDateDiff(nextTime));
            ret.append("</i>");
        }
        return ret.toString();
    }

    public String getDisplayTime() {
        String disp = SessionManager.getTimeFormat().format(nextTime);
        return disp;
    }

    private String getDateDiff(Date ref) {
        Date now = new Date();

        long diff = (ref.getTime() - now.getTime()) / 1000;

        return calcDiff(diff);
    }

    private String calcDiff(long timeInSeconds) {
        long days = timeInSeconds / NUM_SECONDS_IN_DAY;
        String ret = (days > 0 ? pluralize(days, "day") : "");
        ret += calcHMS(timeInSeconds % NUM_SECONDS_IN_DAY);

        return ret;
    }

    private String calcHMS(long timeInSeconds) {
        long hours, minutes, seconds;
        hours = timeInSeconds / 3600;
        timeInSeconds = timeInSeconds - (hours * 3600);
        minutes = timeInSeconds / 60;
        timeInSeconds = timeInSeconds - (minutes * 60);
        seconds = timeInSeconds;

        StringBuilder ret = new StringBuilder();
        if (hours > 0) {
            ret.append(" ");
            ret.append(pluralize(hours, "hour"));
        }
        if (minutes > 0) {
            if (hours > 0) {
                ret.append(", ");
            }
            ret.append(pluralize(minutes, "minute"));
        }
        if (hours == 0 && minutes == 0 && seconds > 0) {
            ret.append(pluralize(seconds, "second"));
        }
        return ret.toString();
    }

    private String pluralize(long qty, String txt) {
        StringBuilder ret = new StringBuilder();
        ret.append(qty);
        ret.append(" ");
        ret.append(txt);
        if (qty != 1) {
            ret.append("s");
        }
        ret.append(" ");
        return ret.toString();
    }
}