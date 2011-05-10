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
