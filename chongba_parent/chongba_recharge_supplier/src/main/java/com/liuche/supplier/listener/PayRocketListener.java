package com.liuche.supplier.listener;

import com.liuche.common.recharge.RechargeRequest;
import com.liuche.supplier.service.SupplierService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RocketMQMessageListener(topic = "pay",consumerGroup = "order-paid-consumer")
public class PayRocketListener implements RocketMQListener<RechargeRequest> {
    @Autowired
    private SupplierService supplierService;
    // 监听消息
    @Override
    public void onMessage(RechargeRequest rechargeRequest) {
        log.info("PayRocketListener 监听到了消息，{}",rechargeRequest);
        supplierService.recharge(rechargeRequest);
    }
}
