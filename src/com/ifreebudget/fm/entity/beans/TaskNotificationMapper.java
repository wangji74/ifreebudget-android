package com.ifreebudget.fm.entity.beans;

import com.ifreebudget.fm.entity.Mappings;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class TaskNotificationMapper extends DefaultAbstractTableMapper {

    private final String TABLENAME = "TASKNOTIF";
    private final String TAG = "TaskNotificationMapper";

    public TaskNotificationMapper() {
        super();
    }

    @Override
    public String getTableName() {
        return TABLENAME;
    }

    @Override
    public void doUpdate(SQLiteDatabase database, FManEntity entity)
            throws SQLException {
        throw new RuntimeException("Do not use!!!");
    }

    @Override
    protected String getTag() {
        return TAG;
    }

    @Override
    protected String[] getMappings() {
        return Mappings.TaskNotificationMapperMappings;
    }

    @Override
    protected FManEntity self() {
        return new TaskNotification();
    }

}
