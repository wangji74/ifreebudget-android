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
    
    public static String ScheduledTaskMapperMappings[] = {
            "ID:INTEGER:true:true:true:id:java.lang.Long", 
            "NAME:VARCHAR(128):true:false:false:Name:java.lang.String",
            "STARTTIME:INTEGER:true:false:false:StartTime:java.lang.Long",
            "ENDTIME:INTEGER:true:false:false:EndTime:java.lang.Long",
            "BUSOBJID:INTEGER:false:false:false:BusinessObjectId:java.lang.Long",
            "TASKTYPE:VARCHAR(64):true:false:false:TaskType:java.lang.String" };
}
