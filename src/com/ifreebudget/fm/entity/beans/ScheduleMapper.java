package com.ifreebudget.fm.entity.beans;

import com.ifreebudget.fm.entity.Mappings;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class ScheduleMapper extends DefaultAbstractTableMapper {

    private final String TABLENAME = "SCHED";
    private final String TAG = "ScheduleMapper";
    
    public ScheduleMapper() {
        super();
    }
    
    @Override
    public String getTableName() {
        return TABLENAME;
    }

    public String getUpdateSql() {
        throw new RuntimeException(getTag() + "-Update not supported");
    }
    
    @Override
    public void doUpdate(SQLiteDatabase database, FManEntity entity)
            throws SQLException {
        throw new RuntimeException(getTag() + "-Update task not supported.");
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    protected String[] getMappings() {
        return Mappings.ScheduleMapperMappings;
    }
}
