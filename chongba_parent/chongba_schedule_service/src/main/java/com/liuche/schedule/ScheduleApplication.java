package com.liuche.schedule;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.liuche.schedule.config.MyThreadPool;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@SpringBootApplication
@MapperScan("com.liuche.schedule.mapper")
@ComponentScan({"com.liuche.common.utils","com.liuche.schedule.service","com.liuche.schedule.config","com.liuche.schedule.controller"})
@EnableScheduling
@EnableDiscoveryClient // 配置nacos
public class ScheduleApplication {
    public static void main(String[] args) {
        SpringApplication.run(ScheduleApplication.class, args);
    }

    /*
        mybatis-plus乐观锁的支持
    * */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }

    @Bean
    public ThreadPoolTaskExecutor visibleThreadPool() {
        MyThreadPool pool = new MyThreadPool();
        pool.setCorePoolSize(10); // 核心线程数
        pool.setMaxPoolSize(1000); // 最大线程数
        pool.setKeepAliveSeconds(60); // 线程执行完任务后的存活时间
        pool.setQueueCapacity(1000); // 缓冲等待队列的长度
        pool.setThreadNamePrefix("MyVisibleThread-"); // 线程名字的前缀
        pool.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy()); // 当线程池满了之后执行的策略
        pool.initialize(); // 线程初始化

        return pool;
    }
}