package com.ifreebudget.fm.entity.beans;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.ifreebudget.fm.entity.Mappings;

public class TaskEntityMapper extends DefaultAbstractTableMapper {

    private final String TABLENAME = "STASK";
    private final String TAG = "ScheduledTaskMapper";
    
    public TaskEntityMapper() {
        super();
    }

    @Override
    public String getTableName() {
        return TABLENAME;
    }
    
    public String getUpdateSql() {
        throw new RuntimeException(getTag() + "-Update not supported");
    }

    public void doUpdate(SQLiteDatabase database, FManEntity entity)
            throws SQLException {
        throw new RuntimeException(getTag() + "-Update not supported");
    }
    
    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    protected String[] getMappings() {
        return Mappings.ScheduledTaskMapperMappings;
    } 
    
    @Override
    protected FManEntity self() {
        return new TaskEntity();
    }
}
