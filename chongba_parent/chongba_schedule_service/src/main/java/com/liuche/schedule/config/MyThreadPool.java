package com.liuche.schedule.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

@Slf4j
public class MyThreadPool extends ThreadPoolTaskExecutor {

    private static final long serialVersionUID = 8328139107589627048L;

    private void logs(String msg) {
        /**
         * - getThreadNamePrefix：线程池内线程名称前缀，方便定位
         - getTaskCount：线程池已经执行的和未执行的任务总数；
         - getCompletedTaskCount：线程池已完成的任务数量，该值小于等于taskCount；
         - getActiveCount：当前线程池中正在执行任务的线程数量。
         - queueSize：缓冲队列大小。
         */
        String prefix = this.getThreadNamePrefix();
        long taskCount = this.getThreadPoolExecutor().getTaskCount();
        long completedTaskCount = this.getThreadPoolExecutor().getCompletedTaskCount();
        int activeCount = this.getThreadPoolExecutor().getActiveCount();
        int queueSize = this.getThreadPoolExecutor().getQueue().size();
//        log.info("prefix={},info={},taskCount={},completedCount={},activeCount={},queueSize={}",prefix,
//                msg,taskCount,completedTaskCount,activeCount,queueSize);

    }

    @Override
    public void execute(Runnable task) {
        super.execute(task);
        logs("do execute");
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        Future<T> submit = super.submit(task);
        logs("do submit");
        return submit;
    }
}
