package com.liuche.supplier.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "supplier")
public class SupplierConfig {
    private Map<String,String> apis;
}
