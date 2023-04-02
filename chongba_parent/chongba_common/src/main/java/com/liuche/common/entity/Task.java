package com.liuche.common.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class Task implements Serializable {

    private static final long serialVersionUID = 4559658454544407544L;
    private Long taskId;
    //类型
    private Integer taskType;
    // 优先级
    private Integer priority;
    // 执行id
    private long  executeTime;
    // task参数
    private byte[] parameters;
}
