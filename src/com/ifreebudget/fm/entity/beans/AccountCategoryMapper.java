/*******************************************************************************
 * Copyright 2011 ifreebudget@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.ifreebudget.fm.entity.beans;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class AccountCategoryMapper extends AbstractTableMapper {

    private final String TABLE_NAME = "ACCOUNTCATEGORY";

    private final String CREATE_SQL = "("
            + "CATEGORYID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
            + "PARENTCATEGORYID INTEGER," + "CATEGORYNAME VARCHAR(256)" + ")";

    private final String INSERT_SQL = "(" + "CATEGORYID, "
            + "PARENTCATEGORYID, " + "CATEGORYNAME " + ") VALUES (?, ?, ?)";

    private final String RETRIEVE_SQL = "SELECT * FROM " + TABLE_NAME;

    private final String UPDATE_SQL = "CATEGORYNAME = ? WHERE CATEGORYID=?";

    private final String TAG = "AccountCategoryMapper";

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

    public String getUpdateSql() {
        StringBuilder ret = new StringBuilder();

        ret.append("UPDATE");
        ret.append(SPACE);
        ret.append(TABLE_NAME);
        ret.append(SPACE);
        ret.append("SET");
        ret.append(SPACE);
        ret.append(UPDATE_SQL);

        return ret.toString();
    }

    public Object doInsert(SQLiteDatabase database, FManEntity entity) {
        AccountCategory ac = (AccountCategory) entity;

        SQLiteStatement stmt = database.compileStatement(getInsertSql());

        stmt.clearBindings();

        if (ac.getCategoryId() != null) {
            stmt.bindLong(1, ac.getCategoryId());
        }
        else {
            stmt.bindNull(1);
        }
        stmt.bindLong(2, ac.getParentCategoryId());
        stmt.bindString(3, ac.getCategoryName());

        Long id = stmt.executeInsert();

        ac.setCategoryId(id);

        return id;
    }

    public void doUpdate(SQLiteDatabase database, FManEntity entity)
            throws SQLException {
        AccountCategory account = (AccountCategory) entity;

        SQLiteStatement stmt = database.compileStatement(getUpdateSql());

        stmt.clearBindings();

        safeBindString(stmt, 1, account.getCategoryName());
        safeBindLong(stmt, 2, account.getCategoryId());

        stmt.execute();
    }

    // public List<FManEntity> getList(SQLiteDatabase database, String filter,
    // int offset, int limit) {
    //
    // String q = RETRIEVE_SQL;
    // if (filter != null) {
    // q += filter;
    // }
    // Cursor cursor = null;
    // try {
    // cursor = database.rawQuery(q, null);
    // if (cursor == null) {
    // return EMPTY_LIST;
    // }
    // else {
    // int sz = cursor.getCount();
    // ArrayList<FManEntity> ret = new ArrayList<FManEntity>(sz);
    // cursor.moveToFirst();
    // while (!cursor.isAfterLast()) {
    // AccountCategory a = (AccountCategory) getCurrentEntityFromCursor(cursor);
    // ret.add(a);
    // cursor.moveToNext();
    // }
    // return ret;
    // }
    // }
    // finally {
    // closeCursor(cursor);
    // }
    // }

    public FManEntity getCurrentEntityFromCursor(Cursor cursor) {
        AccountCategory a = new AccountCategory();
        setField(cursor, a, "CATEGORYID", "setCategoryId", Long.class);
        setField(cursor, a, "PARENTCATEGORYID", "setParentCategoryId",
                Long.class);
        setField(cursor, a, "CATEGORYNAME", "setCategoryName", String.class);

        return a;
    }
}
