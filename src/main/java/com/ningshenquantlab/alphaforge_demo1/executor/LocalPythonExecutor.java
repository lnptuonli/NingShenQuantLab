package com.ningshenquantlab.alphaforge_demo1.executor;
//本地 Python 执行器实现
public class LocalPythonExecutor implements PythonExecutor{
    @Override
    public ExecutionResult executeSync(String scriptName, List<String> args) {
        return null;
    }

    @Override
    public CompletableFuture<ExecutionResult> executeAsync(String scriptName, List<String> args) {
        return null;
    }

    @Override
    public void cancelTask(Long taskId) {

    }

    @Override
    public TaskStatus getTaskStatus(Long taskId) {
        return null;
    }
}
