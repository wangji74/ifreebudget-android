package com.ifreebudget.fm.entity.beans;

public class TaskEntity implements FManEntity {
    private static final long serialVersionUID = 1L;
    Long id;
    String name;
    Long startTime;
    Long endTime;
    Long businessObjectId;
    String taskType;
    
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
    public Long getStartTime() {
        return startTime;
    }
    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }
    public Long getEndTime() {
        return endTime;
    }
    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }
    public Long getBusinessObjectId() {
        return businessObjectId;
    }
    public void setBusinessObjectId(Long businessObjectId) {
        this.businessObjectId = businessObjectId;
    }
    public String getTaskType() {
        return taskType;
    }
    public void setTaskType(String taskType) {
        this.taskType = taskType;
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
        return new TaskEntityMapper();
    }
    @Override
    public void setPK(Object pk) {
        Long id = (Long) pk;
        setId(id);
    }
}
