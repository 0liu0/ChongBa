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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;


@Slf4j
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
            return;
        }
        // 进行远程供应商的接口的调用，由于有多个供应商，所以需要判断
        Result<RechargeResponse> result = doDispatchSupplier(rechargeRequest);
        // 根据供应商返回的错误信息判断应该添加哪种类型的ErrorCode
        if (result!=null) {
            if (result.getCode()==StatusCode.ORDER_REQ_FAILED) {
                // 添加重试任务
                rechargeRequest.setErrorCode(StatusCode.ORDER_REQ_FAILED);
                supplierTask.addRetryTask(rechargeRequest);
            } else if (result.getCode()==StatusCode.BALANCE_NOT_ENOUGH) {
                // 添加轮询任务
                rechargeRequest.setErrorCode(StatusCode.BALANCE_NOT_ENOUGH);
                // 重置重复次数
                rechargeRequest.setRepeat(0);
                // 修改供应商（暂时用这种方法，实际开发中不可能只有一个供应商）
                rechargeRequest.setSupply(Constants.jisuapi);
                supplierTask.addRetryTask(rechargeRequest);
            }
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
        log.info("doPostJisu,{}",rechargeRequest);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        //设置表单参数
        MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
        map.add("mobile",rechargeRequest.getMobile());
        map.add("amount",rechargeRequest.getPamt()+"");
        map.add("outorderNo", rechargeRequest.getOrderNo());
        map.add("repeat", ""+rechargeRequest.getRepeat());
        //模拟请求失败
        map.add("req_status", ""+StatusCode.ERROR);
        HttpEntity<MultiValueMap<String, String>> requestEntity = new
                HttpEntity<MultiValueMap<String, String>>(map, headers);
        ResponseEntity<String> responseEntity =
                restTemplate.postForEntity(rechargeRequest.getRechargeUrl(), requestEntity , String.class);
        //转换成统一对象
        Result<RechargeResponse> result= JSON.parseObject(responseEntity.getBody(), new
                TypeReference<Result<RechargeResponse>>(){});
        return result;
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
