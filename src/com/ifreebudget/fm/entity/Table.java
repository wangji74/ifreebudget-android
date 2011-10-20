package com.ifreebudget.fm.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Table {

    private String tableName;
    private List<Field> fields;

    public Table(String tableName) {
        this.tableName = tableName;
        fields = new ArrayList<Field>();
    }

    public List<Field> getPrimaryKeys() {
        List<Field> ret = new ArrayList<Field>();

        for (Field f : fields) {
            if (f.isPrimaryKey()) {
                ret.add(f);
            }
        }

        return ret;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public String getRetrieveSql() {
        return "SELECT * FROM " + tableName;
    }

    public String getCreateSql() {
        StringBuilder ret = new StringBuilder("CREATE TABLE ");
        ret.append(tableName);

        int sz = fields.size();

        ret.append(Field.OPENB);

        for (int i = 0; i < sz; i++) {
            Field f = fields.get(i);
            ret.append(f.getSql());
            if (i < sz - 1) {
                ret.append(",");
            }
        }
        ret.append(Field.CLOSEB);
        return ret.toString();
    }

    public String getInsertSql() {
        StringBuilder ret = new StringBuilder("INSERT INTO");
        ret.append(Field.SPACE);
        ret.append(tableName);
        ret.append(Field.SPACE);

        int sz = fields.size();

        ret.append(Field.OPENB);

        for (int i = 0; i < sz; i++) {
            Field f = fields.get(i);
            ret.append(f.getDbName());
            if (i < sz - 1) {
                ret.append(",");
            }
        }

        ret.append(Field.CLOSEB);
        ret.append(Field.SPACE);
        ret.append(Field.VALUES);
        ret.append(Field.SPACE);
        ret.append(Field.OPENB);

        for (int i = 0; i < sz; i++) {
            ret.append("?");
            if (i < sz - 1) {
                ret.append(",");
            }
        }

        ret.append(Field.CLOSEB);

        return ret.toString();
    }

    public String getUpdateSql(Set<String> fieldsToUpdate) {
        if(fieldsToUpdate == null || fieldsToUpdate.size() == 0) {
            return null;
        }
        StringBuilder ret = new StringBuilder("UPDATE");
        ret.append(Field.SPACE);
        ret.append(tableName);
        ret.append(Field.SPACE);

        ret.append("SET");

        ret.append(Field.SPACE);

        int sz = fields.size();
        Iterator<String> it = fieldsToUpdate.iterator();
        int count = 0;
        while (it.hasNext()) {
            String toCheck = "get" + it.next();
            for (int i = 0; i < sz; i++) {
                Field f = fields.get(i);
                if (toCheck.equals(f.getAccessorName())) {
                    if(count > 0) {
                        ret.append(Field.COMMA);                        
                    }
                    ret.append(f.getDbName());
                    ret.append(Field.EQ);
                    ret.append(Field.Q);
                    count++;
                }
            }
        }

        ret.append(Field.SPACE);
        ret.append("WHERE");
        ret.append(Field.SPACE);
        count = 0;
        List<Field> pks = getPrimaryKeys();
        for(Field f : pks) {
            if(count > 0) {
                ret.append(Field.COMMA);
            }
            ret.append(f.getDbName());
            ret.append(Field.EQ);
            ret.append(Field.Q);
            ret.append(Field.SPACE);
        }
        return ret.toString();
    }
}
