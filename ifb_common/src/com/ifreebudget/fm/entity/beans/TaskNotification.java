package com.ifreebudget.fm.entity.beans;

public class TaskNotification implements FManEntity {
    private static final long serialVersionUID = 1L;
    Long id;
    Long taskId;
    Long timestamp;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
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
        return new TaskNotificationMapper();
    }
}
