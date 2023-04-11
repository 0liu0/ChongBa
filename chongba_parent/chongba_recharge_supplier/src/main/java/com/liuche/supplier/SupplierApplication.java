package com.liuche.supplier;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;

@SpringBootApplication
@MapperScan("com.liuche.recharge.mapper")
@EnableFeignClients(basePackages = {"com.liuche.feign"}) // 扫描feign接口所在的包
//@ComponentScan({"com.liuche.cache","com.liuche.supplier"})
public class SupplierApplication {
    public static void main(String[] args) {
        SpringApplication.run(SupplierApplication.class,args);
    }
}

