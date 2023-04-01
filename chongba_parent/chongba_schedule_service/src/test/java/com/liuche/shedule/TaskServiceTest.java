package com.liuche.shedule;

import com.liuche.entity.Task;
import com.liuche.schedule.ScheduleApplication;
import com.liuche.schedule.service.TaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ScheduleApplication.class)
public class TaskServiceTest {
    @Autowired
    private TaskService taskService;

    @Test
    public void taskAdd() {
        Task task = new Task();
        task.setTaskType(1009);
        task.setPriority(100);
        task.setExecuteTime(new Date().getTime());
        task.setParameters("taskServiceTest".getBytes());
        long taskId = taskService.addTask(task);
        System.out.println("添加成功的任务id："+taskId);
    }

    @Test
    public void cancel() {
        //取消任务
        taskService.cancelTask(1642151312218935298L);
    }
}
