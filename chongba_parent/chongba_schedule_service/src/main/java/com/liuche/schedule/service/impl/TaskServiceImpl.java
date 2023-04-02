package com.liuche.schedule.service.impl;

import com.alibaba.fastjson.JSON;
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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class TaskServiceImpl implements TaskService {
    @Autowired
    private TaskInfoMapper taskInfoMapper;
    @Autowired
    private TaskInfoLogsMapper taskInfoLogsMapper;

    @Autowired
    private CacheService cacheService;

    @PostConstruct
    private void syncData() {
        System.out.println("init ..............");
        // 清除缓存中原有的数据
        clearCache();
        //从数据库查询所有任务数据
        List<TaskInfo> allTaskInfo = taskInfoMapper.selectList(null);
        //将任务数据存入缓存
        for (TaskInfo taskInfoEntity : allTaskInfo) {
            Task task = new Task();
            //属性拷贝
            BeanUtils.copyProperties(taskInfoEntity, task);
            task.setExecuteTime(taskInfoEntity.getExecuteTime().getTime());
            //放入缓存
            saveTaskInCache(task);
        }
    }

    private void clearCache() {
//移除所有的数据
        cacheService.delete(Constants.DBCACHE);
    }


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
        // 使用任务类型和优先级作为key
        String key = task.getTaskType() + "_" + task.getPriority();
        if (task.getExecuteTime() <= System.currentTimeMillis()) {
            // 当前任务在当前状态就需执行，存到redis中list集合中，减少时间复杂度
            // 存放到list集合中
            cacheService.lLeftPush(Constants.TOPIC + key, JSON.toJSONString(task));
        } else {
            // 未来需要执行的任务，存放到ZSet集合里面去
            cacheService.zAdd(Constants.FUTURE + key, JSON.toJSONString(task), task.getExecuteTime());
        }

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
        // 使用任务类型和优先级作为key
        String key = task.getTaskType() + "_" + task.getPriority();
        // 优化:如果task的执行时间小于当前时间戳则直接在list集合里面去删除，否则在ZSet集合里面删除
        if (task.getExecuteTime() <= System.currentTimeMillis()) {
            cacheService.lRemove(Constants.TOPIC + key, 0, JSON.toJSONString(task));
        } else {
            cacheService.zRemove(Constants.FUTURE + key, JSON.toJSONString(task));
        }
    }

    @Override
    public long size(int type, int priority) { // 返回当前缓存中的任务的数量
        String key = type + "_" + priority;
        Long len = cacheService.lLen(Constants.TOPIC + key);
        int size = cacheService.zRangeAll(Constants.FUTURE + key).size();
        return len + size;
    }

    @Override
    @Transactional
    public Task poll(int type, int priority) throws TaskNotExistException { // 拉取当前最近的任务
        Task task = null;
        try {
            // 从list得到当前可执行的定时任务
            String key = Constants.TOPIC + type + "_" + priority;
            String str = cacheService.lRightPop(key);
            if (StringUtils.hasLength(str)){
                task = JSON.parseObject(str, Task.class);
                // 更新数据库信息
                updateDB(task.getTaskId(), Constants.EXECUTED);
            }
        } catch (Exception e) {
            log.warn("poll task exception");
            throw new TaskNotExistException(e);
        }
//        try {
//            // 得到时间点到现在的所有任务
//            Set<String> tasks = cacheService.zRange(Constants.DBCACHE, 0, System.currentTimeMillis());
//            // 返回最近的任务
//            if (tasks == null || tasks.isEmpty()) return null; // 还没有任务task来返回,则返回null
//            String value = tasks.iterator().next(); // 用迭代器得到最近的一个任务，因为zSet已经做了排序
//            // 转换成对象
//            task = JSON.parseObject(value, Task.class);
//            if (task.getExecuteTime() > System.currentTimeMillis()) return null;
//            // 从缓存中删除该任务
//            cacheService.zRemove(Constants.DBCACHE, value);
//            // 更新数据库的信息
//            updateDB(task.getTaskId(), Constants.EXECUTED);
//        } catch (Exception e) {
//            log.warn("poll task exception");
//            throw new TaskNotExistException(e);
//        }

        // 返回任务
        return task;
    }
}
