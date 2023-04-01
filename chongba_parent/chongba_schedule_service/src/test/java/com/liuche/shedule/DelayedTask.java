package com.liuche.shedule;

import java.util.Calendar;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayedTask implements Delayed {
    private final int executeTime;

    public DelayedTask(int delay) { // 计算出执行时间executeTime
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, delay);
        this.executeTime = (int) (calendar.getTimeInMillis() / 1000);
    }

    @Override
    public long getDelay(TimeUnit unit) { // 得到延迟的时间
        return executeTime - (int) (System.currentTimeMillis() / 1000);
    }

    @Override
    public int compareTo(Delayed o) { // 比较排序延迟时间
        long l = this.getDelay(TimeUnit.SECONDS) - o.getDelay(TimeUnit.SECONDS);
        return l == 0 ? 0 : (l < 0 ? -1 : 1);
     }

    public static void main(String[] args) {
        DelayQueue<DelayedTask> queue = new DelayQueue<>();
        // 向队列添加任务
        queue.add(new DelayedTask(5));
        queue.add(new DelayedTask(10));
        queue.add(new DelayedTask(15));
        // 消费任务
        System.out.println(System.currentTimeMillis()/1000 + "开始消费任务");
        while (queue.size() != 0){
            DelayedTask poll = queue.poll();
            if (poll != null) {
                System.out.println(System.currentTimeMillis()/1000 + "消费了任务");
            }
            // 每隔一秒钟消费一次
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
