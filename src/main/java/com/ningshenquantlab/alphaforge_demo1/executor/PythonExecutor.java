package com.ningshenquantlab.alphaforge_demo1.executor;
//Python 执行器接口
/**
 * Python 脚本执行器接口
 */
public interface PythonExecutor {

    /**
     * 同步执行 Python 脚本
     * @param scriptName 脚本名称
     * @param args 参数列表
     * @return 执行结果
     */
    ExecutionResult executeSync(String scriptName, List<String> args);

    /**
     * 异步执行 Python 脚本
     * @param scriptName 脚本名称
     * @param args 参数列表
     * @return 任务ID
     */
    CompletableFuture<ExecutionResult> executeAsync(String scriptName, List<String> args);

    /**
     * 取消正在执行的任务
     * @param taskId 任务ID
     */
    void cancelTask(Long taskId);

    /**
     * 获取任务状态
     * @param taskId 任务ID
     * @return 任务状态
     */
    TaskStatus getTaskStatus(Long taskId);
}
