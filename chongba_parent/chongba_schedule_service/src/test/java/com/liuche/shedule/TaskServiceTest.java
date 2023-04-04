package com.liuche.shedule;

import com.liuche.common.entity.Task;
import com.liuche.common.utils.CacheService;
import com.liuche.schedule.ScheduleApplication;
import com.liuche.schedule.service.TaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ScheduleApplication.class)
public class TaskServiceTest {
    @Autowired
    private TaskService taskService;
    @Autowired
    private CacheService cacheService;

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
        taskService.cancelTask(1642480512582856705L);
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
            task.setTaskType(1002);
            task.setPriority(100);
            task.setExecuteTime(now + 5000 * i);
            task.setParameters("testPoolTask".getBytes());
            taskService.addTask(task);
        }
        // 拉取任务
        while (taskService.size(1002,100) > 0) {
            // 得到一个任务
            Task task = taskService.poll(1002,100);
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

    @Test
    public void testSyncDate() {
        for (int i = 1; i <= 3; i++) {
            Task task = new Task();
            task.setTaskType(250);
            task.setPriority(250);
            task.setExecuteTime(new Date().getTime() + 10000 * i);
            task.setParameters("testPoolTask".getBytes());
            taskService.addTask(task);
        }
    }

    @Test
    public void test04() {
        Set<String> keys = cacheService.keys("future_*");
        for (String key : keys) {
            System.out.println(key+"key");
        }
        Set<String> scan = cacheService.scan("future_*");
        for (String key : scan) {
            System.out.println(key+"scan");
        }
    }

}
