package com.ifreebudget.fm.entity.beans;

import java.util.List;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class TxHistoryMapper extends AbstractTableMapper {
    private final String TABLE_NAME = "TXHISTORY";

    private final String CREATE_SQL = "("
            + "ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
            + "FROMACCOUNTID INTEGER NOT NULL,"
            + "TOACCOUNTID INTEGER NOT NULL," + "CNT INTEGER,"
            + "LOC VARCHAR(512)," + "LASTUPDATE BIGINT, "
            + "UNIQUE (FROMACCOUNTID, TOACCOUNTID)" + ")";

    private final String INSERT_SQL = "(ID, FROMACCOUNTID, TOACCOUNTID, CNT, LOC, LASTUPDATE) VALUES (?, ?, ?, ?, ?, ?)";

    private final String RETRIEVE_SQL = "SELECT * FROM " + TABLE_NAME;

    private final String UPDATE_SQL = "CNT=?, LASTUPDATE=? WHERE ID=?";

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getCreateSql() {
        StringBuilder ret = new StringBuilder();

        ret.append("CREATE TABLE " + TABLE_NAME);
        ret.append(" ");
        ret.append(CREATE_SQL);
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

    @Override
    public String getInsertSql() {
        StringBuilder ret = new StringBuilder();

        ret.append("INSERT INTO " + TABLE_NAME);
        ret.append(INSERT_SQL);

        return ret.toString();
    }

    @Override
    public String getRetrieveSql() {
        return RETRIEVE_SQL;
    }

    @Override
    public Object doInsert(SQLiteDatabase database, FManEntity entity)
            throws SQLException {
        TxHistory txh = (TxHistory) entity;

        SQLiteStatement stmt = database.compileStatement(getInsertSql());

        stmt.clearBindings();

        int idx = 0;
        stmt.bindNull(++idx);
        safeBindLong(stmt, ++idx, txh.getFromAccountId());
        safeBindLong(stmt, ++idx, txh.getToAccountId());
        safeBindInt(stmt, ++idx, txh.getCount());
        safeBindString(stmt, ++idx, txh.getLoc());

        Long id = stmt.executeInsert();

        txh.setId(id);

        return id;
    }

    @Override
    public void doUpdate(SQLiteDatabase database, FManEntity entity)
            throws SQLException {
        TxHistory txh = (TxHistory) entity;

        SQLiteStatement stmt = database.compileStatement(getUpdateSql());

        stmt.clearBindings();

        safeBindInt(stmt, 1, txh.getCount());
        safeBindLong(stmt, 2, txh.getLastUpdate());
        safeBindLong(stmt, 3, txh.getId());

        stmt.execute();
    }

    @Override
    public List<FManEntity> getList(SQLiteDatabase database, String filter,
            String[] order, String[] direction, int offset, int limit) {

        StringBuilder q = new StringBuilder(RETRIEVE_SQL);
        if (filter != null) {
            q.append(SPACE);
            q.append(filter);
        }
        String orderBy = buildOrderByClause(order, direction);
        q.append(orderBy);
        if (limit > 0) {
            q.append(SPACE);
            q.append("LIMIT");
            q.append(SPACE);
            q.append(limit);
        }
        if (offset >= 0) {
            q.append(SPACE);
            q.append("OFFSET");
            q.append(SPACE);
            q.append(offset);
        }
        return executeListQuery(database, q.toString());
    }

    @Override
    public FManEntity getCurrentEntityFromCursor(Cursor cursor) {
        TxHistory a = new TxHistory();
        setField(cursor, a, "ID", "setId", Long.class);
        setField(cursor, a, "FROMACCOUNTID", "setFromAccountId", Long.class);
        setField(cursor, a, "TOACCOUNTID", "setToAccountId", Long.class);
        setField(cursor, a, "CNT", "setCount", Integer.class);
        setField(cursor, a, "LOC", "setLoc", String.class);

        return a;
    }
}
