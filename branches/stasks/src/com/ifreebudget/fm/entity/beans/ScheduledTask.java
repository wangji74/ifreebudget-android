package com.ifreebudget.fm.entity.beans;

public class ScheduledTask implements FManEntity {
    long id;
    String name;
    long startTime;
    long endTime;
    long businessObjectId;
    String taskType;

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public long getStartTime() {
        return startTime;
    }
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    public long getEndTime() {
        return endTime;
    }
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
    public long getBusinessObjectId() {
        return businessObjectId;
    }
    public void setBusinessObjectId(long businessObjectId) {
        this.businessObjectId = businessObjectId;
    }
    public String getTaskType() {
        return taskType;
    }
    public void setTaskType(String type) {
        this.taskType = type;
    }
    @Override
    public Object getPK() {
        return getId();
    }
    @Override
    public String getPKColumnName() {
        return "id";
    }
    @Override
    public TableMapper getTableMapper() {
        return new ScheduledTaskMapper();
    }
    @Override
    public void setPK(Object pk) {
        Long id = (Long) pk;
        setId(id);
    }
}
