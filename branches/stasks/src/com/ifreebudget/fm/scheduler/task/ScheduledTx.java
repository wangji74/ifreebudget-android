package com.ifreebudget.fm.scheduler.task;

import android.util.Log;

import com.ifreebudget.fm.utils.MiscUtils;

public class ScheduledTx extends BasicTask {
    private static String TAG = "ScheduledTx";
    
    private long txId = -1;

    public ScheduledTx(String name, long txId) {
        super(name);
        this.txId = txId;
    }

    public long getTxId() {
        return txId;
    }

    @Override
    public void executeTask() {
        try {
            Log.e(TAG, "Scheduled transaction id = " + txId);
            // createTx();
            //submitNotification();

        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
        finally {
            done = true;
            cancelled = true;
            runCount++;
            //updateTask();
        }
    }

//    private void createTx() throws Exception {
//        FManEntityManager em = new FManEntityManager();
//        long uid = SessionManager.getSessionUserId();
//        boolean isUpdate = false;
//        Date today = new Date();
//
//        ArrayList<Transaction> txList = new ArrayList<Transaction>();
//        User user = em.getUser(uid);
//        Transaction curr = em.getTransaction(user, txId);
//        if (curr != null) {
//            curr.setFitid(null);
//            curr.setTxDate(today);
//            curr.setCreateDate(today);
//            curr.setActivityBy("Scheduled transaction");
//            txList.add(curr);
//
//            ArrayList<Transaction> tmp = (ArrayList<Transaction>) em
//                    .getChildTransactions(user, curr.getTxId());
//            if (tmp != null && tmp.size() > 0) {
//                for (Transaction t : tmp) {
//                    t.setFitid(null);
//                    t.setTxDate(today);
//                    t.setCreateDate(today);
//                    t.setActivityBy("Scheduled transaction");
//                    txList.add(t);
//                }
//            }
//        }
//
//        ActionRequest req = new ActionRequest();
//        req.setActionName("addNestedTransaction");
//        req.setUser(user);
//        req.setProperty("TXLIST", txList);
//        req.setProperty("UPDATETX", isUpdate);
//
//        resp = new AddNestedTransactionsAction().executeAction(req);
//        if (resp.getErrorCode() == ActionResponse.NOERROR) {
//            logger.info("Added tx");
//            resp.addResult("TXLIST", txList);
//        }
//        else {
//            logger.error("Unable to add scheduled tx");
//        }
//    }
}
