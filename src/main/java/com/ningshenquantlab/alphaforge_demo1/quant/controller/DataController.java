package com.ningshenquantlab.alphaforge_demo1.quant.controller;

import com.ningshenquantlab.alphaforge_demo1.common.Result;
import com.ningshenquantlab.alphaforge_demo1.quant.dto.DataFetchRequest;
import com.ningshenquantlab.alphaforge_demo1.quant.dto.TaskResponse;
import com.ningshenquantlab.alphaforge_demo1.quant.entity.TaskLog;
import com.ningshenquantlab.alphaforge_demo1.quant.service.DataService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据获取 API
 * 
 * 路径：/api/quant/data
 * 
 * 功能：
 * - 提交数据获取任务
 * - 查询任务状态
 * - 查询任务列表
 * - 取消任务
 * - 查询任务日志
 * 
 * 使用示例：
 * 1. 提交任务：
 *    POST /api/quant/data/fetch
 *    Body: {"startDate":"20240101","endDate":"20240201"}
 *    Response: {"code":200,"message":"任务已提交","data":{"taskId":123}}
 * 
 * 2. 查询任务状态：
 *    GET /api/quant/data/tasks/123
 *    Response: {"code":200,"data":{"taskId":123,"status":"RUNNING",...}}
 * 
 * 3. 取消任务：
 *    DELETE /api/quant/data/tasks/123
 *    Response: {"code":200,"message":"任务已取消"}
 */
@Slf4j
@RestController
@RequestMapping("/api/quant/data")
public class DataController {
    
    @Autowired
    private DataService dataService;
    
    /**
     * 提交数据获取任务
     * 
     * POST /api/quant/data/fetch
     * 
     * 请求示例：
     * {
     *   "startDate": "20240101",
     *   "endDate": "20240201",
     *   "dataType": "stock_daily",
     *   "symbols": "000001,000002",
     *   "taskName": "日常数据更新",
     *   "forceRefresh": false,
     *   "timeoutSeconds": 3600
     * }
     * 
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "任务已提交，正在后台执行",
     *   "data": {
     *     "taskId": 123,
     *     "taskName": "数据获取：20240101-20240201",
     *     "status": "PENDING",
     *     "message": "任务已提交，请稍后查询执行结果"
     *   }
     * }
     * 
     * @param request 数据获取请求
     * @return 任务ID和提示信息
     */
    @PostMapping("/fetch")
    public Result<Object> submitDataFetchTask(@Valid @RequestBody DataFetchRequest request) {
        log.info("接收数据获取请求：{}", request);
        
        try {
            // 提交任务
            Long taskId = dataService.submitDataFetchTask(request);
            
            // 返回任务ID
            return Result.success("任务已提交，正在后台执行", 
                    java.util.Map.of(
                            "taskId", taskId,
                            "taskName", request.getEffectiveTaskName(),
                            "status", "PENDING",
                            "message", "任务已提交，请稍后查询执行结果"
                    ));
            
        } catch (IllegalArgumentException e) {
            log.error("参数校验失败", e);
            return Result.error("参数错误：" + e.getMessage());
        } catch (Exception e) {
            log.error("提交任务失败", e);
            return Result.error("提交任务失败：" + e.getMessage());
        }
    }
    
    /**
     * 查询任务状态
     * 
     * GET /api/quant/data/tasks/{taskId}
     * 
     * 响应示例：
     * {
     *   "code": 200,
     *   "data": {
     *     "taskId": 123,
     *     "taskType": "DATA_FETCH",
     *     "taskTypeDesc": "数据获取",
     *     "taskName": "数据获取：20240101-20240201",
     *     "status": "RUNNING",
     *     "statusDesc": "执行中",
     *     "progress": 50,
     *     "startTime": "2024-01-01 10:00:00",
     *     "duration": "5分30秒",
     *     "result": null,
     *     "errorMessage": null
     *   }
     * }
     * 
     * @param taskId 任务ID
     * @return 任务详细信息
     */
    @GetMapping("/tasks/{taskId}")
    public Result<TaskResponse> getTaskStatus(@PathVariable Long taskId) {
        log.info("查询任务状态，taskId={}", taskId);
        
        try {
            TaskResponse response = dataService.getTaskStatus(taskId);
            return Result.success(response);
        } catch (IllegalArgumentException e) {
            log.error("任务不存在", e);
            return Result.error("任务不存在：" + taskId);
        } catch (Exception e) {
            log.error("查询任务状态失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }
    
    /**
     * 查询所有任务
     * 
     * GET /api/quant/data/tasks
     * 
     * 响应示例：
     * {
     *   "code": 200,
     *   "data": [
     *     {"taskId": 123, "taskName": "数据获取：20240101-20240201", "status": "RUNNING", ...},
     *     {"taskId": 122, "taskName": "数据获取：20231201-20231231", "status": "SUCCESS", ...}
     *   ]
     * }
     * 
     * @return 任务列表
     */
    @GetMapping("/tasks")
    public Result<List<TaskResponse>> getAllTasks() {
        log.info("查询所有任务");
        
        try {
            List<TaskResponse> tasks = dataService.getAllTasks();
            return Result.success(tasks);
        } catch (Exception e) {
            log.error("查询任务列表失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据状态查询任务
     * 
     * GET /api/quant/data/tasks?status=RUNNING
     * 
     * @param status 任务状态（PENDING、RUNNING、SUCCESS、FAILED、CANCELLED、TIMEOUT）
     * @return 任务列表
     */
    @GetMapping("/tasks/by-status")
    public Result<List<TaskResponse>> getTasksByStatus(@RequestParam String status) {
        log.info("查询任务，status={}", status);
        
        try {
            List<TaskResponse> tasks = dataService.getTasksByStatus(status);
            return Result.success(tasks);
        } catch (Exception e) {
            log.error("查询任务失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }
    
    /**
     * 取消任务
     * 
     * DELETE /api/quant/data/tasks/{taskId}
     * 
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "任务已取消"
     * }
     * 
     * @param taskId 任务ID
     * @return 操作结果
     */
    @DeleteMapping("/tasks/{taskId}")
    public Result<Object> cancelTask(@PathVariable Long taskId) {
        log.info("取消任务，taskId={}", taskId);
        
        try {
            dataService.cancelTask(taskId);
            return Result.success("任务已取消");
        } catch (IllegalArgumentException e) {
            log.error("任务不存在", e);
            return Result.error("任务不存在：" + taskId);
        } catch (IllegalStateException e) {
            log.error("任务状态不允许取消", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("取消任务失败", e);
            return Result.error("取消失败：" + e.getMessage());
        }
    }
    
    /**
     * 查询任务日志
     * 
     * GET /api/quant/data/tasks/{taskId}/logs
     * 
     * 响应示例：
     * {
     *   "code": 200,
     *   "data": [
     *     {"logLevel": "INFO", "logContent": "任务已提交，等待执行", "createTime": "2024-01-01 10:00:00"},
     *     {"logLevel": "STDOUT", "logContent": "成功获取 1000 条数据", "createTime": "2024-01-01 10:05:00"}
     *   ]
     * }
     * 
     * @param taskId 任务ID
     * @return 日志列表
     */
    @GetMapping("/tasks/{taskId}/logs")
    public Result<List<TaskLog>> getTaskLogs(@PathVariable Long taskId) {
        log.info("查询任务日志，taskId={}", taskId);
        
        try {
            List<TaskLog> logs = dataService.getTaskLogs(taskId);
            return Result.success(logs);
        } catch (Exception e) {
            log.error("查询任务日志失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }
    
    /**
     * 健康检查接口
     * 
     * GET /api/quant/data/health
     * 
     * @return OK
     */
    @GetMapping("/health")
    public Result<Object> health() {
        return Result.success("Data Service is running");
    }
}

