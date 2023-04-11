package com.liuche.supplier.service.impl;

import com.alibaba.fastjson.JSON;
import com.chongba.utils.ProtostuffUtil;
import com.liuche.common.entity.ResponseMessage;
import com.liuche.common.entity.Task;
import com.liuche.common.enums.TaskTypeEnum;
import com.liuche.common.recharge.RechargeRequest;
import com.liuche.feign.TaskFeign;
import com.liuche.supplier.service.SupplierService;
import com.liuche.supplier.service.SupplierTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Calendar;

@Service
@Slf4j
public class SupplierTaskImpl implements SupplierTask {
    @Autowired
    private TaskFeign taskFeign;
    @Autowired
    private SupplierService supplierService;
    @Override
    public void addRetryTask(RechargeRequest rechargeRequest) {
        // 创建任务对象
        Task task = new Task();
        TaskTypeEnum taskTypeEnum = TaskTypeEnum.getTaskType(rechargeRequest.getErrorCode());
        if (taskTypeEnum != null) {
            task.setTaskType(taskTypeEnum.getTaskType());
        }
        if (taskTypeEnum != null) {
            task.setPriority(taskTypeEnum.getPriority());
        }

        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.MINUTE,0); // 方便测试设为0
        task.setExecuteTime(instance.getTimeInMillis());
        task.setParameters(ProtostuffUtil.serialize(rechargeRequest));
        // 生成日志
        if (taskTypeEnum != null) {
            log.info("addRetryTask {},task={}",taskTypeEnum.getDesc(),task);
        }
        // 添加任务
        taskFeign.pushTask(task);
    }

    @Override
    @Scheduled(fixedRate = 1000)
    public void retryRecharge() {
        // 得到枚举对象
        TaskTypeEnum taskTypeEnum = TaskTypeEnum.ORDER_REQ_FAILED;
        ResponseMessage responseMessage = taskFeign.pollTask(taskTypeEnum.getPriority(), taskTypeEnum.getTaskType());
        if (responseMessage.isFlag()) {
            // 得到消费任务
            if (responseMessage.getData() != null) {
                // 得到任务对象 直接转换对象会报错
                String str = JSON.toJSONString(responseMessage.getData());
                Task task = JSON.parseObject(str, Task.class);
                // 得到任务RechargeRequest对象进行重试
                RechargeRequest request = ProtostuffUtil.deserialize(task.getParameters(), RechargeRequest.class);
                // 重试次数加1
                request.setRepeat(request.getRepeat()+1);
                // 打印日志
                log.info("retryRecharge,{},rechargeRequest={}",taskTypeEnum.getDesc(),request);
                // 进行接口重试
                supplierService.recharge(request);
            }
        }
    }
}
