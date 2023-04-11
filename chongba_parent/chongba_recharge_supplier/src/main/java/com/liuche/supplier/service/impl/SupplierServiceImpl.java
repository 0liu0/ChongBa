package com.liuche.supplier.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.chongba.recharge.OrderStatusEnum;
import com.chongba.recharge.RechargeResponse;
import com.chongba.recharge.entity.OrderTrade;
import com.chongba.recharge.mapper.OrderTradeMapper;
import com.liuche.common.entity.Constants;
import com.liuche.common.entity.StatusCode;
import com.liuche.common.entity.order.Result;
import com.liuche.common.recharge.RechargeRequest;
import com.liuche.supplier.config.SupplierConfig;
import com.liuche.supplier.service.SupplierService;
import com.liuche.supplier.service.SupplierTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@Component
public class SupplierServiceImpl implements SupplierService {
    @Autowired
    private SupplierConfig supplierConfig;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    @Lazy
    private SupplierTask supplierTask;

    @Override
    public void recharge(RechargeRequest rechargeRequest) {
        // 判断重试次数
        if (rechargeRequest.getRepeat() >= supplierConfig.getMaxrepeat()) {
            // 结束重试 --------修改订单为失败
            updateTrade(rechargeRequest.getOrderNo(), OrderStatusEnum.FAIL.getCode());
            System.out.println("我要退出了哦");
            return;
        }
        // 进行远程供应商的接口的调用，由于有多个供应商，所以需要判断
        Result<RechargeResponse> result = doDispatchSupplier(rechargeRequest);
        if (result!=null) {
            // 添加重试任务
            rechargeRequest.setErrorCode(StatusCode.ORDER_REQ_FAILED);
            supplierTask.addRetryTask(rechargeRequest);
        }
    }

    private Result<RechargeResponse> doDispatchSupplier(RechargeRequest rechargeRequest) {
        // 根据供应商的编号进行分发
        Result<RechargeResponse> result = null;
        String supply = supplierConfig.getApis().get(rechargeRequest.getSupply());
        rechargeRequest.setRechargeUrl(supply); // 设置供应商接口地址
        if (rechargeRequest.getSupply().equals(Constants.juheapi)) {
            // 如果对应的供应商编号为聚合
            result = doPostJuhe(rechargeRequest);

        } else if (rechargeRequest.getSupply().equals(Constants.jisuapi)) {
            // 如果对应的供应商编号为极速
            result = doPostJisu(rechargeRequest);
        }
        return result;
    }

    private Result<RechargeResponse> doPostJisu(RechargeRequest rechargeRequest) {
        return null;
    }

    private Result<RechargeResponse> doPostJuhe(RechargeRequest rechargeRequest) {
        // 聚合要求传递的是json格式数据
        // 创建并设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 创建请求实体
        HttpEntity<String> httpEntity = new HttpEntity<>(JSON.toJSONString(rechargeRequest), headers);
        // 发送请求
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(rechargeRequest.getRechargeUrl(), httpEntity, String.class);
        // 获得结果
//        String body = responseEntity.getBody();
//        System.out.println(body);

        Result<RechargeResponse> result = JSON.parseObject(responseEntity.getBody(), new TypeReference<Result<RechargeResponse>>() {});
        return result;
    }


    @Autowired
    private OrderTradeMapper orderTradeMapper;
    private void updateTrade(String orderNo, int orderStatus) {
        //修改订单状态
        QueryWrapper<OrderTrade> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_no", orderNo);
        OrderTrade orderTrade = orderTradeMapper.selectOne(queryWrapper);
        if(orderTrade!=null) {
            orderTrade.setOrderStatus(orderStatus);
            orderTradeMapper.updateById(orderTrade);
        }
    }

}
