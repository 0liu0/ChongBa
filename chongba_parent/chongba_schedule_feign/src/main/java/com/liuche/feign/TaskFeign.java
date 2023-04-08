package com.liuche.feign;

import com.liuche.common.entity.ResponseMessage;
import com.liuche.common.entity.Task;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

@FeignClient(value = "schedule-service",path = "/task")
public interface TaskFeign {
    @PostMapping("/push")
    ResponseMessage pushTask(@RequestBody Task task);
    @GetMapping("/poll/{taskType}/{priority}")
    ResponseMessage pollTask(@PathVariable Integer priority, @PathVariable Integer taskType);
    @GetMapping ("/cancel")
    ResponseMessage cancelTask(@RequestParam("taskId") Long taskId);
    @GetMapping("/refresh")
    ResponseMessage refresh();
}
