package com.liuche.schedule.service;

import com.liuche.common.entity.Task;
import com.liuche.common.exception.ScheduleSystemException;
import com.liuche.common.exception.TaskNotExistException;

public interface TaskService {
    /**
     * 添加任务
     *
     * @param task 任务对象
     * @return 任务id
     * @throws ScheduleSystemException
     */
    public long addTask(Task task) throws ScheduleSystemException;

    /**
     * 取消任务
     *
     * @param taskId 任务id
     * @throws TaskNotExistException
     * @return 取消结果
     */
    public boolean cancelTask(long taskId) throws TaskNotExistException;
    long size(int type,int priority);

    Task poll(int type,int priority)throws TaskNotExistException;

}
