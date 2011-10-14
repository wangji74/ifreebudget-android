package com.ifreebudget.fm.entity;

import java.lang.reflect.Method;

import com.ifreebudget.fm.entity.beans.FManEntity;

public class Field {
    private String dbName;
    private String dbType;
    private String mutatorName;
    private String accessorName;
    private boolean isNullable;
    private boolean isPrimaryKey;
    private boolean isAutoincrement;
    private Class<?> javaType;
    private String extra;

    public static final String SPACE = " ";
    public static final String NOT_NULL = "NOT NULL";
    public static final String PRIM_KEY = "PRIMARY KEY";
    public static final String AUTO_INC = "AUTOINCREMENT";
    public static final String OPENB = "(";
    public static final String CLOSEB = ")";
    public static final String VALUES = "VALUES";
    public static final String EQ = "=";

    public static Field create(String dbName, String dbType, String javaName,
            Class<?> javaType) {
        Field f = new Field();
        f.dbName = dbName;
        f.dbType = dbType;

        f.mutatorName = "set" + javaName;
        f.accessorName = "get" + javaName;

        f.isPrimaryKey = false;
        f.isNullable = false;
        f.isAutoincrement = false;
        f.javaType = javaType;
        return f;
    }

    public Object getValue(FManEntity entity) throws Exception {
        Method m = entity.getClass().getMethod(accessorName);
        Object val = m.invoke(entity);
        return val;
    }

    public String getSql() {
        StringBuilder ret = new StringBuilder();

        ret.append(SPACE);
        ret.append(dbName);
        ret.append(SPACE);
        ret.append(dbType);
        if (!isNullable) {
            ret.append(SPACE);
            ret.append(NOT_NULL);
        }
        if (isPrimaryKey) {
            ret.append(SPACE);
            ret.append(PRIM_KEY);
        }
        if (isAutoincrement) {
            ret.append(SPACE);
            ret.append(AUTO_INC);
        }
        if (extra != null) {
            ret.append(SPACE);
            ret.append(extra);
        }
        ret.append(SPACE);
        return ret.toString();
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public void setJavaType(Class<?> javaType) {
        this.javaType = javaType;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public boolean isAutoincrement() {
        return isAutoincrement;
    }

    public void setAutoincrement(boolean isAutoincrement) {
        this.isAutoincrement = isAutoincrement;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getMutatorName() {
        return mutatorName;
    }

    public void setMutatorName(String mutatorName) {
        this.mutatorName = mutatorName;
    }

    public String getAccessorName() {
        return accessorName;
    }

    public void setAccessorName(String accessorName) {
        this.accessorName = accessorName;
    }

    public boolean isNullable() {
        return isNullable;
    }

    public void setNullable(boolean isNullable) {
        this.isNullable = isNullable;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public void setPrimaryKey(boolean isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dbName == null) ? 0 : dbName.hashCode());
        result = prime * result + ((dbType == null) ? 0 : dbType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Field other = (Field) obj;
        if (dbName == null) {
            if (other.dbName != null)
                return false;
        }
        else if (!dbName.equals(other.dbName))
            return false;
        if (dbType == null) {
            if (other.dbType != null)
                return false;
        }
        else if (!dbType.equals(other.dbType))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Field [dbName=" + dbName + ", dbType=" + dbType
                + ", isNullable=" + isNullable + ", isPrimaryKey="
                + isPrimaryKey + ", isAutoincrement=" + isAutoincrement + "]";
    }
}
