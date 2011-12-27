package com.ifreebudget.rmapp.actions;

import android.util.Log;

import com.ifreebudget.fm.actions.ActionRequest;
import com.ifreebudget.fm.actions.ActionResponse;
import com.ifreebudget.fm.entity.beans.ConstraintEntity;
import com.ifreebudget.fm.entity.beans.ScheduleEntity;
import com.ifreebudget.fm.entity.beans.TaskEntity;
import com.ifreebudget.fm.scheduler.task.BasicTask;
import com.ifreebudget.fm.scheduler.task.Schedule;
import com.ifreebudget.fm.scheduler.task.Task;
import com.ifreebudget.fm.scheduler.task.TaskUtils.SerializationHelper;
import com.ifreebudget.fm.scheduler.task.constraints.Constraint;
import com.ifreebudget.fm.utils.MiscUtils;
import com.ifreebudget.rmapp.entity.RMAppEntityManager;

public class AddReminderAction {
    private String TAG = "RMApp.AddReminderAction";

    public ActionResponse execute(ActionRequest req) {
        ActionResponse resp = new ActionResponse();

        Task task = (Task) req.getProperty("TASK");
        String taskType = (String) req.getProperty("TASKTYPE");

        RMAppEntityManager em = RMAppEntityManager.getInstance();
        em.beginTransaction();
        try {
            TaskEntity te = getTaskEntity(task, taskType);
            em.createEntity(te);

            ScheduleEntity se = getScheduleEntity(te.getId(),
                    task.getSchedule());

            em.createEntity(se);

            Constraint c = task.getSchedule().getConstraint();
            if (c != null) {
                ConstraintEntity ce = getConstraintEntity(se.getId(), task
                        .getSchedule().getConstraint());
                em.createEntity(ce);
            }

            em.setTransactionSuccessful();
            resp.addResult("TASKID", te.getPK());
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
            resp.setErrorCode(ActionResponse.GENERAL_ERROR);
            resp.setErrorMessage(e.getMessage());
        }
        finally {
            em.endTransaction();
        }
        return resp;
    }

    private ConstraintEntity getConstraintEntity(Long scheduleId, Constraint c)
            throws Exception {
        ConstraintEntity ce = new ConstraintEntity();
        ce.setScheduleId(scheduleId);
        ce.setConstraint(SerializationHelper.serialize(c));
        return ce;
    }

    private ScheduleEntity getScheduleEntity(Long taskId, Schedule sch) {
        ScheduleEntity se = new ScheduleEntity();

        se.setScheduledTaskId(taskId);
        se.setRepeatType(sch.getRepeatType().getType());
        se.setStep(sch.getStep());
        se.setNextRunTime(sch.getNextRunTime().getTime());
        se.setLastRunTime(0L);

        return se;
    }

    private TaskEntity getTaskEntity(Task t, String taskType) {
        TaskEntity te = new TaskEntity();

        te.setName(t.getName());
        te.setStartTime(t.getSchedule().getStartTime().getTime());
        te.setEndTime(t.getSchedule().getEndTime().getTime());
        te.setTaskType(taskType);
        return te;
    }
}
