package com.ningshenquantlab.alphaforge_demo1.executor;

import com.ningshenquantlab.alphaforge_demo1.common.enums.TaskStatusEnum;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Python 脚本执行器接口
 * 
 * 定义 Python 脚本执行的核心能力：
 * - 同步执行：阻塞等待脚本完成
 * - 异步执行：立即返回，后台执行
 * - 任务管理：取消任务、查询状态
 */
public interface PythonExecutor {

    /**
     * 同步执行 Python 脚本（阻塞式）
     * 
     * 说明：
     * - 方法会阻塞，直到脚本执行完成
     * - 适用于执行时间短的任务（< 30秒）
     * - 不适合长时间运行的任务（会阻塞线程）
     * 
     * @param scriptName 脚本名称（如：feature_manager.py）
     * @param args 参数列表（如：["calc", "--start", "20250301"]）
     * @return 执行结果（包含退出码、输出、错误信息等）
     */
    ExecutionResult executeSync(String scriptName, List<String> args);

    /**
     * 异步执行 Python 脚本（非阻塞式）
     * 
     * 说明：
     * - 方法立即返回 CompletableFuture
     * - 脚本在后台线程中执行
     * - 适用于长时间运行的任务（特征计算、模型训练）
     * 
     * @param scriptName 脚本名称
     * @param args 参数列表
     * @return CompletableFuture<ExecutionResult>，可以通过 .get() 等待结果
     */
    CompletableFuture<ExecutionResult> executeAsync(String scriptName, List<String> args);

    /**
     * 取消正在执行的任务
     * 
     * 说明：
     * - 终止正在运行的 Python 进程
     * - 先尝试优雅终止，再强制终止
     * 
     * @param taskId 任务ID
     */
    void cancelTask(Long taskId);

    /**
     * 获取任务状态
     * 
     * @param taskId 任务ID
     * @return 任务状态枚举（PENDING/RUNNING/SUCCESS/FAILED等）
     */
    TaskStatusEnum getTaskStatus(Long taskId);
}
