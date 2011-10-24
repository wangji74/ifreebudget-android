package com.ifreebudget.fm.entity.beans;

public class ConstraintEntity implements FManEntity {
    private static final long serialVersionUID = 1L;
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
    @Override
    public String getPKColumnName() {
        return "id";
    }
    @Override
    public Object getPK() {
        return getId();
    }
    @Override
    public void setPK(Object pk) {
        setId( (Long) pk);
    }
    @Override
    public TableMapper getTableMapper() {
        return new ConstraintEntityMapper();
    }
}
