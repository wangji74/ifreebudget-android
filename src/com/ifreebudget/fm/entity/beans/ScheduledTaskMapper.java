package com.ifreebudget.fm.entity.beans;

import java.util.List;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.ifreebudget.fm.entity.Field;
import com.ifreebudget.fm.entity.Mappings;
import com.ifreebudget.fm.entity.Table;
import com.ifreebudget.fm.utils.MiscUtils;

public class ScheduledTaskMapper extends AbstractTableMapper {

    private Table table;

    private final String TABLENAME = "STASK";
    
    public ScheduledTaskMapper() {
        String[] mappings = Mappings.ScheduledTaskMapperMappings;
        
        table = new Table(TABLENAME);
        
        for(String s : mappings) {
            String[] split = s.split(":");
            Class<?> cls = null;
            try {
                cls = Class.forName(split[Mappings.JTIDX]);
            }
            catch (ClassNotFoundException e) {
                Log.e("ScheduledTableMapper", "Invalid mapping for class type: " + split[Mappings.JTIDX]);
                cls = String.class;
            } 
            Field f = Field.create(
                    split[Mappings.DNIDX], 
                    split[Mappings.DTIDX],
                    split[Mappings.JNIDX], 
                    cls);
            
            f.setPrimaryKey(Boolean.valueOf(split[Mappings.PKIDX]));
            f.setNullable(!Boolean.valueOf(split[Mappings.NNIDX]));
            f.setAutoincrement(Boolean.valueOf(split[Mappings.ACIDX]));
            table.getFields().add(f);
        }
    }

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

        List<Field> fields = table.getFields();
        int index = 1;
        for (Field f : fields) {
            try {
                safeBind(stmt, index, f, task);
            }
            catch (Exception e) {
                Log.e("ScheduledTaskMapper", MiscUtils.stackTrace2String(e));
            }
            index++;
        }

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
        List<Field> fields = table.getFields();
        for (Field f : fields) {
            setField(cursor, a, f.getDbName(), f.getMutatorName(),
                    f.getJavaType());
        }

        return a;
    }
}