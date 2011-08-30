package com.ifreebudget.fm.entity.beans;

import java.math.BigDecimal;
import java.util.List;

import com.ifreebudget.fm.entity.Table;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class ScheduledTaskMapper extends AbstractTableMapper {

    private Table table;
    
    @Override
    public String getRetrieveSql() {
        return table.getRetrieveSql();
    }

    @Override
    public String getCreateSql() {
        return table.getCreateSql();
    }

    @Override
    public String getInsertSql() {
        return table.getInsertSql();
    }

    public String getUpdateSql() {
        throw new RuntimeException("Update not supported");
    }

    public List<FManEntity> getList(SQLiteDatabase database, String filter,
            int offset, int limit) {
        return getList(database, filter, null, null, offset, limit);
    }

    public List<FManEntity> getList(SQLiteDatabase database, String filter,
            String[] order, String[] direction, int offset, int limit) {

        StringBuilder q = new StringBuilder(getRetrieveSql());
        if (filter != null) {
            q.append(SPACE);
            q.append(filter);
        }

        String orderBy = buildOrderByClause(order, direction);

        q.append(orderBy);

        return executeListQuery(database, q.toString());
    }

    public Object doInsert(SQLiteDatabase database, FManEntity entity)
            throws SQLException {
        ScheduledTask task = (ScheduledTask) entity;

        SQLiteStatement stmt = database.compileStatement(getInsertSql());

        stmt.clearBindings();

        stmt.bindNull(1);
        safeBindString(stmt, 2, task.getName());
        safeBindLong(stmt, 3, task.getStartTime());
        safeBindLong(stmt, 4, task.getEndTime());
        safeBindLong(stmt, 5, task.getBusinessObjectId());
        safeBindString(stmt, 6, task.getTaskType());

        Long id = stmt.executeInsert();

        task.setId(id);

        return id;
    }

    public void doUpdate(SQLiteDatabase database, FManEntity entity)
            throws SQLException {
        throw new RuntimeException("Update task not supported.");
    }

    public String getTableName() {
        return table.getTableName();
    }

    public FManEntity getCurrentEntityFromCursor(Cursor cursor) {
        ScheduledTask a = new ScheduledTask();
        setField(cursor, a, "ID", "setId", Long.class);
        setField(cursor, a, "NAME", "setName", String.class);
        setField(cursor, a, "STARTTIME", "setStartTime",
                Long.class);
        setField(cursor, a, "ENDTIME", "setEndTime", Long.class);
        setField(cursor, a, "BUSOBJID", "setBusinessObjectId", Long.class);
        setField(cursor, a, "TASKTYPE", "setTaskType", String.class);

        return a;
    }
}
