package com.ningshenquantlab.alphaforge_demo1.controller;

import com.ningshenquantlab.alphaforge_demo1.common.Result;
import com.ningshenquantlab.alphaforge_demo1.executor.ExecutionResult;
import com.ningshenquantlab.alphaforge_demo1.executor.LocalPythonExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Python 执行测试控制器
 * 
 * 用途：测试 Python 执行引擎是否正常工作
 * 
 * 测试接口：
 * 1. POST /api/test/python/sync   - 同步执行测试
 * 2. POST /api/test/python/async  - 异步执行测试
 * 3. GET  /api/test/python/hello  - 简单测试（执行 python --version）
 * 
 * 注意：这是测试接口，生产环境应该删除或限制访问
 */
@Slf4j
@RestController
@RequestMapping("/api/test/python")
public class TestPythonController {
    
    @Autowired
    private LocalPythonExecutor pythonExecutor;
    
    /**
     * 测试接口：Hello World
     * 
     * 功能：执行 python --version，验证 Python 环境是否配置正确
     * 
     * 请求示例：
     * GET http://localhost:8080/api/test/python/hello
     * 
     * 返回示例：
     * {
     *   "code": 200,
     *   "message": "success",
     *   "data": {
     *     "taskId": 1,
     *     "exitCode": 0,
     *     "stdout": "Python 3.9.7",
     *     "success": true,
     *     "duration": "0秒"
     *   }
     * }
     */
    @GetMapping("/hello")
    public Result<Object> hello() {
        log.info("测试 Python 环境");
        
        try {
            // 执行 python --version
            ExecutionResult result = pythonExecutor.executeSync("--version", Arrays.asList());
            
            // 构建响应
            return Result.success("Python 环境正常", buildResponse(result));
            
        } catch (Exception e) {
            log.error("Python 环境测试失败", e);
            return Result.error("Python 环境异常：" + e.getMessage());
        }
    }
    
    /**
     * 同步执行 Python 脚本
     * 
     * 功能：测试同步执行（阻塞式）
     * 
     * 请求示例：
     * POST http://localhost:8080/api/test/python/sync
     * Content-Type: application/json
     * 
     * {
     *   "scriptName": "test.py",
     *   "args": ["arg1", "arg2"]
     * }
     * 
     * @param request 请求参数
     * @return 执行结果
     */
    @PostMapping("/sync")
    public Result<Object> executeSync(@RequestBody TestRequest request) {
        log.info("同步执行 Python 脚本，scriptName={}，args={}", 
                request.getScriptName(), request.getArgs());
        
        try {
            ExecutionResult result = pythonExecutor.executeSync(
                    request.getScriptName(), 
                    request.getArgs()
            );
            
            if (result.isSuccess()) {
                return Result.success("执行成功", buildResponse(result));
            } else {
                return Result.error("执行失败：" + result.getErrorMessage());
            }
            
        } catch (Exception e) {
            log.error("执行失败", e);
            return Result.error("执行异常：" + e.getMessage());
        }
    }
    
    /**
     * 异步执行 Python 脚本
     * 
     * 功能：测试异步执行（非阻塞式）
     * 
     * 请求示例：
     * POST http://localhost:8080/api/test/python/async
     * Content-Type: application/json
     * 
     * {
     *   "scriptName": "test.py",
     *   "args": ["arg1", "arg2"]
     * }
     * 
     * @param request 请求参数
     * @return 执行结果（立即返回，不等待脚本执行完成）
     */
    @PostMapping("/async")
    public Result<Object> executeAsync(@RequestBody TestRequest request) {
        log.info("异步执行 Python 脚本，scriptName={}，args={}", 
                request.getScriptName(), request.getArgs());
        
        try {
            CompletableFuture<ExecutionResult> future = pythonExecutor.executeAsync(
                    request.getScriptName(), 
                    request.getArgs()
            );
            
            // 等待结果（最多30秒）
            ExecutionResult result = future.get(30, java.util.concurrent.TimeUnit.SECONDS);
            
            if (result.isSuccess()) {
                return Result.success("执行成功", buildResponse(result));
            } else {
                return Result.error("执行失败：" + result.getErrorMessage());
            }
            
        } catch (java.util.concurrent.TimeoutException e) {
            log.error("执行超时", e);
            return Result.error("执行超时（30秒）");
            
        } catch (Exception e) {
            log.error("执行失败", e);
            return Result.error("执行异常：" + e.getMessage());
        }
    }
    
    /**
     * 查询正在运行的任务
     * 
     * GET http://localhost:8080/api/test/python/running
     */
    @GetMapping("/running")
    public Result<Object> getRunningTasks() {
        List<Long> taskIds = pythonExecutor.getRunningTaskIds();
        int count = pythonExecutor.getRunningTaskCount();
        
        return Result.success(java.util.Map.of(
                "count", count,
                "taskIds", taskIds
        ));
    }
    
    /**
     * 取消任务
     * 
     * DELETE http://localhost:8080/api/test/python/tasks/{taskId}
     */
    @DeleteMapping("/tasks/{taskId}")
    public Result<String> cancelTask(@PathVariable Long taskId) {
        log.info("取消任务，taskId={}", taskId);
        
        pythonExecutor.cancelTask(taskId);
        
        return Result.success("任务已取消");
    }
    
    /**
     * 构建响应数据
     */
    private Object buildResponse(ExecutionResult result) {
        return java.util.Map.of(
                "taskId", result.getTaskId(),
                "exitCode", result.getExitCode() != null ? result.getExitCode() : -1,
                "stdout", result.getStdout() != null ? result.getStdout() : "",
                "stderr", result.getStderr() != null ? result.getStderr() : "",
                "success", result.isSuccess(),
                "duration", result.getFormattedDuration(),
                "errorMessage", result.getErrorMessage() != null ? result.getErrorMessage() : ""
        );
    }
    
    /**
     * 测试请求对象
     */
    @lombok.Data
    public static class TestRequest {
        private String scriptName;
        private List<String> args;
    }
}

