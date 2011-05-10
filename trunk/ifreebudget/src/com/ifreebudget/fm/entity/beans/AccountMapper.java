package com.ifreebudget.fm.entity.beans;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class AccountMapper extends AbstractTableMapper {

    private final String TABLE_NAME = "ACCOUNT";

    private final String TAG = "AccountMapper";

    private final String CREATE_SQL = "(ACCOUNTID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
            + "ACCOUNTTYPE INTEGER,"
            + "ACCOUNTPARENTTYPE INTEGER,"
            + "STATUS INTEGER,"
            + "ACCOUNTNAME VARCHAR(512),"
            + "ACCOUNTNUMBER VARCHAR(512),"
            + "ACCOUNTNOTES VARCHAR(1024),"
            + "STARTDATE TIMESTAMP,"
            + "STARTINGBALANCE DECIMAL(10, 5),"
            + "CURRENTBALANCE DECIMAL(10, 5), CATEGORYID INTEGER)";

    private final String INSERT_SQL = "(ACCOUNTID, " + "ACCOUNTTYPE, "
            + "ACCOUNTPARENTTYPE, " + "STATUS, " + "ACCOUNTNAME, "
            + "ACCOUNTNUMBER, " + "ACCOUNTNOTES, " + "STARTDATE, "
            + "STARTINGBALANCE, " + "CURRENTBALANCE, "
            + "CATEGORYID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private final String UPDATE_SQL = "ACCOUNTNAME = ?, "
            + "ACCOUNTNUMBER = ? , " + "ACCOUNTNOTES = ? , "
            + "CURRENTBALANCE = ? WHERE ACCOUNTID = ?";

    private final String RETRIEVE_SQL = "SELECT * FROM " + TABLE_NAME;

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

    public List<FManEntity> getList(SQLiteDatabase database, String filter,
            int offset, int limit) {
        return getList(database, filter, null, null, offset, limit);
    }

    public List<FManEntity> getList(SQLiteDatabase database, String filter,
            String[] order, String[] direction, int offset, int limit) {

        StringBuilder q = new StringBuilder(RETRIEVE_SQL);
        if (filter != null) {
            q.append(SPACE);
            q.append(filter);
        }

        String orderBy = buildOrderByClause(order, direction);

        q.append(orderBy);

        return executeListQuery(database, q.toString());
    }

    public Object doInsert(SQLiteDatabase database, FManEntity entity)
            throws SQLException {
        Account account = (Account) entity;

        SQLiteStatement stmt = database.compileStatement(getInsertSql());

        stmt.clearBindings();

        stmt.bindNull(1);
        safeBindInt(stmt, 2, account.getAccountType());
        safeBindInt(stmt, 3, account.getAccountParentType());
        safeBindInt(stmt, 4, account.getStatus());
        safeBindString(stmt, 5, account.getAccountName());
        safeBindString(stmt, 6, account.getAccountNumber());
        safeBindString(stmt, 7, account.getAccountNotes());
        safeBindLong(stmt, 8, account.getStartDate());
        safeBindBigDecimal(stmt, 9, account.getStartingBalance());
        safeBindBigDecimal(stmt, 10, account.getCurrentBalance());
        safeBindLong(stmt, 11, account.getCategoryId());

        Long id = stmt.executeInsert();

        account.setAccountId(id);

        return id;
    }

    public void doUpdate(SQLiteDatabase database, FManEntity entity)
            throws SQLException {
        Account account = (Account) entity;

        SQLiteStatement stmt = database.compileStatement(getUpdateSql());

        stmt.clearBindings();

        safeBindString(stmt, 1, account.getAccountName());
        safeBindString(stmt, 2, account.getAccountNumber());
        safeBindString(stmt, 3, account.getAccountNotes());
        safeBindBigDecimal(stmt, 4, account.getCurrentBalance());
        safeBindLong(stmt, 5, account.getAccountId());

        stmt.execute();
    }

    public String getTableName() {
        return TABLE_NAME;
    }

    public FManEntity getCurrentEntityFromCursor(Cursor cursor) {
        Account a = new Account();
        setField(cursor, a, "ACCOUNTID", "setAccountId", Long.class);
        setField(cursor, a, "ACCOUNTTYPE", "setAccountType", Integer.class);
        setField(cursor, a, "ACCOUNTPARENTTYPE", "setAccountParentType",
                Integer.class);
        setField(cursor, a, "STATUS", "setStatus", Integer.class);
        setField(cursor, a, "ACCOUNTNAME", "setAccountName", String.class);
        setField(cursor, a, "ACCOUNTNUMBER", "setAccountNumber", String.class);
        setField(cursor, a, "ACCOUNTNOTES", "setAccountNotes", String.class);
        setField(cursor, a, "STARTDATE", "setStartDate", Long.class);
        setField(cursor, a, "STARTINGBALANCE", "setStartingBalance",
                BigDecimal.class);
        setField(cursor, a, "CURRENTBALANCE", "setCurrentBalance",
                BigDecimal.class);
        setField(cursor, a, "CATEGORYID", "setCategoryId", Long.class);

        return a;
    }
}
