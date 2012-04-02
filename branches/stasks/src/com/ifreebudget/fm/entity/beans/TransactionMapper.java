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
import java.util.Set;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class TransactionMapper extends AbstractTableMapper {

    private final String TABLE_NAME = "FMTRANSACTION";

    private final String CREATE_SQL = "("
            + "TXID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
            + "FITID VARCHAR(255)," + "FROMACCOUNTID INTEGER,"
            + "TOACCOUNTID INTEGER," + "TXAMOUNT DECIMAL(10, 5),"
            + "FROMACCOUNTENDINGBAL DECIMAL(10, 5),"
            + "TOACCOUNTENDINGBAL DECIMAL(10, 5)," + "TXNOTES VARCHAR(512),"
            + "TXDATE BIGINT," + "CREATEDATE BIGINT," + "TXSTATUS INTEGER,"
            + "PARENTTXID INTEGER," + "ATTCHMNTPATH VARCHAR(255)" + ")";

    private final String INSERT_SQL = "(" + "TXID, " + "FITID, "
            + "FROMACCOUNTID, " + "TOACCOUNTID, " + "TXAMOUNT, "
            + "FROMACCOUNTENDINGBAL, " + "TOACCOUNTENDINGBAL, " + "TXNOTES, "
            + "TXDATE, " + "CREATEDATE, " + "TXSTATUS, "
            + "PARENTTXID, ATTCHMNTPATH) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final String RETRIEVE_SQL = "SELECT * FROM " + TABLE_NAME;

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

        ret.append("CREATE TABLE IF NOT EXISTS " + TABLE_NAME);
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

    public Object doInsert(SQLiteDatabase database, FManEntity entity) {
        Transaction tx = (Transaction) entity;

        SQLiteStatement stmt = database.compileStatement(getInsertSql());

        stmt.clearBindings();

        stmt.bindNull(1);
        safeBindString(stmt, 2, tx.getFitid());
        safeBindLong(stmt, 3, tx.getFromAccountId());
        safeBindLong(stmt, 4, tx.getToAccountId());
        safeBindBigDecimal(stmt, 5, tx.getTxAmount());
        safeBindBigDecimal(stmt, 6, tx.getFromAccountEndingBal());
        safeBindBigDecimal(stmt, 7, tx.getToAccountEndingBal());
        safeBindString(stmt, 8, tx.getTxNotes());
        safeBindLong(stmt, 9, tx.getTxDate());
        safeBindLong(stmt, 10, tx.getCreateDate());
        safeBindInt(stmt, 11, tx.getTxStatus());
        safeBindLong(stmt, 12, tx.getParentTxId());
        safeBindString(stmt, 13, tx.getAttachmentPath());

        Long id = stmt.executeInsert();

        tx.setTxId(id);

        return id;
    }

    public void doUpdate(SQLiteDatabase database, FManEntity entity)
            throws SQLException {
    }

    public FManEntity getCurrentEntityFromCursor(Cursor cursor) {
        Transaction a = new Transaction();
        setField(cursor, a, "TXID", "setTxId", Long.class);

        setField(cursor, a, "FITID", "setFitid", String.class);

        setField(cursor, a, "FROMACCOUNTID", "setFromAccountId", Long.class);

        setField(cursor, a, "TOACCOUNTID", "setToAccountId", Long.class);

        setField(cursor, a, "TXAMOUNT", "setTxAmount", BigDecimal.class);

        setField(cursor, a, "TXNOTES", "setTxNotes", String.class);

        setField(cursor, a, "TXDATE", "setTxDate", Long.class);

        setField(cursor, a, "CREATEDATE", "setCreateDate", Long.class);

        setField(cursor, a, "FROMACCOUNTENDINGBAL", "setFromAccountEndingBal",
                BigDecimal.class);

        setField(cursor, a, "TOACCOUNTENDINGBAL", "setToAccountEndingBal",
                BigDecimal.class);

        setField(cursor, a, "TXSTATUS", "setTxStatus", Integer.class);

        setField(cursor, a, "PARENTTXID", "setParentTxID", Long.class);

        setField(cursor, a, "ATTCHMNTPATH", "setAttachmentPath", String.class);
        
        return a;
    }

    @Override
    public List<FManEntity> getList(SQLiteDatabase database, String filter,
            int offset, int limit) {

        StringBuilder q = new StringBuilder(RETRIEVE_SQL);
        if (filter != null) {
            q.append(SPACE);
            q.append(filter);
        }
        if (limit > 0 && offset > 0) {
            q.append(SPACE);
            q.append("LIMIT");
            q.append(SPACE);
            q.append(offset);
            q.append(COMMA);
            q.append(limit);
        }

        return executeListQuery(database, q.toString());
    }
}
