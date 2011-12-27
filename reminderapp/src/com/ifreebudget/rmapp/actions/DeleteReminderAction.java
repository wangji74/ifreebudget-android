package com.ifreebudget.rmapp.actions;

import android.util.Log;

import com.ifreebudget.fm.actions.ActionRequest;
import com.ifreebudget.fm.actions.ActionResponse;
import com.ifreebudget.fm.entity.beans.ConstraintEntity;
import com.ifreebudget.fm.entity.beans.ScheduleEntity;
import com.ifreebudget.fm.entity.beans.TaskEntity;
import com.ifreebudget.fm.scheduler.task.Task;
import com.ifreebudget.fm.scheduler.task.constraints.Constraint;
import com.ifreebudget.fm.utils.MiscUtils;
import com.ifreebudget.rmapp.entity.RMAppEntityManager;

public class DeleteReminderAction {
    private String TAG = "RMApp.AddReminderAction";

    public ActionResponse execute(ActionRequest req) {
        ActionResponse resp = new ActionResponse();

        Long taskId = (Long) req.getProperty("TASKID");

        RMAppEntityManager em = RMAppEntityManager.getInstance();
        em.beginTransaction();

        try {
            // 0. Retrieve all data from db
            TaskEntity te = em.getTask(taskId);
            ScheduleEntity se = em.getScheduleByTaskId(te.getId());
            ConstraintEntity c = em.getConstraintByScheduleId(se.getId());

            int numDelete = 0;
            // 1. Delete constraints
            if (c != null) {
                numDelete = em.deleteEntity(c);
            }

            // 2. Delete schedules
            numDelete = em.deleteEntity(se);
            if (numDelete != 1) {
                Log.e(TAG,
                        "Problem deleting schedule entity, num rows deleted: "
                                + numDelete);
            }
            // 3. Delete task
            numDelete = em.deleteEntity(te);
            if (numDelete != 1) {
                Log.e(TAG, "Problem deleting task entity, num rows deleted: "
                        + numDelete);
            }

            // 4. Reschedule all tasks
            em.setTransactionSuccessful();
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
}
