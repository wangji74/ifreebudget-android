package com.ifreebudget.fm.entity;

import java.util.List;

public class Table {
    
    private String tableName;
    private List<Field> fields;
    
    public Table(String tableName) {
        this.tableName = tableName;
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
        StringBuilder ret = new StringBuilder();
        
        int sz = fields.size();
        
        ret.append(Field.OPENB);
        
        for(int i = 0; i < sz; i++) {
            Field f = fields.get(i);
            ret.append(f.getSql());
            if(i < sz - 1) {
                ret.append(",");
            }
        }   
        ret.append(Field.CLOSEB);
        return ret.toString();
    }
    
    public String getInsertSql() {
        StringBuilder ret = new StringBuilder();
        
        int sz = fields.size();
        
        
        ret.append(Field.OPENB);
        
        for(int i = 0; i < sz; i++) {
            Field f = fields.get(i);
            ret.append(f.getDbName());
            if(i < sz - 1) {
                ret.append(",");
            }
        }        
        
        ret.append(Field.CLOSEB);
        return ret.toString();
    }
    
}
