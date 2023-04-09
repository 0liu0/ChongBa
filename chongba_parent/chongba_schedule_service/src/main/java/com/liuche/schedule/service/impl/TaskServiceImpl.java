package com.liuche.schedule.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.liuche.common.entity.Constants;
import com.liuche.common.entity.Task;
import com.liuche.common.exception.ScheduleSystemException;
import com.liuche.common.exception.TaskNotExistException;
import com.liuche.common.utils.CacheService;
import com.liuche.schedule.config.SystemParams;
import com.liuche.schedule.entity.TaskInfo;
import com.liuche.schedule.entity.TaskInfoLogs;
import com.liuche.schedule.mapper.TaskInfoLogsMapper;
import com.liuche.schedule.mapper.TaskInfoMapper;
import com.liuche.schedule.service.SelectMaster;
import com.liuche.schedule.service.TaskService;
import com.liuche.schedule.utils.CopyUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class TaskServiceImpl implements TaskService {
    private static final Logger threadLog = LoggerFactory.getLogger("thread");
    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;
    @Autowired
    private ThreadPoolTaskExecutor poolTaskExecutor;
    @Autowired
    private TaskInfoMapper taskInfoMapper;
    @Autowired
    private TaskInfoLogsMapper taskInfoLogsMapper;
    @Autowired
    private SystemParams params;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private SelectMaster selectMaster;

    @PostConstruct
    private void syncData() {
        // 抢占主节点
        selectMaster.selectMaster(Constants.schedule_leaderPath);
        // 选取会有些许延迟等个一秒
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        threadPoolTaskScheduler.scheduleAtFixedRate(() -> {
            // 判断是不是主节点，是主节点才执行刷新动作
            if(selectMaster.checkMaster(Constants.schedule_leaderPath)){
                threadLog.info("schedule-service主节点进行数据恢复reloadData---");
                init();
            }else {
                threadLog.info("schedule-service从节点备用");
            }
        },TimeUnit.MINUTES.toMillis(params.getPreLoad()));

//        boolean flag = selectMaster.checkMaster(Constants.schedule_leaderPath);
//        if (flag) {
//            threadPoolTaskScheduler.scheduleAtFixedRate(this::init, TimeUnit.MINUTES.toMillis(params.getPreLoad())); // 两分钟执行数据恢复init的方法
//        }
    }

    private void init() {
        /**
         * 清除缓存原有数据：编写：clearCache()
         查询所有任务数据：调用 taskMapper.selectAll()
         将任务数据放入缓存：封装Task，调用addTaskToCache(task);
         */
        System.out.println("init");
        clearCache();
        //查询所有任务类型和优先级的分组
        QueryWrapper<TaskInfo> wrapper = new QueryWrapper<>();
        wrapper.select("task_type", "priority");
        wrapper.groupBy("task_type", "priority");
        List<Map<String, Object>> maps = taskInfoMapper.selectMaps(wrapper);
        long start = System.currentTimeMillis();

        // 得到当前时间的未来两分钟时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, params.getPreLoad());

        CountDownLatch latch = new CountDownLatch(maps.size());

        for (Map<String, Object> map : maps) {

            poolTaskExecutor.execute(() -> {
                String task_type = String.valueOf(map.get("task_type"));
                String priority = String.valueOf(map.get("priority"));
                //根据任务类型和优先级去查询该组下的任务数据
                // List<TaskInfo> allTasks = taskInfoMapper.queryAll(Integer.parseInt(task_type), Integer.parseInt(priority));
                // 获取此刻到未来时间的任务
                List<TaskInfo> allTasks = taskInfoMapper.queryFuture(Integer.parseInt(task_type), Integer.parseInt(priority), calendar.getTime());
                System.out.println("=================>");
                for (TaskInfo task : allTasks) {
                    System.out.println(task);
                }
                if (allTasks != null && !allTasks.isEmpty()) {
                    for (TaskInfo taskInfoEntity : allTasks) {
                        Task task = new Task();
                        BeanUtils.copyProperties(taskInfoEntity, task);
                        task.setExecuteTime(taskInfoEntity.getExecuteTime().getTime());
                        saveTaskInCache(task);
                    }
                }

                latch.countDown();
                threadLog.info("当前线程名称{},计数器的值{},当前分组数据恢复的时间{}",
                        Thread.currentThread().getName(), latch.getCount(), System.currentTimeMillis() - start);
            });
        }

        try {
            //阻塞当前线程 当latch=0结束阻塞
            latch.await(5, TimeUnit.MINUTES);
            threadLog.info("数据恢复完成，耗时{}", System.currentTimeMillis() - start);
        } catch (InterruptedException e) {
            threadLog.error("数据恢复出现异常，异常信息{}", e.getMessage());
        }
        /*List<TaskInfoEntity> allTasks = infoMapper.selectAll();
        if(allTasks!=null && ! allTasks.isEmpty()){
            for (TaskInfoEntity taskInfoEntity : allTasks) {
                Task task = new Task();
                BeanUtils.copyProperties(taskInfoEntity,task);
                task.setExecuteTime(taskInfoEntity.getExecuteTime().getTime());
                addTaskToCache(task);
            }
        }*/
//        System.out.println("init ..............");
//        // 清除缓存中原有的数据
//        clearCache();
//        //从数据库查询所有任务数据
//        List<TaskInfo> allTaskInfo = taskInfoMapper.selectList(null);
//        //将任务数据存入缓存
//        for (TaskInfo taskInfoEntity : allTaskInfo) {
//            Task task = new Task();
//            //属性拷贝
//            BeanUtils.copyProperties(taskInfoEntity, task);
//            task.setExecuteTime(taskInfoEntity.getExecuteTime().getTime());
//            //放入缓存
//            saveTaskInCache(task);
//        }

    }

    private void clearCache() {
        //移除所有的数据 包括未来数据集合和消费者队列，移除所有的key即可
        //cacheService.delete(Constants.DBCACHE);
        // 获取未来数据集合所有的key
        Set<String> futureKeys = cacheService.scan(Constants.FUTURE + "*");// future_*
        cacheService.delete(futureKeys);
        //获取消费者队列所有的key
        Set<String> topicKeys = cacheService.scan(Constants.TOPIC + "*");// topic_*
        cacheService.delete(topicKeys);
    }


    @Override
    @Transactional // 添加事务
    public long addTask(Task task) throws ScheduleSystemException {
        /*
            向任务表中添加数据
            向任务日志表中添加数据
            将延时任务写入缓存
        * */
        Future<Long> future = poolTaskExecutor.submit(() -> {
            boolean flag = saveTaskInDB(task);
            if (flag) {
                saveTaskInCache(task);
            }
            return task.getTaskId();
        });
        Long id;
        try {
            id = future.get();
        } catch (Exception e) {
            threadLog.warn("addTask was in mistake!");
            throw new ScheduleSystemException(e);
        }
        return id;
//        boolean flag = saveTaskInDB(task);
//        if (flag) {
//            saveTaskInCache(task);
//        }
//        return task.getTaskId();
    }

    private void saveTaskInCache(Task task) {
        // 使用任务类型和优先级作为key
        String key = task.getTaskType() + "_" + task.getPriority();
        if (task.getExecuteTime() <= System.currentTimeMillis()) {
            // 当前任务在当前状态就需执行，存到redis中list集合中，减少时间复杂度
            // 存放到list集合中
            cacheService.lLeftPush(Constants.TOPIC + key, JSON.toJSONString(task));
        } else if (task.getExecuteTime() <= System.currentTimeMillis() + 1000 * (params.getPreLoad() * 60L)) {
            // 未来需要执行的任务，存放到ZSet集合里面去
            cacheService.zAdd(Constants.FUTURE + key, JSON.toJSONString(task), task.getExecuteTime());
        }

    }

    private boolean saveTaskInDB(Task task) {
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
            threadLog.warn("add task exception taskid={}", task.getTaskId());
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
            threadLog.warn("task cancel exception taskid={}", taskId);
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
            threadLog.warn("task cancel exception taskid={}", taskId);
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
        Task task;
        Future<Task> future = poolTaskExecutor.submit(() -> {
            try {
                // 从list得到当前可执行的定时任务
                String key = Constants.TOPIC + type + "_" + priority;
                String str = cacheService.lRightPop(key);
                Task t = null;
                if (StringUtils.hasLength(str)) {
                    t = JSON.parseObject(str, Task.class);
                    // 更新数据库信息
                    updateDB(t.getTaskId(), Constants.EXECUTED);
                }
                return t;
            } catch (Exception e) {
                threadLog.warn("poll() was in mistake!");
                throw new ScheduleSystemException(e);
            }
        });
        try {
            task = future.get();
        } catch (Exception e) {
            threadLog.warn("poll() was in mistake!");
            throw new ScheduleSystemException(e);
        }
        // 返回任务
        return task;
//        try {
//            // 从list得到当前可执行的定时任务
//            String key = Constants.TOPIC + type + "_" + priority;
//            String str = cacheService.lRightPop(key);
//            if (StringUtils.hasLength(str)){
//                task = JSON.parseObject(str, Task.class);
//                // 更新数据库信息
//                updateDB(task.getTaskId(), Constants.EXECUTED);
//            }
//        } catch (Exception e) {
//            threadLog.warn("poll task exception");
//            throw new TaskNotExistException(e);
//        }
//        // 返回任务
//        return task;
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
//            threadLog.warn("poll task exception");
//            throw new TaskNotExistException(e);
//        }
    }
}
