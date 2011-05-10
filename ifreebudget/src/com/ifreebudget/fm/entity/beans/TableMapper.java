package com.ifreebudget.fm.entity.beans;

import java.util.List;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public interface TableMapper {

    String getTableName();

    String getDropSql();

    String getCreateSql();

    String getInsertSql();

    String getRetrieveSql();

    Object doInsert(SQLiteDatabase database, FManEntity entity)
            throws SQLException;

    void doUpdate(SQLiteDatabase database, FManEntity entity)
            throws SQLException;

    int doDelete(SQLiteDatabase database, FManEntity entity)
            throws SQLException;

    List<FManEntity> getList(SQLiteDatabase database, String filter,
            int offset, int limit);

    List<FManEntity> getList(SQLiteDatabase database, String filter,
            String[] orderColumns, String[] direction, int offset, int limit);

    FManEntity getCurrentEntityFromCursor(Cursor cursor);
}
