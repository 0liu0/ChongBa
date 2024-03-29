package com.liuche.schedule.service;

import com.liuche.common.entity.Constants;
import com.liuche.common.utils.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class Refresh {
    @Autowired
    private ThreadPoolTaskExecutor poolTaskExecutor;
    @Autowired
    private CacheService cacheService;

//    @Scheduled(cron = "*/1 * * * * ? ")
    public void refresh() {
        poolTaskExecutor.execute(() -> {
            // 得到所有可以执行的定时任务
            // 1.得到所有前缀具有Constants.FUTURE + "*"的key
            String str = Constants.FUTURE + "*";
            Set<String> keys = cacheService.scan(str);
            System.out.println(System.currentTimeMillis() / 1000 + "：执行了定时任务");
            if (keys.size() == 0) return;
            // 2.遍历keys集合，得到所有可以执行的定时任务
            for (String key : keys) {
                // 使用tasks装所有可执行的task
                Set<String> tasks = cacheService.zRangeByScore(key, 0, System.currentTimeMillis());
                String topicKey = Constants.TOPIC + key.split(Constants.FUTURE)[1];
                // 3.push到指定的list集合中
                if (!tasks.isEmpty()) {
//                for (String task : tasks) {
//                    cacheService.lLeftPush(topicKey,task);
//                    cacheService.zRemove(key,task);
//                }
                    cacheService.refreshWithPipeline(key, topicKey, tasks);
                    System.out.println("成功的将" + key + "定时刷新到" + topicKey);
                }
            }
        });


//        // 得到所有可以执行的定时任务
//        // 1.得到所有前缀具有Constants.FUTURE + "*"的key
//        String str = Constants.FUTURE + "*";
//        Set<String> keys = cacheService.scan(str);
//        if (keys.size()==0) return;
//        System.out.println(System.currentTimeMillis()/1000+"：执行了定时任务");
//        // 2.遍历keys集合，得到所有可以执行的定时任务
//        for (String key : keys) {
//            // 使用tasks装所有可执行的task
//            Set<String> tasks = cacheService.zRangeByScore(key, 0, System.currentTimeMillis());
//            String topicKey =Constants.TOPIC+key.split(Constants.FUTURE)[1];
//            // 3.push到指定的list集合中
//            if (!tasks.isEmpty()) {
////                for (String task : tasks) {
////                    cacheService.lLeftPush(topicKey,task);
////                    cacheService.zRemove(key,task);
////                }
//                cacheService.refreshWithPipeline(key,topicKey,tasks);
//                System.out.println("成功的将"+key+"定时刷新到"+topicKey);
//            }
//        }
    }
}
