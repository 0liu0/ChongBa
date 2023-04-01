package com.liuche.schedule.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor

@TableName("taskinfo") // 实体和表产生映射关系，mybatis-plus
/*
    什么情况下休要实现序列化？
    1：当你想把的内存中的对象状态保存到一个文件中或者数据库中时候；
    2：当你想用套接字在网络上传送对象的时候；
    3：当你想通过RMI传输对象的时候；
    Java 提供了一种对象序列化的机制，该机制中，一个对象可以被表示为一个字节序列，该字节序列包括该对象的
    数据、有关对象的类型的信息和存储在对象中数据的类型。将序列化对象写入文件之后，可以从文件中读取出来，并
    且对它进行反序列化
* */
public class TaskInfoEntity implements Serializable {
    private static final long serialVersionUID = 7239712854365348931L;
    @TableId(type = IdType.ASSIGN_ID) // 配置映射关系 id专属 自动生成id
    private Long taskId;
    @TableField // 配置映射关系
    private Date executeTime;
    @TableField() // 配置映射关系
    private Integer priority;
    @TableField // 配置映射关系
    private Integer taskType;
    @TableField // 配置映射关系
    private byte[] parameters;
}
