package com.ifreebudget.fm.entity.beans;

public class Budget implements FManEntity {
    private static final long serialVersionUID = 1L;
    public final static int WEEKLY = 1;
    public final static int MONTHLY = 3;
    public final static int BIWEEKLY = 2;

    private Long id;
    private String name;
    private int type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public static int getTypeFromString(String type) {
        if (type.equals("Weekly")) {
            return WEEKLY;
        }
        else if (type.equals("Bi-weekly")) {
            return BIWEEKLY;
        }
        return MONTHLY;
    }

    public static String getTypeAsString(int type) {
        if (type == WEEKLY) {
            return "Weekly";
        }
        else if (type == BIWEEKLY) {
            return "Bi-weekly";
        }
        return "Monthly";
    }

    public String toString() {
        return name;
    }

    @Override
    public String getPKColumnName() {
        return "id";
    }

    @Override
    public Object getPK() {
        return id;
    }

    @Override
    public void setPK(Object pk) {
        setId((Long) pk);
    }

    @Override
    public TableMapper getTableMapper() {
        return new BudgetMapper();
    }
}
