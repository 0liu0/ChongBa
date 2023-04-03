package com.liuche.shedule;

import com.liuche.common.entity.Task;
import com.liuche.schedule.ScheduleApplication;
import com.liuche.schedule.entity.TaskInfo;
import com.liuche.schedule.mapper.TaskInfoMapper;
import com.liuche.schedule.service.TaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ScheduleApplication.class)
public class ThreadTest {
    @Autowired
    private TaskInfoMapper taskInfoMapper;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ThreadPoolTaskExecutor poolTaskExecutor;

    @Test
    public void test01() {
        for (int i = 0; i < 100; i++) {
            int index = i;
            poolTaskExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    System.out.println("visibleThreadPool test- " + Thread.currentThread().getName());
                }
            });
        }
    }

    @Test
    public void initGroupData() {
        //构造不同分组的任务数据
        for (int i = 0; i < 20; i++) {
            Task task = new Task();
            task.setExecuteTime(System.currentTimeMillis()+50000);
            if (i < 10) {
                task.setTaskType(1001);
                task.setPriority(50);
            } else {
                task.setTaskType(1002);
                task.setPriority(100);
            }
            task.setParameters("testGroup".getBytes());
            taskService.addTask(task);
        }
    }
}
