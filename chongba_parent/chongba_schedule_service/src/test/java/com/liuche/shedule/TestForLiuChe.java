package com.liuche.shedule;

import com.alibaba.fastjson.JSON;
import com.liuche.common.entity.Task;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TestForLiuChe {
    static ThreadPoolExecutor executor = new ThreadPoolExecutor(3, 5, 2, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    public static void main(String[] args) throws InterruptedException {
//        act();
//        act();
//        Calendar instance = Calendar.getInstance();
//        instance.add(Calendar.MINUTE,2);
//        System.out.println(instance.getTime());
        Task task = new Task();
        task.setTaskType(1001);
        task.setPriority(1);
        task.setExecuteTime(new Date().getTime());
        System.out.println(JSON.toJSONString(task));
    }

    private static void act() throws InterruptedException {
        System.out.println("我在执行中:" + new Date());
        Thread.sleep(2000);
        executor.execute(() -> {
            try {
                Thread.sleep(3000);
                System.out.println("我执行完了");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
