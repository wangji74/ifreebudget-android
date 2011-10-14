package com.ifreebudget.fm.entity.beans;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.ifreebudget.fm.entity.Mappings;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class ScheduleEntityMapper extends DefaultAbstractTableMapper {

    private final String TABLENAME = "SCHED";
    private final String TAG = "ScheduleMapper";

    private Set<String> fieldsToUpdate = new LinkedHashSet<String>();

    public ScheduleEntityMapper() {
        super();
    }

    @Override
    public String getTableName() {
        return TABLENAME;
    }

    public String getUpdateSql() {
        fieldsToUpdate.clear();
        fieldsToUpdate.add("NextRunTime");
        fieldsToUpdate.add("LastRunTime");
        return table.getUpdateSql(fieldsToUpdate);
    }

    @Override
    public void doUpdate(SQLiteDatabase database, FManEntity entity)
            throws SQLException {
        ScheduleEntity se = (ScheduleEntity) entity;

        String sql = getUpdateSql();
        if(sql == null) {
            return;
        }
        SQLiteStatement stmt = database.compileStatement(sql);

        stmt.clearBindings();

        safeBindLong(stmt, 1, se.getNextRunTime());
        safeBindLong(stmt, 2, se.getNextRunTime());

        stmt.execute();
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    protected String[] getMappings() {
        return Mappings.ScheduleMapperMappings;
    }

    @Override
    protected FManEntity self() {
        return new ScheduleEntity();
    }
}
