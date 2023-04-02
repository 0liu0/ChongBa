package com.liuche.shedule;

import com.liuche.common.entity.Task;
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
        System.out.println("添加成功的任务id：" + taskId);
    }

    @Test
    public void cancel() {
        //取消任务
        taskService.cancelTask(1642403148926574594L);
    }

    @Test
    public void test03() {
        long time = new Date().getTime();
        System.out.println(time);
    }

    @Test
    public void testPoll() {
        // 添加任务数据
        long now = System.currentTimeMillis();
        for (int i = 0; i < 3; i++) {
            Task task = new Task();
            task.setTaskType(250);
            task.setPriority(250);
            task.setExecuteTime(now + 50000 * i);
            task.setParameters("testPoolTask".getBytes());
            taskService.addTask(task);
        }
        // 拉取任务
        while (taskService.size() > 0) {
            // 得到一个任务
            Task task = taskService.poll();
            // 如果任务为不为null
            if (task != null) {
                System.out.println("成功消费了任务：" + task.getTaskId());
            }
            // 每隔一秒执行一次
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
