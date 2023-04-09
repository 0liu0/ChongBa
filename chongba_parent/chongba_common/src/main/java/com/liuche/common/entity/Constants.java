package com.liuche.common.entity;

public class Constants {
    //task状态
    public static final int SCHEDULED=0; //初始化状态
    public static final int EXECUTED=1; //已执行状态
    public static final int CANCELLED=2; //已取消状态
    public static String DBCACHE="db_cache"; // redis缓存的key名
    public static String FUTURE="future_"; // 未来执行的任务
    public static String TOPIC="topic_"; // 当前需执行的任务
    public static final String schedule_leaderPath="/chongba/schedule_master";
    public static final String job_leaderPath="/chongba/job_master";



}
