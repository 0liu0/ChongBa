package com.liuche.schedule.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.liuche.common.entity.Constants;
import com.liuche.common.entity.Task;
import com.liuche.common.exception.ScheduleSystemException;
import com.liuche.common.exception.TaskNotExistException;
import com.liuche.common.utils.CacheService;
import com.liuche.schedule.entity.TaskInfo;
import com.liuche.schedule.entity.TaskInfoLogs;
import com.liuche.schedule.mapper.TaskInfoLogsMapper;
import com.liuche.schedule.mapper.TaskInfoMapper;
import com.liuche.schedule.service.TaskService;
import com.liuche.schedule.utils.CopyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@Slf4j
public class TaskServiceImpl implements TaskService {
    @Autowired
    private TaskInfoMapper taskInfoMapper;
    @Autowired
    private TaskInfoLogsMapper taskInfoLogsMapper;

    @Autowired
    private CacheService cacheService;

    @Override
    @Transactional // 添加事务
    public long addTask(Task task) throws ScheduleSystemException {
        /*
            向任务表中添加数据
            向任务日志表中添加数据
            将延时任务写入缓存
        * */
        boolean flag = saveTaskInDB(task);
        if (flag) {
            saveTaskInCache(task);
        }
        return task.getTaskId();
    }

    private void saveTaskInCache(Task task) {
        cacheService.zAdd(Constants.DBCACHE, JSON.toJSONString(task), task.getExecuteTime());
    }

    public boolean saveTaskInDB(Task task) {
        try {
            // 未执行任务入库
            TaskInfo taskInfo = CopyUtil.copy(task, TaskInfo.class);
            taskInfo.setExecuteTime(new Date(task.getExecuteTime())); // 因为Task的setExecuteTime是long型的而TaskInfo是data型的copy不了
            taskInfoMapper.insert(taskInfo);

            // 设置主键id
            task.setTaskId(taskInfo.getTaskId());
            // 记录任务日志
            TaskInfoLogs infoLogs = CopyUtil.copy(task, TaskInfoLogs.class);
            infoLogs.setExecuteTime(new Date(task.getExecuteTime()));
            infoLogs.setVersion(1);
            infoLogs.setStatus(Constants.SCHEDULED);
            taskInfoLogsMapper.insert(infoLogs);
        } catch (Exception e) {
            // 日志记录
            log.warn("add task exception taskid={}", task.getTaskId());
            throw new ScheduleSystemException(e.getMessage());
        }
        return true;
    }

    @Override
    @Transactional
    public boolean cancelTask(long taskId) throws TaskNotExistException {
        /*
            根据任务id更新数据库Task updateDb(long taskId,int status)：删除任务表数据，更新任务日志表状态为已取消
            更新完成后返回任务对象Task，从redis中删除任务数据 removeTaskFromCache(Task task)
        * */
        try {
            Task task = updateDB(taskId, Constants.CANCELLED);
            removeTaskFromCache(task);
        } catch (Exception e) {
            log.warn("task cancel exception taskid={}", taskId);
            throw new TaskNotExistException(e);
        }
        return true;
    }

    private Task updateDB(long taskId, int status) {
        Task task = null;
        try {
            // 删除数据库中taskinfo中的Task
            taskInfoMapper.deleteById(taskId);
            // 修改taskinfo_logs数据剧中的STATUS状态信息
            TaskInfoLogs taskLog = taskInfoLogsMapper.selectById(taskId);
            taskLog.setStatus(status);
            task = CopyUtil.copy(taskLog, Task.class);
            task.setExecuteTime(taskLog.getExecuteTime().getTime());
            taskInfoLogsMapper.updateById(taskLog);
            return task; // 返回task对象
        } catch (Exception e) {
            log.warn("task cancel exception taskid={}", taskId);
            throw new TaskNotExistException(e);
        }
    }

    private void removeTaskFromCache(Task task) {
        cacheService.zRemove(Constants.DBCACHE, JSON.toJSONString(task));
    }


}
