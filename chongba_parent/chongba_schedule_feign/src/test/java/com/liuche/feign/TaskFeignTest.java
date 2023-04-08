package com.liuche.feign;

import com.liuche.TaskFeignApplication;
import com.liuche.common.entity.ResponseMessage;
import com.liuche.common.entity.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TaskFeignApplication.class)
public class TaskFeignTest {
    @Autowired
    private TaskFeign taskFeign;
    @Test
    public void test01() {
        for (int i = 0; i < 4; i++) {
            Task task = new Task();
            task.setTaskType(2003);
            task.setParameters("testFeignClient".getBytes());
            task.setPriority(100);
            task.setExecuteTime(new Date().getTime());
            ResponseMessage response = taskFeign.pushTask(task);
            System.out.println(response);
        }
    }

    @Test
    public void test2(){
        ResponseMessage message = taskFeign.pollTask(100, 2003);
        System.out.println(message);
    }

    @Test
    public void test3(){
        taskFeign.cancelTask(1644704445173780482L);// 从数据库或缓存中查找一个任务id
    }

    @Test
    public void test4() {
        ResponseMessage refresh = taskFeign.refresh();
        System.out.println(refresh);
    }


}
