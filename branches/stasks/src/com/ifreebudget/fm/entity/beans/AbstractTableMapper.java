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

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.ifreebudget.fm.entity.Field;
import com.ifreebudget.fm.utils.MiscUtils;

public abstract class AbstractTableMapper implements TableMapper {
    private final String TAG = "AbstractTableMapper";

    public static final ArrayList<FManEntity> EMPTY_LIST = new ArrayList<FManEntity>();

    public static final String SPACE = " ";
    public static final String COMMA = ",";

    public String getDropSql() {
        return "DROP TABLE IF EXISTS " + getTableName();
    }

    protected void closeCursor(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }

    public int doDelete(SQLiteDatabase database, FManEntity entity) {
        String filter = entity.getPKColumnName() + " = " + entity.getPK();
        return database.delete(getTableName(), filter, null);
    }

    protected void setField(Cursor cursor, FManEntity entity, String colName,
            String fieldName, Class<?> type) {
        int colIdx = cursor.getColumnIndex(colName);
        if (colIdx < 0) {
            Log.w(TAG, "Column: " + colName + " not found in result set.");
        }
        Object[] val = { getValueFromCursor(cursor, type, colIdx) };
        try {
            Method m = entity.getClass().getMethod(fieldName, type);
            m.invoke(entity, val);
        }
        catch (Exception e1) {
            Log.e(TAG, "Method not found " + fieldName + " on object : " + type);
            setFieldFallback(entity, fieldName, val);
        }
    }

    private void setFieldFallback(FManEntity entity, String fieldName,
            Object[] val) {
        Method[] arr = entity.getClass().getDeclaredMethods();
        for (int i = 0; i < arr.length; i++) {
            Method c = arr[i];
            if (c.getName().equals(fieldName)) {
                try {
                    c.invoke(entity, val);
                }
                catch (Exception e) {
                    Log.e(TAG, MiscUtils.stackTrace2String(e));
                }
            }
        }
    }

    protected Object getValueFromCursor(Cursor c, Class<?> type, int columnIndex) {
        if (type.equals(String.class)) {
            return c.getString(columnIndex);
        }
        else if (type.equals(Long.class)) {
            return c.getLong(columnIndex);
        }
        else if (type.equals(BigDecimal.class)) {
            return BigDecimal.valueOf(c.getDouble(columnIndex));
        }
        else if (type.equals(Integer.class)) {
            return c.getInt(columnIndex);
        }
        else {
            return c.getString(columnIndex);
        }
    }

    protected void safeBind(SQLiteStatement stmt, int index, Field f,
            FManEntity entity) throws Exception {

        if (f.isPrimaryKey()) {
            stmt.bindNull(index);
            return;
        }

        Object val = f.getValue(entity);
        Class<?> clsType = f.getJavaType();
        String type = clsType.getName();
        if (type.equals("java.lang.String")) {
            safeBindString(stmt, index, (String) val);
        }
        else if (type.equals("java.lang.Long")) {
            safeBindLong(stmt, index, (Long) val);
        }
        else if (type.equals("java.lang.Integer")) {
            safeBindInt(stmt, index, (Integer) val);
        }
        else if (type.equals("java.math.Double")) {
            safeBindDouble(stmt, index, (Double) val);
        }
        else if (type.equals("java.math.BigDecimal")) {
            safeBindBigDecimal(stmt, index, (BigDecimal) val);
        }
    }

    protected void safeBindBigDecimal(SQLiteStatement stmt, int index,
            BigDecimal value) {
        if (value == null) {
            stmt.bindNull(index);
        }
        else {
            stmt.bindDouble(index, value.doubleValue());
        }
    }

    protected void safeBindLong(SQLiteStatement stmt, int index, Long value) {
        if (value == null) {
            stmt.bindNull(index);
        }
        else {
            stmt.bindLong(index, value);
        }
    }

    protected void safeBindInt(SQLiteStatement stmt, int index, Integer value) {
        if (value == null) {
            stmt.bindNull(index);
        }
        else {
            stmt.bindLong(index, value);
        }
    }

    protected void safeBindString(SQLiteStatement stmt, int index, String value) {
        if (value == null) {
            stmt.bindNull(index);
        }
        else {
            stmt.bindString(index, value);
        }
    }

    protected void safeBindDouble(SQLiteStatement stmt, int index, Double value) {
        if (value == null) {
            stmt.bindNull(index);
        }
        else {
            stmt.bindDouble(index, value);
        }
    }

    public List<FManEntity> getList(SQLiteDatabase database, String filter,
            int offset, int limit) {

        String q = getRetrieveSql();
        if (filter != null) {
            q += SPACE;
            q += filter;
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
                    FManEntity a = (FManEntity) getCurrentEntityFromCursor(cursor);
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

    public List<FManEntity> getList(SQLiteDatabase database, String filter,
            String[] orderColumns, String[] direction, int offset, int limit) {
        return null;
    }

    protected String buildOrderByClause(String[] orderColumns,
            String[] orderDirection) {
        StringBuilder ret = new StringBuilder();

        if (orderColumns == null || orderDirection == null
                || orderColumns.length != orderDirection.length) {
            return ret.toString();
        }

        ret.append(" ORDER BY ");
        int len = orderColumns.length;
        for (int i = 0; i < len; i++) {
            ret.append(orderColumns[i]);
            ret.append(SPACE);
            ret.append(orderDirection[i]);
            if (i < len - 1) {
                ret.append(", ");
            }
        }
        return ret.toString();
    }

    protected List<FManEntity> executeListQuery(SQLiteDatabase database,
            String query) {
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(query.toString(), null);
            if (cursor == null) {
                return EMPTY_LIST;
            }
            else {
                int sz = cursor.getCount();
                ArrayList<FManEntity> ret = new ArrayList<FManEntity>(sz);
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    FManEntity a = getCurrentEntityFromCursor(cursor);
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

    public abstract FManEntity getCurrentEntityFromCursor(Cursor cursor);
}
