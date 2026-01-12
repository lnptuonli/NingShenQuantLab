package com.ningshenquantlab.alphaforge_demo1.quant.controller;

import com.ningshenquantlab.alphaforge_demo1.common.Result;
import com.ningshenquantlab.alphaforge_demo1.quant.dto.FeatureCalcRequest;
import com.ningshenquantlab.alphaforge_demo1.quant.dto.TaskResponse;
import com.ningshenquantlab.alphaforge_demo1.quant.entity.TaskLog;
import com.ningshenquantlab.alphaforge_demo1.quant.service.FeatureService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 特征计算 API
 * 
 * 路径：/api/quant/features
 * 
 * 功能：
 * - 提交特征计算任务
 * - 查询任务状态
 * - 查询任务列表
 * - 取消任务
 * - 查询任务日志
 * 
 * 使用示例：
 * 1. 提交单日计算任务：
 *    POST /api/quant/features/calculate
 *    Body: {"date":"20250301","force":true}
 *    Response: {"code":200,"message":"任务已提交","data":{"taskId":123}}
 * 
 * 2. 提交批量计算任务：
 *    POST /api/quant/features/calculate
 *    Body: {"startDate":"20250301","endDate":"20250305","force":false}
 *    Response: {"code":200,"message":"任务已提交","data":{"taskId":124}}
 * 
 * 3. 查询任务状态：
 *    GET /api/quant/features/tasks/123
 *    Response: {"code":200,"data":{"taskId":123,"status":"RUNNING",...}}
 * 
 * 4. 取消任务：
 *    DELETE /api/quant/features/tasks/123
 *    Response: {"code":200,"message":"任务已取消"}
 */
@Slf4j
@RestController
@RequestMapping("/api/quant/features")
public class FeatureController {

    @Autowired
    private FeatureService featureService;

    /**
     * 提交特征计算任务
     * 
     * POST /api/quant/features/calculate
     * 
     * 请求示例 1：单日计算
     * {
     *   "date": "20250301",
     *   "force": true,
     *   "taskName": "单日特征计算"
     * }
     * 
     * 请求示例 2：批量计算
     * {
     *   "startDate": "20250301",
     *   "endDate": "20250305",
     *   "force": true,
     *   "featureType": "alpha",
     *   "taskName": "批量特征计算",
     *   "timeoutSeconds": 7200
     * }
     * 
     * 响应示例：
     * {
     *   "code": 200,
     *   "message": "特征计算任务已提交，正在后台执行",
     *   "data": {
     *     "taskId": 123,
     *     "taskName": "特征计算：20250301-20250305",
     *     "status": "PENDING",
     *     "message": "任务已提交，请稍后查询执行结果"
     *   }
     * }
     * 
     * @param request 特征计算请求
     * @return 任务ID和提示信息
     */
    @PostMapping("/calculate")
    public Result<Object> calculateFeatures(@Valid @RequestBody FeatureCalcRequest request) {
        log.info("接收特征计算请求：{}", request);
        
        try {
            // 提交任务
            Long taskId = featureService.submitFeatureCalcTask(request);
            
            // 返回任务ID
            return Result.success("特征计算任务已提交，正在后台执行", 
                    java.util.Map.of(
                            "taskId", taskId,
                            "taskName", request.getEffectiveTaskName(),
                            "status", "PENDING",
                            "calcMode", request.isSingleDateMode() ? "单日计算" : "批量计算",
                            "dateRange", request.getDateRangeDescription(),
                            "force", request.getForce(),
                            "message", "任务已提交，请稍后查询执行结果"
                    ));
            
        } catch (IllegalArgumentException e) {
            log.error("参数校验失败", e);
            return Result.error("参数错误：" + e.getMessage());
        } catch (Exception e) {
            log.error("提交特征计算任务失败", e);
            return Result.error("提交任务失败：" + e.getMessage());
        }
    }

    /**
     * 查询任务状态
     * 
     * GET /api/quant/features/tasks/{taskId}
     * 
     * 响应示例：
     * {
     *   "code": 200,
     *   "data": {
     *     "taskId": 123,
     *     "taskType": "FEATURE_CALC",
     *     "taskTypeDesc": "特征计算",
     *     "taskName": "特征计算：20250301-20250305",
     *     "status": "RUNNING",
     *     "statusDesc": "执行中",
     *     "progress": 50,
     *     "startTime": "2025-03-01 10:00:00",
     *     "duration": "15分30秒",
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
        log.info("查询特征计算任务状态，taskId={}", taskId);
        
        try {
            TaskResponse response = featureService.getTaskStatus(taskId);
            return Result.success(response);
        } catch (IllegalArgumentException e) {
            log.error("任务不存在或类型不匹配", e);
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("查询任务状态失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 查询所有特征计算任务
     * 
     * GET /api/quant/features/tasks
     * 
     * 响应示例：
     * {
     *   "code": 200,
     *   "data": [
     *     {"taskId": 123, "taskName": "特征计算：20250301-20250305", "status": "RUNNING", ...},
     *     {"taskId": 122, "taskName": "特征计算：20250201", "status": "SUCCESS", ...}
     *   ]
     * }
     * 
     * @return 任务列表
     */
    @GetMapping("/tasks")
    public Result<List<TaskResponse>> getAllTasks() {
        log.info("查询所有特征计算任务");
        
        try {
            List<TaskResponse> tasks = featureService.getAllTasks();
            return Result.success(tasks);
        } catch (Exception e) {
            log.error("查询任务列表失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据状态查询任务
     * 
     * GET /api/quant/features/tasks/by-status?status=RUNNING
     * 
     * @param status 任务状态（PENDING、RUNNING、SUCCESS、FAILED、CANCELLED、TIMEOUT）
     * @return 任务列表
     */
    @GetMapping("/tasks/by-status")
    public Result<List<TaskResponse>> getTasksByStatus(@RequestParam String status) {
        log.info("查询特征计算任务，status={}", status);
        
        try {
            List<TaskResponse> tasks = featureService.getTasksByStatus(status);
            return Result.success(tasks);
        } catch (Exception e) {
            log.error("查询任务失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 取消任务
     * 
     * DELETE /api/quant/features/tasks/{taskId}
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
        log.info("取消特征计算任务，taskId={}", taskId);
        
        try {
            featureService.cancelTask(taskId);
            return Result.success("任务已取消");
        } catch (IllegalArgumentException e) {
            log.error("任务不存在或类型不匹配", e);
            return Result.error(e.getMessage());
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
     * GET /api/quant/features/tasks/{taskId}/logs
     * 
     * 响应示例：
     * {
     *   "code": 200,
     *   "data": [
     *     {"logLevel": "INFO", "logContent": "任务已提交，等待执行", "createTime": "2025-03-01 10:00:00"},
     *     {"logLevel": "STDOUT", "logContent": "成功计算 100 个特征", "createTime": "2025-03-01 10:15:00"}
     *   ]
     * }
     * 
     * @param taskId 任务ID
     * @return 日志列表
     */
    @GetMapping("/tasks/{taskId}/logs")
    public Result<List<TaskLog>> getTaskLogs(@PathVariable Long taskId) {
        log.info("查询特征计算任务日志，taskId={}", taskId);
        
        try {
            List<TaskLog> logs = featureService.getTaskLogs(taskId);
            return Result.success(logs);
        } catch (Exception e) {
            log.error("查询任务日志失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 健康检查接口
     * 
     * GET /api/quant/features/health
     * 
     * @return OK
     */
    @GetMapping("/health")
    public Result<Object> health() {
        return Result.success("Feature Service is running");
    }
}