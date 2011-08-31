package com.ifreebudget.fm.entity.beans;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.ifreebudget.fm.entity.Mappings;

public class ConstraintMapper extends DefaultAbstractTableMapper {

    private final String TABLENAME = "CONSTRT";
    private final String TAG = "ConstraintMapper";
    
    public ConstraintMapper() {
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
        return Mappings.ConstraintMapperMappings;
    }    
}
