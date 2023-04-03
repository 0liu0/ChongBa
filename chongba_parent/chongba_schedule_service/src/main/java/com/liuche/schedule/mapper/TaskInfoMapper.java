package com.liuche.schedule.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liuche.schedule.entity.TaskInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Date;
import java.util.List;

public interface TaskInfoMapper extends BaseMapper<TaskInfo> {
    @Select("select * from taskinfo")
    public List<TaskInfo> selectAll();

    @Select("select * from taskinfo where task_type = #{task_type} and priority = #{priority}")
    List<TaskInfo> queryAll(@Param("task_type") int type, @Param("priority") int priority);

    @Select("select * from taskinfo where task_type = #{task_type} and priority = #{priority} and execute_time <=#{futureTime}")
    List<TaskInfo> queryFuture(@Param("task_type")int taskType,  @Param("priority")int priority, @Param("futureTime") Date futureTime);
}
