package com.ifreebudget.fm.scheduler.task;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

import com.ifreebudget.fm.entity.beans.ConstraintEntity;
import com.ifreebudget.fm.entity.beans.ScheduleEntity;
import com.ifreebudget.fm.scheduler.task.Schedule.RepeatType;
import com.ifreebudget.fm.scheduler.task.constraints.Constraint;
import com.ifreebudget.fm.scheduler.task.constraints.MonthConstraint;
import com.ifreebudget.fm.scheduler.task.constraints.MonthConstraintDayBased;

public class TaskUtils {

    public static Schedule rebuildSchedule(Date next, Date end,
            ScheduleEntity scheduleEntity, ConstraintEntity constr)
            throws Exception {
        Schedule.RepeatType type = getTypeFromInt(scheduleEntity
                .getRepeatType());

        if (type == RepeatType.WEEK) {
            if (constr != null) {
                Schedule sch = new WeekSchedule(next, end);
                byte[] bb = constr.getConstraint();
                Constraint s = (Constraint) SerializationHelper.deserialize(bb);
                sch.setRepeatType(type, scheduleEntity.getStep());
                sch.setConstraint(s);
                return sch;
            }
        }
        else if (type == RepeatType.MONTH) {
            if (constr != null) {
                Schedule sch = null;
                byte[] bb = constr.getConstraint();
                Constraint s = (Constraint) SerializationHelper.deserialize(bb);
                if (s instanceof MonthConstraint) {
                    sch = new MonthSchedule(next, end);
                }
                else if (s instanceof MonthConstraintDayBased) {
                    sch = new MonthScheduleDayBased(next, end);
                }
                sch.setRepeatType(type, scheduleEntity.getStep());
                sch.setConstraint(s);
                return sch;
            }
        }
        else {
            Schedule sch = new BasicSchedule(next, end);
            sch.setRepeatType(type, scheduleEntity.getStep());
            return sch;
        }
        return null;
    }

    private static ConstraintEntity getConstraint(ScheduleEntity ts) {
        return null;
    }

    private static Schedule.RepeatType getTypeFromInt(int type) {
        switch (type) {
        case 1:
            return Schedule.RepeatType.SECOND;
        case 2:
            return Schedule.RepeatType.MINUTE;
        case 3:
            return Schedule.RepeatType.HOUR;
        case 4:
            return Schedule.RepeatType.DATE;
        case 5:
            return Schedule.RepeatType.WEEK;
        case 6:
            return Schedule.RepeatType.MONTH;
        case 7:
            return Schedule.RepeatType.YEAR;
        case 8:
            return Schedule.RepeatType.DAYOFWEEK;
        case 9:
            return Schedule.RepeatType.DAYOFMONTH;
        }
        return Schedule.RepeatType.NONE;
    }

    public static class SerializationHelper {
        public static byte[] serialize(Object obj) throws IOException {
            byte[] bytes = null;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            oos.close();
            bos.close();
            bytes = bos.toByteArray();
            return bytes;
        }

        public static Object deserialize(byte[] bb) throws Exception {
            ByteArrayInputStream bis = new ByteArrayInputStream(bb);
            ObjectInputStream ois = new ObjectInputStream(bis);
            Object o = ois.readObject();
            return o;
        }
    }

//    public static void createNotificationEntity(String TAG, Context context,
//            Long taskId, Long timeStamp) {
//        try {
//            TaskNotification tn = new TaskNotification();
//            tn.setTaskId(taskId);
//            tn.setTimestamp(timeStamp);
//
//            FManEntityManager em = FManEntityManager.getInstance();
//            em.createEntity(tn);
//
//            Log.i(TAG, "Created notification entity id: " + tn.getId());
//        }
//        catch (Exception e) {
//            Log.e(TAG, MiscUtils.stackTrace2String(e));
//        }
//    }
}
