package com.ningshenquantlab.alphaforge_demo1.quant.controller;
//特征计算 API

import com.ningshenquantlab.alphaforge_demo1.common.Result;
import com.ningshenquantlab.alphaforge_demo1.quant.dto.FeatureCalcRequest;
import com.ningshenquantlab.alphaforge_demo1.quant.dto.TaskResponse;
import com.ningshenquantlab.alphaforge_demo1.quant.entity.TaskLog;
import com.ningshenquantlab.alphaforge_demo1.quant.service.FeatureService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 特征计算 Controller
 */
@RestController
@RequestMapping("/api/v1/features")
public class FeatureController {

    private final FeatureService featureService;

    /**
     * 计算特征
     * POST /api/v1/features/calculate
     * Body: {
     *   "startDate": "20250301",
     *   "endDate": "20250305",
     *   "force": false,
     *   "async": true
     * }
     */
    @PostMapping("/calculate")
    public Result<TaskResponse> calculateFeatures(
            @RequestBody @Validated FeatureCalcRequest request) {
        TaskResponse task = featureService.calculateFeatures(request);
        return Result.success(task);
    }

    /**
     * 查询任务状态
     * GET /api/v1/features/tasks/{taskId}
     */
    @GetMapping("/tasks/{taskId}")
    public Result<TaskResponse> getTaskStatus(@PathVariable Long taskId) {
        TaskResponse task = featureService.getTaskStatus(taskId);
        return Result.success(task);
    }

    /**
     * 获取任务日志
     * GET /api/v1/features/tasks/{taskId}/logs
     */
    @GetMapping("/tasks/{taskId}/logs")
    public Result<List<TaskLog>> getTaskLogs(@PathVariable Long taskId) {
        List<TaskLog> logs = featureService.getTaskLogs(taskId);
        return Result.success(logs);
    }

    /**
     * 取消任务
     * POST /api/v1/features/tasks/{taskId}/cancel
     */
    @PostMapping("/tasks/{taskId}/cancel")
    public Result<Void> cancelTask(@PathVariable Long taskId) {
        featureService.cancelTask(taskId);
        return Result.success();
    }
}