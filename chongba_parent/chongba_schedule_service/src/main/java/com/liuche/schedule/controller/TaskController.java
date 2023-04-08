package com.liuche.schedule.controller;

import com.liuche.common.entity.ResponseMessage;
import com.liuche.common.entity.Task;
import com.liuche.common.exception.ScheduleSystemException;
import com.liuche.common.exception.TaskNotExistException;
import com.liuche.schedule.service.Refresh;
import com.liuche.schedule.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/task")
@RestController
@Slf4j
public class TaskController {
    @Autowired
    private TaskService taskService;
    @Autowired
    private Refresh refresh;

    @GetMapping("/test")
    public ResponseMessage test() {
        return ResponseMessage.ok("你好啊");
    }

    @PostMapping("/push")
    public ResponseMessage pushTask(@RequestBody Task task) {
        log.info("add task {}",task);
        //参数校验
        try {
            Assert.notNull(task.getTaskType(),"任务类型不能为空");
            Assert.notNull(task.getPriority(),"任务优先级不能为空");
            long taskId = taskService.addTask(task);
            return ResponseMessage.ok(taskId);
        } catch (ScheduleSystemException e) {
            log.error("push task exception {}",task);
            return ResponseMessage.error(e.getMessage());
        }
    }
    @GetMapping ("/poll/{taskType}/{priority}")
    public ResponseMessage pollTask(@PathVariable Integer priority, @PathVariable Integer taskType) {
        log.info("poll task taskType:{} priority:{}",taskType,priority);
        try {
            Assert.notNull(priority,"优先值不能为空");
            Assert.notNull(taskType,"任务类型不能为空");
            Task task = taskService.poll(taskType, priority);
            return ResponseMessage.ok(task);
        } catch (TaskNotExistException e) {
            log.info("poll task exception {}",e.getMessage());
            return ResponseMessage.error(e.getMessage());
        }
    }
    @GetMapping ("/cancel")
    public ResponseMessage cancelTask(@RequestParam("taskId") Long taskId) {
        log.info("cancel task {}",taskId);
        try {
            Assert.notNull(taskId,"任务id不能为空");
            boolean flag = taskService.cancelTask(taskId);
            return ResponseMessage.ok(flag);
        } catch (TaskNotExistException e) {
            log.info("cancel task exception {}",e.getMessage());
            return ResponseMessage.error(e.getMessage());
        }
    }
    @GetMapping("/refresh")
    public ResponseMessage refresh() {
        try {
            refresh.refresh();
        } catch (Exception e) {
            log.warn("定时刷新失败！");
            return ResponseMessage.error(e.getMessage());
        }
        return ResponseMessage.ok("刷新成功！");
    }

}
