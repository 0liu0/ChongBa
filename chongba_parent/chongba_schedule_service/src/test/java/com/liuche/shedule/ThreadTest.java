package com.liuche.shedule;

import com.liuche.schedule.ScheduleApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ScheduleApplication.class)
public class ThreadTest {

    @Autowired
    private ThreadPoolTaskExecutor poolTaskExecutor;

    @Test
    public void test01() {
        for (int i = 0; i < 100; i++) {
            int index = i;
            poolTaskExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    System.out.println("visibleThreadPool test- "+Thread.currentThread().getName());
                }
            });
        }
    }
}
