package com.liuche.job;

import com.liuche.common.entity.Constants;
import com.liuche.common.entity.ResponseMessage;
import com.liuche.feign.TaskFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@Slf4j
public class TaskJob {
    @Autowired
    private TaskFeign taskFeign;
    @Autowired
    private SelectJobMaster selectJobMaster;
    @PostConstruct
    public void init() {
        // 选举主节点
        selectJobMaster.selectMaster(Constants.job_leaderPath);
    }
    @Scheduled(cron = "*/1 * * * * ? ")
    public void refresh() {
        // 判断是不是主节点 是的话就执行refresh指令否则不执行
        boolean flag = selectJobMaster.checkMaster(Constants.job_leaderPath);
        if (flag) {
            log.info("job主节点开始进行刷新任务调度");
            ResponseMessage refresh = taskFeign.refresh();
            System.out.println(refresh);
        }else {
            log.info("job从节点备用");
        }

    }
}
