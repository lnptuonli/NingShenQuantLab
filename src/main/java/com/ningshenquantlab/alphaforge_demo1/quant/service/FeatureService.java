package com.ningshenquantlab.alphaforge_demo1.quant.service;

import com.ningshenquantlab.alphaforge_demo1.quant.dto.FeatureCalcRequest;
import com.ningshenquantlab.alphaforge_demo1.quant.dto.TaskResponse;
import com.ningshenquantlab.alphaforge_demo1.quant.entity.TaskLog;

import java.util.List;

/**
 * 特征计算服务接口
 * 
 * 职责：
 * 1. 提交特征计算任务
 * 2. 查询任务状态和列表
 * 3. 取消任务
 * 4. 查询任务日志
 * 
 * 实现类：FeatureServiceImpl
 */
public interface FeatureService {
    
    /**
     * 提交特征计算任务（异步执行）
     * 
     * @param request 特征计算请求
     * @return 任务ID
     */
    Long submitFeatureCalcTask(FeatureCalcRequest request);
    
    /**
     * 根据任务ID查询任务状态
     * 
     * @param taskId 任务ID
     * @return 任务响应
     */
    TaskResponse getTaskStatus(Long taskId);
    
    /**
     * 查询所有特征计算任务
     * 
     * @return 任务列表
     */
    List<TaskResponse> getAllTasks();
    
    /**
     * 根据状态查询任务
     * 
     * @param status 任务状态
     * @return 任务列表
     */
    List<TaskResponse> getTasksByStatus(String status);
    
    /**
     * 取消任务
     * 
     * @param taskId 任务ID
     */
    void cancelTask(Long taskId);
    
    /**
     * 查询任务日志
     * 
     * @param taskId 任务ID
     * @return 日志列表
     */
    List<TaskLog> getTaskLogs(Long taskId);
}

