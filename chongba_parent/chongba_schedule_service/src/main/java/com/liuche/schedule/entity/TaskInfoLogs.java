package com.liuche.schedule.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;


/**
 * (TaskinfoLogs)实体类
 *
 * @author makejava
 * @since 2023-04-01 14:43:50
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@TableName("taskinfo_logs") // 实体和表产生映射关系，mybatis-plus
public class TaskInfoLogs implements Serializable {
    private static final long serialVersionUID = 409539090699263187L;
    @TableId(type = IdType.ASSIGN_ID) // 配置映射关系 id专属 自动生成id
    private Long taskId;
    @Version
    private Integer version;
    @TableField
    private Integer status;

    @TableField // 配置映射关系
    private Date executeTime;
    @TableField() // 配置映射关系
    private Integer priority;
    @TableField // 配置映射关系
    private Integer taskType;
    @TableField // 配置映射关系
    private byte[] parameters;

}

