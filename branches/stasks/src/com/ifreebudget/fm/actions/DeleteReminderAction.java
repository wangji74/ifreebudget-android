package com.ifreebudget.fm.actions;

import com.ifreebudget.fm.constants.AccountTypes;
import com.ifreebudget.fm.entity.FManEntityManager;
import com.ifreebudget.fm.entity.beans.Account;
import com.ifreebudget.fm.entity.beans.ConstraintEntity;
import com.ifreebudget.fm.entity.beans.ScheduleEntity;
import com.ifreebudget.fm.entity.beans.TaskEntity;
import com.ifreebudget.fm.entity.beans.Transaction;

public class DeleteReminderAction {

    public ActionResponse executeAction(ActionRequest req) throws Exception {
        long id = (Long) req.getProperty("ID");

        ActionResponse resp = new ActionResponse();

        /* Begin tx */
        FManEntityManager em = FManEntityManager.getInstance();
        em.beginTransaction();
        try {
            TaskEntity t = em.getTask(id);
            int delCount = deleteTransaction(em, t);
            if (delCount != 1) {
                resp.setErrorCode(ActionResponse.GENERAL_ERROR);
                resp.setErrorMessage("Unable to delete reminder for id " + id);
            }
            else {
                em.setTransactionSuccessful();
            }
        }
        catch (Exception e) {
            resp.setErrorCode(ActionResponse.TX_DELETE_ERROR);
            resp.setErrorMessage(e.getMessage());
            throw e;
        }
        finally {
            em.endTransaction();
        }
        return resp;
    }

    private int deleteTransaction(FManEntityManager em, TaskEntity t)
            throws Exception {
        try {
            ScheduleEntity scheduleEntity = em.getScheduleByTaskId(t.getId());
            ConstraintEntity constraintEntity = em
                    .getConstraintByScheduleId(scheduleEntity.getId());

            int rc = 0;
            if (constraintEntity != null) {
                rc = em.deleteEntity(constraintEntity);
                if (rc != 1) {
                    throw new Exception(
                            "Failed to delete constraint for transaction : "
                                    + t.getName());
                }
            }

            if (scheduleEntity != null) {
                rc = em.deleteEntity(scheduleEntity);
                if (rc != 1) {
                    throw new Exception(
                            "Failed to delete schedule for transaction : "
                                    + t.getName());
                }
            }
            rc = em.deleteEntity(t);

            return rc;
        }
        catch (Exception e) {
            throw e;
        }
    }
}
