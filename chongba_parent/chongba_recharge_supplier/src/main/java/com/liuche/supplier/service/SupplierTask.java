package com.liuche.supplier.service;

import com.liuche.common.recharge.RechargeRequest;

public interface SupplierTask {
    void addRetryTask(RechargeRequest rechargeRequest);
    void retryRecharge();
}
