package com.ifreebudget.fm.entity.beans;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class CategoryIconMapMapper extends AbstractTableMapper {

    private final String TABLE_NAME = "CATEGORYICONMAP";

    private final String TAG = "AccountMapper";

    private final String RETRIEVE_SQL = "SELECT * FROM " + TABLE_NAME;

    private final String CREATE_SQL = "(CATEGORYID INTEGER NOT NULL PRIMARY KEY,"
            + "ICONPATH VARCHAR(128))";

    private final String INSERT_SQL = "(CATEGORYID, ICONPATH) VALUES (?, ?)";

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
        CategoryIconMap account = (CategoryIconMap) entity;

        SQLiteStatement stmt = database.compileStatement(getInsertSql());

        stmt.clearBindings();

        safeBindLong(stmt, 1, account.getCategoryId());
        safeBindString(stmt, 2, account.getIconPath());

        Long id = stmt.executeInsert();

        account.setId(id);

        return id;
    }

    @Override
    public void doUpdate(SQLiteDatabase database, FManEntity entity)
            throws SQLException {
        // TODO Auto-generated method stub

    }

    public List<FManEntity> getList(SQLiteDatabase database, String filter,
            int offset, int limit) {

        String q = RETRIEVE_SQL;
        if (filter != null) {
            q += SPACE + filter;
        }

        Cursor cursor = null;
        try {
            cursor = database.rawQuery(q, null);
            if (cursor == null) {
                return EMPTY_LIST;
            }
            else {
                int sz = cursor.getCount();
                ArrayList<FManEntity> ret = new ArrayList<FManEntity>(sz);
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    CategoryIconMap a = (CategoryIconMap) getCurrentEntityFromCursor(cursor);
                    ret.add(a);
                    cursor.moveToNext();
                }
                return ret;
            }
        }
        finally {
            closeCursor(cursor);
        }
    }

    @Override
    public FManEntity getCurrentEntityFromCursor(Cursor cursor) {
        CategoryIconMap a = new CategoryIconMap();
        setField(cursor, a, "CATEGORYID", "setCategoryId", Long.class);
        setField(cursor, a, "ICONPATH", "setIconPath", String.class);

        return a;
    }
}
