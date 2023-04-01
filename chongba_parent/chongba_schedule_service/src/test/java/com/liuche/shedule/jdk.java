package com.liuche.shedule;


import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class jdk {
    // 创建timer
    private static final Timer timer = new Timer();

    public static void main(String[] args) {
        timerTest06();
    }

    private static void timerTest01() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println(System.currentTimeMillis() / 1000 + "执行了任务");
            }
        }, 1000L);
        System.out.println(System.currentTimeMillis() / 1000);
    }

    public static void timerTest02() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println(System.currentTimeMillis() / 1000 + "执行了任务");
            }
        }, new Date(System.currentTimeMillis() + 1000L));
        System.out.println(System.currentTimeMillis() / 1000);
    }

    public static void timerTest03() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println(System.currentTimeMillis() / 1000 + "，执行了任务");
            }
        }, new Date(System.currentTimeMillis() + 1000L), 2000L);
        System.out.println("当前时间：" + System.currentTimeMillis() / 1000);
    }
    public static void timerTest04() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                System.out.println(System.currentTimeMillis() / 1000 + "，执行了任务");
            }
        },new Date(System.currentTimeMillis()-5000L),1000L);
        System.out.println("当前时间：" + System.currentTimeMillis() / 1000);
    }
    public static void timerTest05() {
        for (int i = 0; i < 100; i++) {
            int index = i;
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    System.out.println("当前任务：" + index);
                    if (index == 20) throw new RuntimeException();
                }
            },1000L);
        }
    }

    public static void timerTest06() {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(5);
        for (int i = 0; i <100; i++) {
            int index = i;
            executorService.schedule(new TimerTask() {
                @Override
                public void run() {
                    System.out.println("当前任务：" + index);
                    if (index == 20) throw new RuntimeException();
                }
            },1, TimeUnit.SECONDS);
        }
    }
}
