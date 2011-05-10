package com.ifreebudget.fm.entity.beans;

import java.math.BigDecimal;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class BudgetedAccountMapper extends AbstractTableMapper {

    private final String TABLE_NAME = "BUDGETEDACCOUNT";

    private final String CREATE_SQL = "("
            + "ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
            + "BUDGETID INTEGER NOT NULL," + "ACCOUNTID INTEGER NOT NULL,"
            + "ALLOCATEDAMOUNT  DECIMAL(10, 5))";

    private final String INSERT_SQL = "(" + "ID, " + "BUDGETID, "
            + "ACCOUNTID, " + "ALLOCATEDAMOUNT " + ") VALUES (?, ?, ?, ?)";

    private final String RETRIEVE_SQL = "SELECT * FROM " + TABLE_NAME;

    private final String TAG = "BudgetMapper";

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
        BudgetedAccount ac = (BudgetedAccount) entity;

        SQLiteStatement stmt = database.compileStatement(getInsertSql());

        stmt.clearBindings();

        stmt.bindNull(1);
        safeBindLong(stmt, 2, ac.getBudgetId());
        safeBindLong(stmt, 3, ac.getAccountId());
        safeBindBigDecimal(stmt, 4, ac.getAllocatedAmount());

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
        BudgetedAccount a = new BudgetedAccount();
        setField(cursor, a, "ID", "setId", Long.class);
        setField(cursor, a, "BUDGETID", "setBudgetId", Long.class);
        setField(cursor, a, "ACCOUNTID", "setAccountId", Long.class);
        setField(cursor, a, "ALLOCATEDAMOUNT", "setAllocatedAmount",
                BigDecimal.class);

        return a;
    }

}
