package com.liuche.schedule.service.impl;

import com.liuche.common.entity.Constants;
import com.liuche.common.entity.Task;
import com.liuche.common.exception.ScheduleSystemException;
import com.liuche.common.exception.TaskNotExistException;
import com.liuche.schedule.entity.TaskInfo;
import com.liuche.schedule.entity.TaskInfoLogs;
import com.liuche.schedule.mapper.TaskInfoLogsMapper;
import com.liuche.schedule.mapper.TaskInfoMapper;
import com.liuche.schedule.service.TaskService;
import com.liuche.schedule.utils.CopyUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class TaskServiceImpl implements TaskService {
    @Autowired
    private TaskInfoMapper taskInfoMapper;
    @Autowired
    private TaskInfoLogsMapper taskInfoLogsMapper;
    @Override
    public long addTask(Task task) throws ScheduleSystemException {
        /*
            向任务表中添加数据
            向任务日志表中添加数据
        * */
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
            log.warn("add task exception taskid={}",task.getTaskId());
            throw new ScheduleSystemException(e.getMessage());
        }
        return task.getTaskId();
    }

    @Override
    public boolean cancelTask(long taskId) throws TaskNotExistException {
        /*
            删除任务表中的数据
            更新日志表中的任务状态：2
        * */
        try {
            taskInfoMapper.deleteById(taskId);
            TaskInfoLogs taskLog = taskInfoLogsMapper.selectById(taskId);
            taskLog.setStatus(Constants.CANCELLED);
            taskInfoLogsMapper.updateById(taskLog);
        } catch (Exception e) {
            log.warn("task cancel exception taskid={}",taskId);
            throw new TaskNotExistException(e);
        }
        return true;
    }

}
