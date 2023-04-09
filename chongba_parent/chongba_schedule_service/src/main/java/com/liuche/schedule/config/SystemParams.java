package com.liuche.schedule.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
@Data
@Component
//@RefreshScope
@ConfigurationProperties(prefix = "chongba")
public class SystemParams {
    private int preLoad;
    private String selectMasterZookeeper;
}
