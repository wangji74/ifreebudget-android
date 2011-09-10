package com.ifreebudget.fm.entity.beans;

public class ScheduleEntity implements FManEntity {
    private static final long serialVersionUID = 1L;
    Long id;
    Long scheduledTaskId;
    Long nextRunTime;
    Long lastRunTime;
    int repeatType;
    int step;
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }    
    public Long getScheduledTaskId() {
        return scheduledTaskId;
    }
    public void setScheduledTaskId(Long scheduledTaskId) {
        this.scheduledTaskId = scheduledTaskId;
    }
    public Long getNextRunTime() {
        return nextRunTime;
    }
    public void setNextRunTime(Long nextRunTime) {
        this.nextRunTime = nextRunTime;
    }
    public Long getLastRunTime() {
        return lastRunTime;
    }
    public void setLastRunTime(Long lastRunTime) {
        this.lastRunTime = lastRunTime;
    }
    public int getRepeatType() {
        return repeatType;
    }
    public void setRepeatType(int repeatType) {
        this.repeatType = repeatType;
    }
    public int getStep() {
        return step;
    }
    public void setStep(int step) {
        this.step = step;
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
        setId((Long) pk);
    }
    @Override
    public TableMapper getTableMapper() {
        return new ScheduleMapper();
    }          
}
