package com.liuche.job;

import lombok.Data;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
@Data
@Component
@ConfigurationProperties(prefix = "chongba")
public class SelectJobMaster {
    private String selectMasterZookeeper;
    //可以放很多节点
    Map<String, Boolean> masterMap = new HashMap<>();

    /**
     * 选主
     *
     * @param leaderPath zookeeper目录节点
     */
    public void selectMaster(String leaderPath) {
        CuratorFramework client = CuratorFrameworkFactory.builder().
                connectString(selectMasterZookeeper)
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
