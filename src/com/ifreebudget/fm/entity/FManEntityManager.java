/*******************************************************************************
 * Copyright 2011 ifreebudget@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.ifreebudget.fm.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.ifreebudget.fm.actions.ActionRequest;
import com.ifreebudget.fm.actions.CreateInitialAccounts;
import com.ifreebudget.fm.entity.beans.Account;
import com.ifreebudget.fm.entity.beans.AccountCategory;
import com.ifreebudget.fm.entity.beans.AccountCategoryMapper;
import com.ifreebudget.fm.entity.beans.AccountMapper;
import com.ifreebudget.fm.entity.beans.Budget;
import com.ifreebudget.fm.entity.beans.BudgetMapper;
import com.ifreebudget.fm.entity.beans.BudgetedAccount;
import com.ifreebudget.fm.entity.beans.BudgetedAccountMapper;
import com.ifreebudget.fm.entity.beans.CategoryIconMap;
import com.ifreebudget.fm.entity.beans.CategoryIconMapMapper;
import com.ifreebudget.fm.entity.beans.ConstraintEntity;
import com.ifreebudget.fm.entity.beans.ConstraintEntityMapper;
import com.ifreebudget.fm.entity.beans.FManEntity;
import com.ifreebudget.fm.entity.beans.ScheduleEntity;
import com.ifreebudget.fm.entity.beans.ScheduleEntityMapper;
import com.ifreebudget.fm.entity.beans.TableMapper;
import com.ifreebudget.fm.entity.beans.TaskEntity;
import com.ifreebudget.fm.entity.beans.TaskEntityMapper;
import com.ifreebudget.fm.entity.beans.TaskNotificationMapper;
import com.ifreebudget.fm.entity.beans.Transaction;
import com.ifreebudget.fm.entity.beans.TransactionMapper;
import com.ifreebudget.fm.entity.beans.TxHistory;
import com.ifreebudget.fm.entity.beans.TxHistoryMapper;
import com.ifreebudget.fm.utils.MiscUtils;

public class FManEntityManager {
    private static final String TAG = "DBHelper";

    public static final String DATABASE_NAME = "com.ifreebudget.db";
    private static final int DATABASE_VERSION = 5;

    private static FManEntityManager em = null;

    private DBOpenHelper openHelper = null;

    private SQLiteDatabase database = null;

    private Map<Class<?>, TableMapper> mappers = new HashMap<Class<?>, TableMapper>();

    public static FManEntityManager getInstance(Context context) {
        if (em == null) {
            em = new FManEntityManager();
            em.initialize(context);
        }
        return em;
    }

    public static void closeInstance() {
        em.openHelper.close();
        /* Null out this instance so it can be re-initialized properly. */
        em = null;
    }

    public static FManEntityManager getInstance() {
        return em;
    }

    private FManEntityManager() {
    }

    private void initialize(Context context) {
        initializeMappers();

        openHelper = new DBOpenHelper(context);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        Boolean isOpen = db.isOpen();
        Log.i(TAG, "Database is open: " + isOpen + ", ver: " + db.getVersion());
    }

    private void initializeMappers() {
        mappers.put(Account.class, new AccountMapper());
        mappers.put(AccountCategory.class, new AccountCategoryMapper());
        mappers.put(Budget.class, new BudgetMapper());
        mappers.put(BudgetedAccount.class, new BudgetedAccountMapper());
        mappers.put(CategoryIconMap.class, new CategoryIconMapMapper());
        mappers.put(Transaction.class, new TransactionMapper());
        mappers.put(TxHistory.class, new TxHistoryMapper());
        mappers.put(TaskEntity.class, new TaskEntityMapper());
        mappers.put(ScheduleEntity.class, new ScheduleEntityMapper());
        mappers.put(ConstraintEntity.class, new ConstraintEntityMapper());
        mappers.put(TaskNotificationMapper.class, new TaskNotificationMapper());
    }

    /* API methods */
    public TableMapper getMapper(Class<?> type) {
        return mappers.get(type);
    }

    public void beginTransaction() {
        database.beginTransaction();
    }

    public void setTransactionSuccessful() {
        database.setTransactionSuccessful();
    }

    public void endTransaction() {
        if (database.inTransaction())
            database.endTransaction();
    }

    public SQLiteStatement prepareStatement(String query) throws Exception {
        SQLiteStatement stmt = database.compileStatement(query);

        return stmt;
    }

    public void updateEntity(FManEntity entity) throws DBException {
        try {
            entity.getTableMapper().doUpdate(database, entity);
        }
        catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public boolean accountExists(int accountType, String accountName)
            throws DBException {
        List<FManEntity> list = getAccount(accountType, accountName);
        return (list != null && list.size() != 0);
    }

    public void addAccount(Account a) throws DBException {
        try {
            TableMapper mapper = a.getTableMapper();
            mapper.doInsert(database, a);
        }
        catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public Account getAccount(Long accountId) throws DBException {
        try {
            String filter = " WHERE ACCOUNTID = " + accountId;
            TableMapper mapper = new AccountMapper();
            List<FManEntity> list = mapper.getList(database, filter, 0, 0);
            if (list != null) {
                if (list.size() != 1) {
                    throw new DBException("Non unique accountId:" + accountId);
                }
                return (Account) list.get(0);
            }
            return null;
        }
        catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public AccountCategory getAccountCategory(Long categoryId)
            throws DBException {
        try {
            String filter = " WHERE CATEGORYID = " + categoryId;
            TableMapper mapper = new AccountCategoryMapper();
            List<FManEntity> list = mapper.getList(database, filter, 0, 0);
            if (list != null) {
                if (list.size() != 1) {
                    throw new DBException("Non unique accountId:" + categoryId);
                }
                return (AccountCategory) list.get(0);
            }
            return null;
        }
        catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public List<FManEntity> getAccount(int accountType, String accountName)
            throws DBException {
        try {
            String filter = " WHERE ACCOUNTTYPE = " + accountType
                    + " AND ACCOUNTNAME = '" + accountName + "'";
            TableMapper mapper = new AccountMapper();
            return mapper.getList(database, filter, 0, 0);
        }
        catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public void addAccountCategory(AccountCategory category) throws DBException {
        try {
            new AccountCategoryMapper().doInsert(database, category);
        }
        catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public List<FManEntity> getAccounts(Long categoryId) throws DBException {
        try {
            String filter = " WHERE CATEGORYID = " + categoryId
                    + " ORDER BY ACCOUNTNAME";
            TableMapper mapper = new AccountMapper();
            return mapper.getList(database, filter, 0, 0);
        }
        catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public List<FManEntity> getAllAccounts() throws DBException {
        try {
            TableMapper mapper = new AccountMapper();
            String[] col = { "ACCOUNTNAME" };
            String[] dir = { "ASC" };

            return mapper.getList(database, null, col, dir, 0, 0);
        }
        catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public List<FManEntity> getAccountCategories() throws DBException {
        try {
            TableMapper mapper = new AccountCategoryMapper();
            return mapper.getList(database, null, 0, 0);
        }
        catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public List<FManEntity> getAccountCategories(Long parentCategoryId)
            throws DBException {
        try {
            String filter = " WHERE PARENTCATEGORYID = " + parentCategoryId
                    + " ORDER BY CATEGORYNAME";
            TableMapper mapper = new AccountCategoryMapper();
            return mapper.getList(database, filter, 0, 0);
        }
        catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public List<FManEntity> getChildren(Long parentId) throws DBException {
        List<FManEntity> categories = getAccountCategories(parentId);
        List<FManEntity> accounts = getAccounts(parentId);

        categories.addAll(accounts);

        return categories;
    }

    // public AccountCategory getParentCategory(Long categoryId)
    // throws DBException {
    // try {
    // String filter = " WHERE CATEGORYID = " + categoryId;
    // TableMapper mapper = new AccountCategoryMapper();
    // List<FManEntity> list = mapper.getList(database, filter, 0, 0);
    // if (list != null && list.size() > 0) {
    // return (AccountCategory) list.get(0);
    // }
    // return null;
    // }
    // catch (SQLException e) {
    // throw new DBException(e);
    // }
    // }

    public boolean fitIdExists(Long fromAccountId, Long toAccountId,
            String fitId) {
        return false;
    }

    public Transaction getTransaction(Long txId) throws DBException {
        try {
            String filter = " WHERE TXID = " + txId;
            TableMapper mapper = new TransactionMapper();
            List<FManEntity> list = mapper.getList(database, filter, 0, 0);
            if (list != null) {
                if (list.size() != 1) {
                    throw new DBException("Non unique accountId:" + txId);
                }
                return (Transaction) list.get(0);
            }
            return null;
        }
        catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public List<FManEntity> getTransactions(int offset, int limit)
            throws DBException {
        TableMapper mapper = new TransactionMapper();
        String filter = " ORDER BY TXDATE DESC, CREATEDATE DESC";
        return mapper.getList(database, filter, offset, limit);
    }

    public int getTransactionsCount(Long accountId) throws DBException {
        Cursor cursor = null;
        try {
            String query = "SELECT COUNT(*) FROM FMTRANSACTION T WHERE (T.FROMACCOUNTID=? OR T.TOACCOUNTID=?)";

            String arg = String.valueOf(accountId);
            String[] args = { arg, arg };
            cursor = database.rawQuery(query, args);

            cursor.moveToFirst();
            return cursor.getInt(0);
        }
        catch (SQLException e) {
            throw new DBException(e);
        }
        finally {
            closeCursor(cursor);
        }
    }

    public boolean isCategoryPopulated(Long categoryId) throws DBException {
        try {
            int num = getNumAccountsForCategory(categoryId);
            if (num != 0) {
                return true;
            }
            num = getNumChildrenForCategory(categoryId);
            return num != 0;
        }
        catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public int getNumAccountsForCategory(Long categoryId) throws DBException {
        Cursor cursor = null;
        try {
            String query = "SELECT COUNT(T.ACCOUNTID) FROM ACCOUNT T WHERE T.CATEGORYID=?";

            String arg = String.valueOf(categoryId);
            String[] args = { arg };
            cursor = database.rawQuery(query, args);

            cursor.moveToFirst();
            int count = cursor.getInt(0);

            return count;
        }
        catch (SQLException e) {
            throw new DBException(e);
        }
        finally {
            closeCursor(cursor);
        }
    }

    public int getNumChildrenForCategory(Long categoryId) throws DBException {
        Cursor cursor = null;
        try {
            String query = "SELECT COUNT(*) FROM ACCOUNTCATEGORY T WHERE T.PARENTCATEGORYID=?";

            String arg = String.valueOf(categoryId);
            String[] args = { arg };
            cursor = database.rawQuery(query, args);

            cursor.moveToFirst();
            int count = cursor.getInt(0);

            return count;
        }
        catch (SQLException e) {
            throw new DBException(e);
        }
        finally {
            closeCursor(cursor);
        }
    }

    public CategoryIconMap getCategoryIconMap(Long categoryId)
            throws DBException {
        try {
            String filter = " WHERE CATEGORYID = " + categoryId;
            TableMapper mapper = new CategoryIconMapMapper();
            List<FManEntity> list = mapper.getList(database, filter, 0, 0);
            if (list != null) {
                int sz = list.size();
                if (sz == 1) {
                    return (CategoryIconMap) list.get(0);
                }
                else if (list.size() >= 1) {
                    throw new DBException("Non unique accountId:" + categoryId);
                }
                return null;
            }
            return null;
        }
        catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public int deleteEntity(FManEntity entity) throws DBException {
        return entity.getTableMapper().doDelete(database, entity);
    }

    public Object createEntity(FManEntity entity) throws DBException {
        return entity.getTableMapper().doInsert(database, entity);
    }

    private void closeCursor(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }

    public List<FManEntity> executeFilterQuery(String query, Class<?> type)
            throws DBException {
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(query, null);

            TableMapper mapper = mappers.get(type);

            int sz = cursor.getCount();
            ArrayList<FManEntity> ret = new ArrayList<FManEntity>(sz);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                FManEntity a = mapper.getCurrentEntityFromCursor(cursor);
                ret.add(a);
                cursor.moveToNext();
            }
            return ret;
        }
        catch (Exception e) {
            throw new DBException(e);
        }
        finally {
            closeCursor(cursor);
        }
    }

    public List<FManEntity> getList(Class<?> type, String filter)
            throws DBException {
        try {
            TableMapper mapper = mappers.get(type);
            if (mapper != null) {
                return mapper.getList(database, filter, 0, 0);
            }
            return null;
        }
        catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public List<FManEntity> getList(Class<?> type) throws DBException {
        return getList(type, null);
    }

    public Budget getBudget(Long id) throws DBException {
        try {
            String filter = " WHERE ID = " + id;
            TableMapper mapper = mappers.get(Budget.class);
            List<FManEntity> list = mapper.getList(database, filter, 0, 0);
            if (list != null) {
                if (list.size() != 1) {
                    throw new DBException("Non unique accountId:" + id);
                }
                return (Budget) list.get(0);
            }
            return null;
        }
        catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public TaskEntity getTask(Long id) throws DBException {
        try {
            TaskEntityMapper mapper = (TaskEntityMapper) mappers
                    .get(TaskEntity.class);
            List<Field> pkList = mapper.getPrimaryKeys();
            Field pk = pkList.get(0);
            String filter = String.format(" WHERE %s = %d", pk.getDbName(), id);
            List<FManEntity> list = mapper.getList(database, filter, 0, 0);
            if (list != null) {
                if (list.size() != 1) {
                    throw new DBException("Non unique taskId:" + id);
                }
                return (TaskEntity) list.get(0);
            }
            return null;
        }
        catch (SQLException e) {
            throw new DBException(e);
        }

    }

    public ScheduleEntity getScheduleByTaskId(Long taskId) throws DBException {
        try {
            ScheduleEntityMapper mapper = (ScheduleEntityMapper) mappers
                    .get(ScheduleEntity.class);
            String filter = String
                    .format(" WHERE %s = %d", "SCHTASKID", taskId);
            List<FManEntity> list = mapper.getList(database, filter, 0, 0);
            if (list != null) {
                if (list.size() != 1) {
                    throw new DBException("Non unique scheduleId:" + taskId);
                }
                return (ScheduleEntity) list.get(0);
            }
            return null;
        }
        catch (SQLException e) {
            throw new DBException(e);
        }

    }

    public ConstraintEntity getConstraintByScheduleId(Long scheduleId)
            throws DBException {
        try {
            ConstraintEntityMapper mapper = (ConstraintEntityMapper) mappers
                    .get(ConstraintEntity.class);
            String filter = String.format(" WHERE %s = %d", "SCHEDID",
                    scheduleId);
            List<FManEntity> list = mapper.getList(database, filter, 0, 0);
            if (list != null) {
                if (list.size() == 1) {
                    return (ConstraintEntity) list.get(0);
                }
                if (list.size() > 1) {
                    throw new DBException("Non unique scheduleId:" + scheduleId);
                }
            }
            return null;
        }
        catch (SQLException e) {
            throw new DBException(e);
        }

    }

    public int deleteBudget(long budgetId) throws DBException {
        try {
            String filter = "BUDGETID = " + budgetId;
            int num = database.delete(mappers.get(BudgetedAccount.class)
                    .getTableName(), filter, null);

            String filter2 = "ID = " + budgetId;

            num = database.delete(mappers.get(Budget.class).getTableName(),
                    filter2, null);

            return num;
        }
        catch (Exception e) {
            throw new DBException(e);
        }
    }

    public int deleteAccountFromBudget(Long accountId) throws DBException {
        try {
            String filter = "ACCOUNTID = " + accountId;
            int num = database.delete(mappers.get(BudgetedAccount.class)
                    .getTableName(), filter, null);

            return num;
        }
        catch (Exception e) {
            throw new DBException(e);
        }
    }

    public List<FManEntity> getTxHistoryShortcutList() throws DBException {
        try {
            String[] order = { "CNT" };
            String[] dir = { "DESC" };

            TableMapper mapper = mappers.get(TxHistory.class);

            return mapper.getList(database, null, order, dir, -1, 3);
        }
        catch (Exception e) {
            throw new DBException(e);
        }
    }

    public void createTxHistory(Long from, Long to, String loc)
            throws DBException {
        try {
            TableMapper mapper = mappers.get(TxHistory.class);

            String filter = " WHERE FROMACCOUNTID = " + from
                    + " AND TOACCOUNTID=" + to;
            List<FManEntity> list = mapper.getList(database, filter, 0, 0);
            if (list == null || list.size() == 0) {
                TxHistory txh = new TxHistory();
                txh.setFromAccountId(from);
                txh.setToAccountId(to);
                txh.setLoc(loc);
                txh.setCount(1);
                txh.setLastUpdate(System.currentTimeMillis());
                em.createEntity(txh);
            }
            else {
                TxHistory txh = (TxHistory) list.get(0);
                int count = txh.getCount();
                txh.setCount(count + 1);
                txh.setLastUpdate(System.currentTimeMillis());
                em.updateEntity(txh);
            }

        }
        catch (Exception e) {
            throw new DBException(e);
        }
    }

    public List<Long> getBestmatchesForAccount(Long accountId)
            throws DBException {
        String[] order = { "CNT" };
        String[] dir = { "DESC" };

        String filter = " WHERE FROMACCOUNTID = " + accountId;

        TableMapper mapper = mappers.get(TxHistory.class);

        List<FManEntity> txhList = mapper.getList(database, filter, order, dir,
                -1, 3);

        List<Long> ret = new ArrayList<Long>(txhList.size());

        for (FManEntity e : txhList) {
            TxHistory txh = (TxHistory) e;
            ret.add(txh.getToAccountId());
        }

        return ret;
    }

    public List<FManEntity> getAccountsForTypes(int[] accountType)
            throws DBException {
        try {
            if (accountType == null) {
                return getAllAccounts();
            }
            StringBuilder inList = new StringBuilder();
            int len = accountType.length;
            for (int i = 0; i < len; i++) {
                inList.append(accountType[i]);
                if (i < len - 1) {
                    inList.append(",");
                }
            }

            String filter = " WHERE ACCOUNTTYPE IN (" + inList.toString()
                    + ") ORDER BY ACCOUNTNAME";

            TableMapper mapper = new AccountMapper();
            return mapper.getList(database, filter, 0, 0);
        }
        catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public List<FManEntity> getAccountsForTypesOrdered(int[] accountTypes)
            throws DBException {

        // String q =
        // "SELECT FROMACCOUNTID, MAX(CNT) FROM TXHISTORY GROUP BY FROMACCOUNTID ORDER BY MAX(CNT) DESC";

        String q = "SELECT ACCOUNTID FROM ACCOUNT LEFT OUTER JOIN "
                + "(SELECT FROMACCOUNTID, MAX(CNT) AS C FROM TXHISTORY GROUP BY FROMACCOUNTID ORDER BY MAX(CNT) DESC) T "
                + "ON ACCOUNT.ACCOUNTID = T.FROMACCOUNTID WHERE ACCOUNTTYPE IN ( <type> ) ORDER BY C DESC, ACCOUNT.ACCOUNTNAME";

        String inList = SQLUtils.buildInList(accountTypes);

        q = q.replaceAll("<type>", inList);

        List<FManEntity> ret = new ArrayList<FManEntity>();
        Map<Long, Object> tmp = null;
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(q, null);
            if (cursor != null) {
                int sz = cursor.getCount();
                tmp = new LinkedHashMap<Long, Object>(sz);
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    Long aId = cursor.getLong(0);
                    Account a = em.getAccount(aId);
                    ret.add(a);
                    tmp.put(aId, null);
                    cursor.moveToNext();
                }
            }
            return ret;
        }
        catch (Exception e) {
            throw new DBException(e);
        }
        finally {
            closeCursor(cursor);
        }

        // inList = SQLUtils.buildInList(accountTypes);
        // String filter = " WHERE ACCOUNTTYPE IN ( " + inList + " ) ";
        // String[] colName1 = { "ACCOUNTNAME" };
        // String[] dir1 = { "ASC" };
        // List<FManEntity> aList = mappers.get(Account.class).getList(database,
        // filter, colName1, dir1, -1, -1);
        //
        // return aList;
    }

    public void reInitializeDb() {
        Set<Class<?>> keys = mappers.keySet();
        for (Class<?> cls : keys) {
            TableMapper mapper = mappers.get(cls);
            reCreateTable(mapper);
        }
        try {
            ActionRequest req = new ActionRequest();
            req.setProperty("DATABASE", database);
            CreateInitialAccounts action = new CreateInitialAccounts();
            action.executeAction(req);
        }
        catch (Exception e) {
            Log.e(TAG, MiscUtils.stackTrace2String(e));
        }
    }

    private void reCreateTable(TableMapper mapper) {
        database.execSQL(mapper.getDropSql());
        Log.i(TAG, "Dropped table: " + mapper.getTableName());
        database.execSQL(mapper.getCreateSql());
        Log.i(TAG, "Created table: " + mapper.getTableName());
    }

    /* End API methods */

    private static class DBOpenHelper extends SQLiteOpenHelper {
        private final Context context;
        private SQLiteDatabase db;

        public DBOpenHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.context = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            this.db = db;

            em.database = db;
            try {
                db.execSQL(new AccountCategoryMapper().getCreateSql());
                Log.i(TAG, "Created AccountCategory table...Success!");
                db.execSQL(new AccountMapper().getCreateSql());
                Log.i(TAG, "Created Account table...Success!");
                db.execSQL(new TransactionMapper().getCreateSql());
                Log.i(TAG, "Created Transaction table...Success!");
                db.execSQL(new CategoryIconMapMapper().getCreateSql());
                Log.i(TAG, "Created CategoryIconMap table...Success!");
                db.execSQL(new BudgetMapper().getCreateSql());
                Log.i(TAG, "Created Budget table...Success!");
                db.execSQL(new BudgetedAccountMapper().getCreateSql());
                Log.i(TAG, "Created BudgetedAccount table...Success!");
                db.execSQL(new TxHistoryMapper().getCreateSql());
                Log.i(TAG, "Created TxHistory table...Success");
                db.execSQL(new TaskEntityMapper().getCreateSql());
                Log.i(TAG, "Created ScheduledTask table...Success");
                db.execSQL(new ScheduleEntityMapper().getCreateSql());
                Log.i(TAG, "Created Schedule table...Success");
                db.execSQL(new ConstraintEntityMapper().getCreateSql());
                Log.i(TAG, "Created Constraint table...Success");
                db.execSQL(new TaskNotificationMapper().getCreateSql());                               
                Log.i(TAG, "Created TaskNotification table...Success");
                
                Log.i(TAG, "Creating initial accounts");

                ActionRequest req = new ActionRequest();
                req.setProperty("DATABASE", db);
                CreateInitialAccounts action = new CreateInitialAccounts();
                action.executeAction(req);
            }
            catch (Exception e) {
                Log.e(TAG, MiscUtils.stackTrace2String(e));
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(new TaskEntityMapper().getCreateSql());
            Log.i(TAG, "Created ScheduledTask table...Success");
            db.execSQL(new ScheduleEntityMapper().getCreateSql());
            Log.i(TAG, "Created Schedule table...Success");
            db.execSQL(new ConstraintEntityMapper().getCreateSql());
            Log.i(TAG, "Created Constraint table...Success");
            db.execSQL(new TaskNotificationMapper().getCreateSql());
            Log.i(TAG, "Created TaskNotification table...Success");
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            this.db = db;
            em.database = db;
        }
    }
}
