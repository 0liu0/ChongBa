package com.liuche.shedule;

import com.liuche.schedule.ScheduleApplication;
import com.liuche.schedule.entity.TaskInfoLogs;
import com.liuche.schedule.mapper.TaskInfoLogsMapper;
import com.liuche.schedule.mapper.TaskInfoMapper;
import com.liuche.schedule.entity.TaskInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ScheduleApplication.class)
public class TaskInfoMapperTest {
    @Autowired
    private TaskInfoMapper taskInfoMapper;

    @Autowired
    private TaskInfoLogsMapper taskInfoLogsMapper;

    @Test
    public void test01() {
        TaskInfo entity = new TaskInfo();
        entity.setExecuteTime(new Date());
        entity.setPriority(2);
        entity.setTaskType(1002);
        entity.setParameters("test".getBytes());
        int insert = taskInfoMapper.insert(entity);
        System.out.println("insert:"+insert);
        System.out.println("自动生成的task_id：" + entity.getTaskId());
    }
    @Test
    public void test02() {
        TaskInfoLogs taskInfoLogsEntity = new TaskInfoLogs();
        taskInfoLogsEntity.setTaskType(1003);
        taskInfoLogsEntity.setPriority(3);
        taskInfoLogsEntity.setParameters("log".getBytes());
        taskInfoLogsEntity.setExecuteTime(new Date());
        taskInfoLogsEntity.setVersion(1);
        taskInfoLogsEntity.setStatus(0);
        taskInfoLogsMapper.insert(taskInfoLogsEntity);
        taskInfoLogsEntity = taskInfoLogsMapper.selectById(taskInfoLogsEntity.getTaskId());
        System.out.println("数据插入:"+taskInfoLogsEntity);
        taskInfoLogsEntity.setStatus(1);
        taskInfoLogsMapper.updateById(taskInfoLogsEntity);
        taskInfoLogsEntity = taskInfoLogsMapper.selectById(taskInfoLogsEntity.getTaskId());
        System.out.println("第一次更新后查询:"+taskInfoLogsEntity);
        taskInfoLogsEntity.setPriority(5);
        taskInfoLogsMapper.updateById(taskInfoLogsEntity);
        taskInfoLogsEntity = taskInfoLogsMapper.selectById(taskInfoLogsEntity.getTaskId());
        System.out.println("第二次更新后查询:"+taskInfoLogsEntity);
    }
}
