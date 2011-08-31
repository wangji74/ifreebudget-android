package com.ifreebudget.fm.entity.beans;

public class Constraint {
    Long id;
    int constraintType;
    byte[] constraint;   
    Long scheduleId;
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public int getConstraintType() {
        return constraintType;
    }
    public void setConstraintType(int constraintType) {
        this.constraintType = constraintType;
    }
    public byte[] getConstraint() {
        return constraint;
    }
    public void setConstraint(byte[] constraint) {
        this.constraint = constraint;
    }
    public Long getScheduleId() {
        return scheduleId;
    }
    public void setScheduleId(Long scheduleId) {
        this.scheduleId = scheduleId;
    }
}
