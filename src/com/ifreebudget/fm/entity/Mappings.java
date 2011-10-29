package com.ifreebudget.fm.entity;

public class Mappings {

    /*DBName:DBType:NotNull:PrimaryKey:AutoIncrement:javaName:javaType*/
    
    public static final int DNIDX = 0;
    public static final int DTIDX = 1;
    public static final int NNIDX = 2;
    public static final int PKIDX = 3;
    public static final int ACIDX = 4;
    public static final int JNIDX = 5;
    public static final int JTIDX = 6;
    public static final int EXIDX = 7;
    
    public static String ScheduledTaskMapperMappings[] = {
            "ID:INTEGER:true:true:true:Id:java.lang.Long", 
            "NAME:VARCHAR(128):true:false:false:Name:java.lang.String",
            "STARTTIME:INTEGER:true:false:false:StartTime:java.lang.Long",
            "ENDTIME:INTEGER:true:false:false:EndTime:java.lang.Long",
            "BUSOBJID:INTEGER:false:false:false:BusinessObjectId:java.lang.Long",
            "TASKTYPE:VARCHAR(64):true:false:false:TaskType:java.lang.String" };

    public static String ScheduleMapperMappings[] = {
        "ID:INTEGER:true:true:true:Id:java.lang.Long", 
        "SCHTASKID:INTEGER:true:false:false:ScheduledTaskId:java.lang.Long",
        "NEXTRT:INTEGER:true:false:false:NextRunTime:java.lang.Long",
        "LASTRT:INTEGER:true:false:false:LastRunTime:java.lang.Long",
        "REPTYPE:INTEGER:true:false:false:RepeatType:java.lang.Integer",
        "STEP:INTEGER:true:false:false:Step:java.lang.Integer" };    

    public static String ConstraintMapperMappings[] = {
        "ID:INTEGER:true:true:true:Id:java.lang.Long", 
        "CONSTRTYPE:INTEGER:true:false:false:ConstraintType:java.lang.Integer",
        "CONSTR:BLOB:true:false:false:Constraint:[Ljava.lang.Byte;",
        "SCHEDID:INTEGER:true:false:false:ScheduleId:java.lang.Long" };

    public static String TaskNotificationMapperMappings[] = {
        "ID:INTEGER:true:true:true:Id:java.lang.Long", 
        "TASKID:INTEGER:true:false:false:TaskId:java.lang.Long",
        "NOTIFTIME:INTEGER:true:false:false:Timestamp:java.lang.Long"};    
}
