package com.liuche.job;

import com.liuche.common.entity.ResponseMessage;
import com.liuche.feign.TaskFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TaskJob {
    @Autowired
    private TaskFeign taskFeign;
    @Scheduled(cron = "*/1 * * * * ? ")
    public void refresh() {
        ResponseMessage refresh = taskFeign.refresh();
        System.out.println(refresh);
    }
}
