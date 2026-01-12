package com.ningshenquantlab.alphaforge_demo1.quant.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ningshenquantlab.alphaforge_demo1.common.enums.TaskStatusEnum;
import com.ningshenquantlab.alphaforge_demo1.common.enums.TaskTypeEnum;
import com.ningshenquantlab.alphaforge_demo1.executor.ExecutionResult;
import com.ningshenquantlab.alphaforge_demo1.executor.LocalPythonExecutor;
import com.ningshenquantlab.alphaforge_demo1.quant.dao.TaskLogDao;
import com.ningshenquantlab.alphaforge_demo1.quant.dao.TaskRecordDao;
import com.ningshenquantlab.alphaforge_demo1.quant.dto.FeatureCalcRequest;
import com.ningshenquantlab.alphaforge_demo1.quant.dto.TaskResponse;
import com.ningshenquantlab.alphaforge_demo1.quant.entity.TaskLog;
import com.ningshenquantlab.alphaforge_demo1.quant.entity.TaskRecord;
import com.ningshenquantlab.alphaforge_demo1.quant.service.FeatureService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 特征计算服务实现类
 * 
 * 职责：
 * 1. 接收特征计算请求，调用 Python 脚本
 * 2. 管理任务生命周期（创建、更新状态、记录日志）
 * 3. 提供任务查询、取消等功能
 */
@Slf4j
@Service
public class FeatureServiceImpl implements FeatureService {
    
    @Autowired
    private LocalPythonExecutor pythonExecutor;
    
    @Autowired
    private TaskRecordDao taskRecordDao;
    
    @Autowired
    private TaskLogDao taskLogDao;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 提交特征计算任务（异步执行）
     * 
     * @param request 特征计算请求
     * @return 任务ID
     */
    @Override
    @Transactional
    public Long submitFeatureCalcTask(FeatureCalcRequest request) {
        log.info("提交特征计算任务：{}", request);
        
        // 1. 校验参数
        try {
            request.validate();
        } catch (IllegalArgumentException e) {
            log.error("参数校验失败", e);
            throw e;
        }
        
        // 2. 创建任务记录
        TaskRecord taskRecord = TaskRecord.builder()
                .taskType(TaskTypeEnum.FEATURE_CALC.getCode())
                .taskName(request.getEffectiveTaskName())
                .status(TaskStatusEnum.PENDING.getCode())
                .params(toJson(request))
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        
        taskRecordDao.insert(taskRecord);
        Long taskId = taskRecord.getId();
        
        log.info("特征计算任务记录已创建，taskId={}，计算范围：{}", 
                taskId, request.getDateRangeDescription());
        
        // 3. 记录系统日志
        String logMsg = String.format("任务已提交，等待执行。计算范围：%s，强制重算：%s", 
                request.getDateRangeDescription(), request.getForce());
        saveTaskLog(taskId, "SYSTEM", "INFO", logMsg);
        
        // 4. 异步执行 Python 脚本
        String scriptName = TaskTypeEnum.FEATURE_CALC.getScriptName();
        List<String> args = request.toPythonArgs();
        
        log.info("开始执行特征计算脚本，scriptName={}，args={}", scriptName, args);
        
        CompletableFuture<ExecutionResult> future = pythonExecutor.executeAsync(scriptName, args);
        
        // 5. 异步更新任务状态
        future.thenAccept(result -> {
            updateTaskStatus(taskId, result);
        }).exceptionally(ex -> {
            log.error("特征计算任务 {} 执行异常", taskId, ex);
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
    @Override
    public TaskResponse getTaskStatus(Long taskId) {
        log.debug("查询特征计算任务状态，taskId={}", taskId);
        
        TaskRecord taskRecord = taskRecordDao.selectById(taskId);
        if (taskRecord == null) {
            throw new IllegalArgumentException("任务不存在：" + taskId);
        }
        
        // 只返回特征计算类型的任务
        if (!TaskTypeEnum.FEATURE_CALC.getCode().equals(taskRecord.getTaskType())) {
            throw new IllegalArgumentException("任务类型不匹配，期望：FEATURE_CALC，实际：" + taskRecord.getTaskType());
        }
        
        return TaskResponse.fromTaskRecord(taskRecord);
    }
    
    /**
     * 查询所有特征计算任务
     * 
     * @return 任务列表
     */
    @Override
    public List<TaskResponse> getAllTasks() {
        log.debug("查询所有特征计算任务");
        
        List<TaskRecord> records = taskRecordDao.selectByTaskType(TaskTypeEnum.FEATURE_CALC.getCode());
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
    @Override
    public List<TaskResponse> getTasksByStatus(String status) {
        log.debug("查询特征计算任务，status={}", status);
        
        List<TaskRecord> records = taskRecordDao.selectByStatus(status);
        
        // 只返回特征计算类型的任务
        return records.stream()
                .filter(r -> TaskTypeEnum.FEATURE_CALC.getCode().equals(r.getTaskType()))
                .map(TaskResponse::fromTaskRecord)
                .toList();
    }
    
    /**
     * 取消任务
     * 
     * @param taskId 任务ID
     */
    @Override
    @Transactional
    public void cancelTask(Long taskId) {
        log.info("取消特征计算任务，taskId={}", taskId);
        
        // 1. 查询任务记录
        TaskRecord taskRecord = taskRecordDao.selectById(taskId);
        if (taskRecord == null) {
            throw new IllegalArgumentException("任务不存在：" + taskId);
        }
        
        // 2. 检查任务类型
        if (!TaskTypeEnum.FEATURE_CALC.getCode().equals(taskRecord.getTaskType())) {
            throw new IllegalArgumentException("任务类型不匹配，无法取消");
        }
        
        // 3. 检查任务状态
        if (taskRecord.isTerminal()) {
            throw new IllegalStateException("任务已完成，无法取消");
        }
        
        // 4. 调用执行器取消任务
        pythonExecutor.cancelTask(taskId);
        
        // 5. 更新任务状态
        taskRecord.setStatus(TaskStatusEnum.CANCELLED.getCode());
        taskRecord.setEndTime(LocalDateTime.now());
        taskRecord.setUpdateTime(LocalDateTime.now());
        
        if (taskRecord.getStartTime() != null) {
            long duration = java.time.Duration.between(taskRecord.getStartTime(), taskRecord.getEndTime()).getSeconds();
            taskRecord.setDuration(duration);
        }
        
        taskRecordDao.update(taskRecord);
        
        // 6. 记录日志
        saveTaskLog(taskId, "SYSTEM", "INFO", "任务已被用户取消");
        
        log.info("特征计算任务 {} 已取消", taskId);
    }
    
    /**
     * 查询任务日志
     * 
     * @param taskId 任务ID
     * @return 日志列表
     */
    @Override
    public List<TaskLog> getTaskLogs(Long taskId) {
        log.debug("查询特征计算任务日志，taskId={}", taskId);
        return taskLogDao.selectByTaskId(taskId);
    }
    
    // ==================== 私有方法 ====================
    
    /**
     * 更新任务状态（执行成功后）
     */
    private void updateTaskStatus(Long taskId, ExecutionResult result) {
        log.info("更新特征计算任务状态，taskId={}，result={}", taskId, result);
        
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
        
        log.info("特征计算任务状态已更新，taskId={}，status={}", taskId, taskRecord.getStatus());
    }
    
    /**
     * 更新任务状态（执行异常时）
     */
    private void updateTaskStatusOnError(Long taskId, Throwable ex) {
        log.error("特征计算任务执行异常，taskId={}", taskId, ex);
        
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
     * 
     * 说明：
     * - 自动限制日志长度，防止超过数据库字段限制
     * - TEXT 类型最大 65,535 字节（约 64KB）
     * - 如果日志过长，保留前 30KB 和后 30KB，中间省略
     */
    private void saveTaskLog(Long taskId, String logSource, String logLevel, String logContent) {
        try {
            // 限制日志长度，防止超过数据库字段限制
            String truncatedContent = truncateLogContent(logContent);
            
            TaskLog taskLog = TaskLog.builder()
                    .taskId(taskId)
                    .logSource(logSource)
                    .logLevel(logLevel)
                    .logContent(truncatedContent)
                    .createTime(LocalDateTime.now())
                    .build();
            
            taskLogDao.insert(taskLog);
        } catch (Exception e) {
            log.error("保存任务日志失败，taskId={}", taskId, e);
        }
    }
    
    /**
     * 截断过长的日志内容
     * 
     * 策略：
     * - 如果日志 <= 500 字符，保留完整内容
     * - 如果日志 > 500 字符，保留前 100 字符 + 中间省略提示 + 后 100 字符
     * 
     * @param logContent 原始日志内容
     * @return 截断后的日志内容
     */
    private String truncateLogContent(String logContent) {
        if (logContent == null) {
            return "";
        }
        
        final int MAX_LENGTH = 500;   // 超过500字符就截断
        final int KEEP_HEAD = 100;    // 保留前100字符
        final int KEEP_TAIL = 100;    // 保留后100字符
        
        // 如果长度在限制内，直接返回
        if (logContent.length() <= MAX_LENGTH) {
            return logContent;
        }
        
        // 截断：保留前后部分
        String head = logContent.substring(0, KEEP_HEAD);
        String tail = logContent.substring(logContent.length() - KEEP_TAIL);
        
        int omittedLength = logContent.length() - KEEP_HEAD - KEEP_TAIL;
        
        return head + 
               "\n... [已省略 " + omittedLength + " 个字符，原始长度 " + logContent.length() + "] ...\n" +
               tail;
    }
    
    /**
     * 从 stdout 中提取结果摘要
     */
    private String extractResult(String stdout) {
        if (stdout == null || stdout.isEmpty()) {
            return "特征计算完成";
        }
        
        // 简单提取最后几行作为结果
        String[] lines = stdout.split("\n");
        if (lines.length > 0) {
            String lastLine = lines[lines.length - 1];
            return lastLine.length() > 200 ? lastLine.substring(0, 200) + "..." : lastLine;
        }
        
        return "特征计算完成";
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
