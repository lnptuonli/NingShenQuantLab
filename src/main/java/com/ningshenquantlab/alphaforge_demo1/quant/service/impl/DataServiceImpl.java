package com.ningshenquantlab.alphaforge_demo1.quant.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ningshenquantlab.alphaforge_demo1.common.enums.TaskStatusEnum;
import com.ningshenquantlab.alphaforge_demo1.common.enums.TaskTypeEnum;
import com.ningshenquantlab.alphaforge_demo1.executor.ExecutionResult;
import com.ningshenquantlab.alphaforge_demo1.executor.LocalPythonExecutor;
import com.ningshenquantlab.alphaforge_demo1.quant.dao.TaskLogDao;
import com.ningshenquantlab.alphaforge_demo1.quant.dao.TaskRecordDao;
import com.ningshenquantlab.alphaforge_demo1.quant.dto.DataFetchRequest;
import com.ningshenquantlab.alphaforge_demo1.quant.dto.TaskResponse;
import com.ningshenquantlab.alphaforge_demo1.quant.entity.TaskLog;
import com.ningshenquantlab.alphaforge_demo1.quant.entity.TaskRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 数据服务实现类
 * 
 * 职责：
 * 1. 接收数据获取请求，调用 Python 脚本
 * 2. 管理任务生命周期（创建、更新状态、记录日志）
 * 3. 提供任务查询、取消等功能
 */
@Slf4j
@Service
public class DataServiceImpl {
    
    @Autowired
    private LocalPythonExecutor pythonExecutor;
    
    @Autowired
    private TaskRecordDao taskRecordDao;
    
    @Autowired
    private TaskLogDao taskLogDao;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 提交数据获取任务（异步执行）
     * 
     * @param request 数据获取请求
     * @return 任务ID
     */
    @Transactional
    public Long submitDataFetchTask(DataFetchRequest request) {
        log.info("提交数据获取任务：{}", request);
        
        // 1. 校验参数
        if (!request.isValidDateRange()) {
            throw new IllegalArgumentException("日期范围不合法：结束日期必须大于等于开始日期");
        }
        
        // 2. 创建任务记录
        // Lombok的链式构造器，每个方法返回的都是 builder 本身，反编译后是一个带有私有构造函数的、setter齐全的公共类
        TaskRecord taskRecord = TaskRecord.builder()
                .taskType(TaskTypeEnum.DATA_FETCH.getCode())
                .taskName(request.getEffectiveTaskName())
                .status(TaskStatusEnum.PENDING.getCode())
                .params(toJson(request))
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        
        taskRecordDao.insert(taskRecord);
        Long taskId = taskRecord.getId();
        
        log.info("任务记录已创建，taskId={}", taskId);
        
        // 3. 记录系统日志
        saveTaskLog(taskId, "SYSTEM", "INFO", "任务已提交，等待执行");
        
        // 4. 异步执行 Python 脚本
        String scriptName = TaskTypeEnum.DATA_FETCH.getScriptName();
        List<String> args = Arrays.asList(request.toPythonArgs());
        
        CompletableFuture<ExecutionResult> future = pythonExecutor.executeAsync(scriptName, args);
        
        // 5. 异步更新任务状态
        future.thenAccept(result -> {
            updateTaskStatus(taskId, result);
        }).exceptionally(ex -> {
            log.error("任务 {} 执行异常", taskId, ex);
            updateTaskStatusOnError(taskId, ex);
            return null;
        });
        
        return taskId;
    }
    
    /**
     * 根据任务ID查询任务状态
     * 
     * @param taskId 任务ID
     * @return 任务响应
     */
    public TaskResponse getTaskStatus(Long taskId) {
        log.debug("查询任务状态，taskId={}", taskId);
        
        TaskRecord taskRecord = taskRecordDao.selectById(taskId);
        if (taskRecord == null) {
            throw new IllegalArgumentException("任务不存在：" + taskId);
        }
        
        return TaskResponse.fromTaskRecord(taskRecord);
    }
    
    /**
     * 查询所有任务
     * 
     * @return 任务列表
     */
    public List<TaskResponse> getAllTasks() {
        log.debug("查询所有任务");
        
        List<TaskRecord> records = taskRecordDao.selectAll();
        return records.stream()
                .map(TaskResponse::fromTaskRecord)
                .toList();
    }
    
    /**
     * 根据状态查询任务
     * 
     * @param status 任务状态
     * @return 任务列表
     */
    public List<TaskResponse> getTasksByStatus(String status) {
        log.debug("查询任务，status={}", status);
        
        List<TaskRecord> records = taskRecordDao.selectByStatus(status);
        return records.stream()
                .map(TaskResponse::fromTaskRecord)
                .toList();
    }
    
    /**
     * 取消任务
     * 
     * @param taskId 任务ID
     */
    @Transactional
    public void cancelTask(Long taskId) {
        log.info("取消任务，taskId={}", taskId);
        
        // 1. 查询任务记录
        TaskRecord taskRecord = taskRecordDao.selectById(taskId);
        if (taskRecord == null) {
            throw new IllegalArgumentException("任务不存在：" + taskId);
        }
        
        // 2. 检查任务状态
        if (taskRecord.isTerminal()) {
            throw new IllegalStateException("任务已完成，无法取消");
        }
        
        // 3. 调用执行器取消任务
        pythonExecutor.cancelTask(taskId);
        
        // 4. 更新任务状态
        taskRecord.setStatus(TaskStatusEnum.CANCELLED.getCode());
        taskRecord.setEndTime(LocalDateTime.now());
        taskRecord.setUpdateTime(LocalDateTime.now());
        
        if (taskRecord.getStartTime() != null) {
            long duration = java.time.Duration.between(taskRecord.getStartTime(), taskRecord.getEndTime()).getSeconds();
            taskRecord.setDuration(duration);
        }
        
        taskRecordDao.update(taskRecord);
        
        // 5. 记录日志
        saveTaskLog(taskId, "SYSTEM", "INFO", "任务已被用户取消");
        
        log.info("任务 {} 已取消", taskId);
    }
    
    /**
     * 查询任务日志
     * 
     * @param taskId 任务ID
     * @return 日志列表
     */
    public List<TaskLog> getTaskLogs(Long taskId) {
        log.debug("查询任务日志，taskId={}", taskId);
        return taskLogDao.selectByTaskId(taskId);
    }
    
    // ==================== 私有方法 ====================
    
    /**
     * 更新任务状态（执行成功后）
     */
    private void updateTaskStatus(Long taskId, ExecutionResult result) {
        log.info("更新任务状态，taskId={}，result={}", taskId, result);
        
        TaskRecord taskRecord = taskRecordDao.selectById(taskId);
        if (taskRecord == null) {
            log.error("任务记录不存在，taskId={}", taskId);
            return;
        }
        
        // 更新任务状态
        taskRecord.setStatus(result.getTaskStatus().getCode());
        taskRecord.setStartTime(result.getStartTime());
        taskRecord.setEndTime(result.getEndTime());
        taskRecord.setExitCode(result.getExitCode());
        taskRecord.setDuration(result.getDurationSeconds());
        taskRecord.setUpdateTime(LocalDateTime.now());
        
        if (result.isSuccess()) {
            taskRecord.setResult(extractResult(result.getStdout()));
        } else {
            taskRecord.setErrorMessage(result.getErrorMessage());
        }
        
        taskRecordDao.update(taskRecord);
        
        // 保存日志
        if (result.getStdout() != null && !result.getStdout().isEmpty()) {
            saveTaskLog(taskId, "PYTHON", "STDOUT", result.getStdout());
        }
        if (result.getStderr() != null && !result.getStderr().isEmpty()) {
            saveTaskLog(taskId, "PYTHON", "STDERR", result.getStderr());
        }
        
        log.info("任务状态已更新，taskId={}，status={}", taskId, taskRecord.getStatus());
    }
    
    /**
     * 更新任务状态（执行异常时）
     */
    private void updateTaskStatusOnError(Long taskId, Throwable ex) {
        log.error("任务执行异常，taskId={}", taskId, ex);
        
        TaskRecord taskRecord = taskRecordDao.selectById(taskId);
        if (taskRecord == null) {
            log.error("任务记录不存在，taskId={}", taskId);
            return;
        }
        
        taskRecord.setStatus(TaskStatusEnum.FAILED.getCode());
        taskRecord.setEndTime(LocalDateTime.now());
        taskRecord.setErrorMessage(ex.getMessage());
        taskRecord.setUpdateTime(LocalDateTime.now());
        
        if (taskRecord.getStartTime() != null) {
            long duration = java.time.Duration.between(taskRecord.getStartTime(), taskRecord.getEndTime()).getSeconds();
            taskRecord.setDuration(duration);
        }
        
        taskRecordDao.update(taskRecord);
        
        // 保存错误日志
        saveTaskLog(taskId, "SYSTEM", "ERROR", "任务执行异常：" + ex.getMessage());
    }
    
    /**
     * 保存任务日志
     */
    private void saveTaskLog(Long taskId, String logSource, String logLevel, String logContent) {
        try {
            TaskLog taskLog = TaskLog.builder()
                    .taskId(taskId)
                    .logSource(logSource)
                    .logLevel(logLevel)
                    .logContent(logContent)
                    .createTime(LocalDateTime.now())
                    .build();
            
            taskLogDao.insert(taskLog);
        } catch (Exception e) {
            log.error("保存任务日志失败，taskId={}", taskId, e);
        }
    }
    
    /**
     * 从 stdout 中提取结果摘要
     */
    private String extractResult(String stdout) {
        if (stdout == null || stdout.isEmpty()) {
            return "执行成功";
        }
        
        // 简单提取最后几行作为结果
        String[] lines = stdout.split("\n");
        if (lines.length > 0) {
            String lastLine = lines[lines.length - 1];
            return lastLine.length() > 200 ? lastLine.substring(0, 200) + "..." : lastLine;
        }
        
        return "执行成功";
    }
    
    /**
     * 将对象转换为 JSON 字符串
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("JSON 序列化失败", e);
            return "{}";
        }
    }
}

