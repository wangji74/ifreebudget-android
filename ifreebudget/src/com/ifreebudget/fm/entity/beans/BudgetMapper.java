package com.ifreebudget.fm.entity.beans;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class BudgetMapper extends AbstractTableMapper {

    private final String TABLE_NAME = "BUDGET";

    private final String CREATE_SQL = "("
            + "ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
            + "NAME VARCHAR(256), " + "TYPE INTEGER)";

    private final String INSERT_SQL = "(" + "ID, " + "NAME, " + "TYPE "
            + ") VALUES (?, ?, ?)";

    private final String RETRIEVE_SQL = "SELECT * FROM " + TABLE_NAME;

    private final String TAG = "BudgetMapper";

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getRetrieveSql() {
        return RETRIEVE_SQL;
    }

    @Override
    public String getCreateSql() {
        StringBuilder ret = new StringBuilder();

        ret.append("CREATE TABLE " + TABLE_NAME);
        ret.append(" ");
        ret.append(CREATE_SQL);
        return ret.toString();
    }

    @Override
    public String getInsertSql() {
        StringBuilder ret = new StringBuilder();

        ret.append("INSERT INTO " + TABLE_NAME);
        ret.append(INSERT_SQL);

        return ret.toString();
    }

    @Override
    public Object doInsert(SQLiteDatabase database, FManEntity entity)
            throws SQLException {
        Budget ac = (Budget) entity;

        SQLiteStatement stmt = database.compileStatement(getInsertSql());

        stmt.clearBindings();

        stmt.bindNull(1);
        safeBindString(stmt, 2, ac.getName());
        safeBindInt(stmt, 3, ac.getType());

        Long id = stmt.executeInsert();

        ac.setId(id);

        return id;
    }

    @Override
    public void doUpdate(SQLiteDatabase database, FManEntity entity)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    @Override
    public FManEntity getCurrentEntityFromCursor(Cursor cursor) {
        Budget b = new Budget();
        setField(cursor, b, "ID", "setId", Long.class);
        setField(cursor, b, "NAME", "setName", String.class);
        setField(cursor, b, "TYPE", "setType", Integer.class);

        return b;
    }

}