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

public abstract class DefaultAbstractTableMapper extends AbstractTableMapper {

    protected Table table;

    protected abstract String getTag();

    protected abstract String[] getMappings();

    protected abstract FManEntity self();

    public DefaultAbstractTableMapper() {
        String[] mappings = getMappings();

        table = new Table(getTableName());

        for (String s : mappings) {
            String[] split = s.split(":");
            Class<?> cls = null;
            try {
                cls = Class.forName(split[Mappings.JTIDX]);
            }
            catch (ClassNotFoundException e) {
                Log.e(getTag(), "Invalid mapping for class type: "
                        + split[Mappings.JTIDX]);
                cls = String.class;
            }
            Field f = Field.create(split[Mappings.DNIDX],
                    split[Mappings.DTIDX], split[Mappings.JNIDX], cls);

            f.setPrimaryKey(Boolean.valueOf(split[Mappings.PKIDX]));
            f.setNullable(!Boolean.valueOf(split[Mappings.NNIDX]));
            f.setAutoincrement(Boolean.valueOf(split[Mappings.ACIDX]));
            table.getFields().add(f);
        }
    }

    @Override
    public String getCreateSql() {
        return table.getCreateSql();
    }

    @Override
    public String getInsertSql() {
        return table.getInsertSql();
    }

    @Override
    public String getRetrieveSql() {
        return table.getRetrieveSql();
    }

    @Override
    public List<FManEntity> getList(SQLiteDatabase database, String filter,
            int offset, int limit) {
        return getList(database, filter, null, null, offset, limit);
    }

    @Override
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

    @Override
    public Object doInsert(SQLiteDatabase database, FManEntity entity)
            throws SQLException {
        SQLiteStatement stmt = database.compileStatement(getInsertSql());

        stmt.clearBindings();

        List<Field> fields = table.getFields();
        int index = 1;
        for (Field f : fields) {
            try {
                safeBind(stmt, index, f, entity);
            }
            catch (Exception e) {
                Log.e(getTag(), MiscUtils.stackTrace2String(e));
            }
            index++;
        }

        Long id = stmt.executeInsert();

        entity.setPK(id);

        return id;
    }

    @Override
    public FManEntity getCurrentEntityFromCursor(Cursor cursor) {
        FManEntity a = self();
        List<Field> fields = table.getFields();
        for (Field f : fields) {
            setField(cursor, a, f.getDbName(), f.getMutatorName(),
                    f.getJavaType());
        }

        return a;
    }
}
