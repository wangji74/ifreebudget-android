package com.ifreebudget.rmapp.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.ifreebudget.fm.entity.DBException;
import com.ifreebudget.fm.entity.Field;
import com.ifreebudget.fm.entity.beans.ConstraintEntity;
import com.ifreebudget.fm.entity.beans.ConstraintEntityMapper;
import com.ifreebudget.fm.entity.beans.FManEntity;
import com.ifreebudget.fm.entity.beans.ScheduleEntity;
import com.ifreebudget.fm.entity.beans.ScheduleEntityMapper;
import com.ifreebudget.fm.entity.beans.TableMapper;
import com.ifreebudget.fm.entity.beans.TaskEntity;
import com.ifreebudget.fm.entity.beans.TaskEntityMapper;
import com.ifreebudget.fm.entity.beans.TaskNotification;
import com.ifreebudget.fm.entity.beans.TaskNotificationMapper;
import com.ifreebudget.fm.utils.MiscUtils;

public class RMAppEntityManager {

    private static final String TAG = "RMApp.RMAppEntityManager";

    public static final String DATABASE_NAME = "com.ifreebudget.rmapp";
    private static final int DATABASE_VERSION = 1;

    private static RMAppEntityManager em = null;

    private DBOpenHelper openHelper = null;

    private SQLiteDatabase database = null;

    private Map<Class<?>, TableMapper> mappers = new HashMap<Class<?>, TableMapper>();
 
    
    public static RMAppEntityManager getInstance(Context context) {
        if (em == null) {
            em = new RMAppEntityManager();
            em.initialize(context);
            Log.i(TAG, "DBHelper created.");
        }
        return em;
    }

    public static void closeInstance() {
        em.openHelper.close();
        /* Null out this instance so it can be re-initialized properly. */
        em = null;
    }

    public static RMAppEntityManager getInstance() {
        return em;
    }

    private RMAppEntityManager() {
    }

    private void initialize(Context context) {
        initializeMappers();

        openHelper = new DBOpenHelper(context);
        SQLiteDatabase db = openHelper.getWritableDatabase();
        Boolean isOpen = db.isOpen();
        Log.i(TAG, "Database is open: " + isOpen + ", ver: " + db.getVersion());
    }

    private void initializeMappers() {
        mappers.put(TaskEntity.class, new TaskEntityMapper());
        mappers.put(ScheduleEntity.class, new ScheduleEntityMapper());
        mappers.put(ConstraintEntity.class, new ConstraintEntityMapper());
        mappers.put(TaskNotification.class, new TaskNotificationMapper());
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
    
    public int deleteEntity(FManEntity entity) throws DBException {
        return entity.getTableMapper().doDelete(database, entity);
    }

    public Object createEntity(FManEntity entity) throws DBException {
        return entity.getTableMapper().doInsert(database, entity);
    }

    public void updateEntity(FManEntity entity) throws DBException {
        try {
            entity.getTableMapper().doUpdate(database, entity);
        }
        catch (SQLException e) {
            throw new DBException(e);
        }
    }
    
    private void closeCursor(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }
    
    public List<FManEntity> getList(Class<?> type, String filter, int offset,
            int limit) throws DBException {
        try {
            TableMapper mapper = mappers.get(type);
            if (mapper != null) {
                return mapper.getList(database, filter, offset, limit);
            }
            return null;
        }
        catch (SQLException e) {
            throw new DBException(e);
        }
    }

    public List<FManEntity> getList(Class<?> type, String filter)
            throws DBException {
        return getList(type, filter, 0, 0);
    }

    public List<FManEntity> getList(Class<?> type) throws DBException {
        return getList(type, null);
    }
    

    public TaskNotification getTaskNotification(Long id) throws DBException {
        try {
            TaskNotificationMapper mapper = (TaskNotificationMapper) mappers
                    .get(TaskNotification.class);
            List<Field> pkList = mapper.getPrimaryKeys();
            Field pk = pkList.get(0);
            String filter = String.format(" WHERE %s = %d", pk.getDbName(), id);
            List<FManEntity> list = mapper.getList(database, filter, 0, 0);
            if (list != null) {
                if (list.size() != 1) {
                    throw new DBException("Non unique taskId:" + id);
                }
                return (TaskNotification) list.get(0);
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
                if (list.size() == 0) {
                    return null;
                }
                else if (list.size() > 1) {
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
    
    /* End API methods*/
    
    /* SQLLite db helper */
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
                db.execSQL(new TaskEntityMapper().getCreateSql());
                Log.i(TAG, "Created ScheduledTask table...Success");
                db.execSQL(new ScheduleEntityMapper().getCreateSql());
                Log.i(TAG, "Created Schedule table...Success");
                db.execSQL(new ConstraintEntityMapper().getCreateSql());
                Log.i(TAG, "Created Constraint table...Success");
                db.execSQL(new TaskNotificationMapper().getCreateSql());
                Log.i(TAG, "Created TaskNotification table...Success");
            }
            catch (Exception e) {
                Log.e(TAG, MiscUtils.stackTrace2String(e));
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            this.db = db;
            em.database = db;
        }
    }    
}
