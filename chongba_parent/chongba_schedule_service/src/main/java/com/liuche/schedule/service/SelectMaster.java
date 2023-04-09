package com.liuche.schedule.service;

import com.liuche.schedule.config.SystemParams;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class SelectMaster {
    @Autowired
    private SystemParams systemParams;
    //可以放很多节点
    Map<String, Boolean> masterMap = new HashMap<>();

    public void selectMaster(String leaderPath) {
        CuratorFramework client = CuratorFrameworkFactory.builder().
                connectString(systemParams.getSelectMasterZookeeper())
                .sessionTimeoutMs(5000) //超时时间
                .retryPolicy(new ExponentialBackoffRetry(1000, 3)) //连接不上重试三次
                .build();
        client.start();
//争抢注册节点
        @SuppressWarnings("resource")
        LeaderSelector selector = new LeaderSelector(client, leaderPath,
                new LeaderSelectorListenerAdapter() {
                    @Override
                    public void takeLeadership(CuratorFramework client) throws Exception {
//如果争抢到当前注册节点
                        masterMap.put(leaderPath, true);
                        while (true) {
//抢占当前节点
                            TimeUnit.SECONDS.sleep(3);
                        }
                    }
                });
        masterMap.put(leaderPath, false);
        selector.autoRequeue();
        selector.start();
    }

    public boolean checkMaster(String leaderPath) {
        Boolean isMaster = masterMap.get(leaderPath);
        return isMaster == null ? false : isMaster;
    }
}
