package com.liuche.supplier.service;

import com.liuche.common.recharge.RechargeRequest;

public interface SupplierService {
    /**
     * 对接供应商下单
     * @param rechargeRequest
     */
    void recharge(RechargeRequest rechargeRequest);
}

