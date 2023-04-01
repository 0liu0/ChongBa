package com.liuche.shedule;

import com.liuche.schedule.ScheduleApplication;
import com.liuche.schedule.mapper.TaskInfoMapper;
import com.liuche.schedule.pojo.TaskInfoEntity;
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

    @Test
    public void test01() {
        TaskInfoEntity entity = new TaskInfoEntity();
        entity.setExecuteTime(new Date());
        entity.setPriority(2);
        entity.setTaskType(1002);
        entity.setParameters("test".getBytes());
        int insert = taskInfoMapper.insert(entity);
        System.out.println("insert:"+insert);
        System.out.println("自动生成的task_id：" + entity.getTaskId());
    }
}
