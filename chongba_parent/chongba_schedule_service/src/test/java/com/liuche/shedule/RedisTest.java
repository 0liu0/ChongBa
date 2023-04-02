package com.liuche.shedule;

import com.liuche.common.utils.CacheService;
import com.liuche.schedule.ScheduleApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ScheduleApplication.class)

public class RedisTest {
    @Autowired
    private CacheService cacheService;
    @Test
    public void test01() {
        cacheService.set("myName","liuche");
    }
}
