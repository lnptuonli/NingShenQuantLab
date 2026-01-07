package com.ningshenquantlab.alphaforge_demo1.executor;

import com.ningshenquantlab.alphaforge_demo1.common.enums.TaskStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 本地 Python 执行器实现
 * 
 * 职责：
 * 1. 组装所有组件（CommandBuilder、ProcessManager、OutputCollector）
 * 2. 实现同步和异步执行 Python 脚本
 * 3. 管理正在运行的任务（缓存、取消、查询状态）
 * 4. 处理超时、异常等边界情况
 * 
 * 核心流程：
 * 1. 构建命令（CommandBuilder）
 * 2. 启动进程（ProcessManager）
 * 3. 收集输出（OutputCollector）
 * 4. 等待完成（ProcessManager）
 * 5. 封装结果（ExecutionResult）
 * 
 * 使用示例：
 * <pre>
 * // 同步执行
 * ExecutionResult result = executor.executeSync("feature_manager.py", 
 *     Arrays.asList("calc", "--start", "20250301"));
 * 
 * // 异步执行
 * CompletableFuture<ExecutionResult> future = executor.executeAsync("feature_manager.py", 
 *     Arrays.asList("calc", "--start", "20250301"));
 * 
 * ExecutionResult result = future.get();  // 阻塞等待结果
 * </pre>
 */
@Slf4j
@Component
public class LocalPythonExecutor implements PythonExecutor {
    
    /**
     * Python 解释器路径
     * 
     * 配置：application.yml 中的 python.interpreter
     * 示例：C:/Users/NING/.conda/envs/quant_gpu/python.exe
     */
    @Value("${python.interpreter}")
    private String pythonPath;
    
    /**
     * Python 脚本根目录
     * 
     * 配置：application.yml 中的 python.script-path
     * 示例：C:/Users/NING/PythonProject/Quantification-Researching-Platform/quantification_stock
     */
    @Value("${python.script-path}")
    private String scriptPath;
    
    /**
     * 默认超时时间（秒）
     * 
     * 配置：application.yml 中的 python.execution.timeout
     * 默认值：3600（1小时）
     */
    @Value("${python.execution.timeout:3600}")
    private int defaultTimeout;
    
    /**
     * 进程管理器（依赖注入）
     */
    @Autowired
    private ProcessManager processManager;
    
    /**
     * 输出收集器（依赖注入）
     */
    @Autowired
    private OutputCollector outputCollector;
    
    /**
     * 任务ID生成器（原子递增）
     * 
     * 说明：
     * - 每次执行任务生成唯一ID
     * - 从1开始递增
     * - 线程安全（AtomicLong）
     * 
     * 注意：
     * - 这只是临时ID，真正的任务ID应该从数据库获取
     * - 后续集成数据库时，可以改为从 task_record 表获取自增ID
     */
    private final AtomicLong taskIdGenerator = new AtomicLong(1);
    
    /**
     * 正在运行的任务缓存
     * 
     * 结构：Map<taskId, ExecutionResult>
     * 
     * 用途：
     * - 保存正在执行的任务
     * - 用于取消任务（获取 Process 对象）
     * - 用于查询任务状态
     * 
     * 注意：
     * - 使用 ConcurrentHashMap 保证线程安全
     * - 任务完成后需要从缓存中移除，避免内存泄漏
     */
    private final Map<Long, ExecutionResult> runningTasks = new ConcurrentHashMap<>();
    
    /**
     * 同步执行 Python 脚本（阻塞式）
     * 
     * 执行流程：
     * 1. 生成任务ID
     * 2. 构建完整命令
     * 3. 启动进程
     * 4. 在单独线程中收集输出
     * 5. 等待进程完成
     * 6. 封装结果并返回
     * 
     * @param scriptName 脚本名称（如：feature_manager.py）
     * @param args 参数列表（如：["calc", "--start", "20250301"]）
     * @return 执行结果
     * 
     * 注意：
     * - 该方法会阻塞，直到脚本执行完成
     * - 如果超时，会终止进程并抛出异常
     * - 适用于短时间任务（< 30秒）
     */
    @Override
    public ExecutionResult executeSync(String scriptName, List<String> args) {
        // 1. 生成任务ID
        Long taskId = taskIdGenerator.getAndIncrement();
        log.info("开始同步执行任务，taskId={}，脚本={}，参数={}", taskId, scriptName, args);
        
        // 2. 创建执行结果对象
        ExecutionResult result = ExecutionResult.builder()
                .taskId(taskId)
                .startTime(LocalDateTime.now())
                .build();
        
        // 添加到运行缓存
        runningTasks.put(taskId, result);
        
        try {
            // 3. 构建完整命令
            List<String> command = buildCommand(scriptName, args);
            result.setCommand(String.join(" ", command));
            log.debug("执行命令：{}", result.getCommand());
            
            // 4. 启动进程
            Process process = processManager.startProcess(command);
            result.setProcess(process);
            
            // 5. 在单独线程中收集输出（避免缓冲区满导致进程挂起）
            CompletableFuture<String> outputFuture = CompletableFuture.supplyAsync(() -> 
                outputCollector.collectOutput(process)
            );
            
            // 6. 等待进程完成（带超时）
            int exitCode = processManager.waitForCompletion(process, defaultTimeout);
            result.setExitCode(exitCode);
            result.setEndTime(LocalDateTime.now());
            
            // 7. 获取输出（阻塞等待输出收集完成）
            String output = outputFuture.get();
            result.setStdout(output);
            result.setStderr("");  // 已合并到 stdout
            
            // 8. 设置执行状态
            result.setSuccess(exitCode == 0);
            if (!result.isSuccess()) {
                result.setErrorMessage(result.extractErrorMessage());
            }
            
            log.info("任务执行完成，taskId={}，退出码={}，耗时={}秒", 
                    taskId, exitCode, result.getDurationSeconds());
            
            return result;
            
        } catch (TimeoutException e) {
            // 超时处理
            log.error("任务执行超时，taskId={}，超时时间={}秒", taskId, defaultTimeout);
            
            // 终止进程
            if (result.getProcess() != null) {
                processManager.terminateProcess(result.getProcess());
            }
            
            result.setEndTime(LocalDateTime.now());
            result.setExitCode(143);  // SIGTERM
            result.setSuccess(false);
            result.setErrorMessage(String.format("任务执行超时（%d秒）", defaultTimeout));
            
            return result;
            
        } catch (Exception e) {
            // 异常处理
            log.error("任务执行失败，taskId={}", taskId, e);
            
            result.setEndTime(LocalDateTime.now());
            result.setExitCode(-1);
            result.setSuccess(false);
            result.setStderr(e.getMessage());
            result.setErrorMessage("任务执行异常：" + e.getMessage());
            
            return result;
            
        } finally {
            // 清理缓存
            runningTasks.remove(taskId);
        }
    }
    
    /**
     * 异步执行 Python 脚本（非阻塞式）
     * 
     * 执行流程：
     * 1. 立即返回 CompletableFuture
     * 2. 在后台线程池中执行脚本
     * 3. 调用方可以通过 future.get() 等待结果
     * 
     * @param scriptName 脚本名称
     * @param args 参数列表
     * @return CompletableFuture<ExecutionResult>
     * 
     * 使用示例：
     * <pre>
     * // 异步执行
     * CompletableFuture<ExecutionResult> future = executor.executeAsync("script.py", args);
     * 
     * // 做其他事情...
     * 
     * // 等待结果
     * ExecutionResult result = future.get();  // 阻塞等待
     * 
     * // 或者设置回调
     * future.thenAccept(result -> {
     *     if (result.isSuccess()) {
     *         System.out.println("任务成功完成");
     *     }
     * });
     * </pre>
     * 
     * 注意：
     * - 使用 @Async 注解，在 Spring 线程池中执行
     * - 需要在配置类中开启 @EnableAsync
     * - 适用于长时间运行的任务（特征计算、模型训练）
     */
    @Async("quantTaskExecutor")  // 使用自定义线程池
    @Override
    public CompletableFuture<ExecutionResult> executeAsync(String scriptName, List<String> args) {
        log.info("开始异步执行任务，脚本={}，参数={}", scriptName, args);
        
        // 直接调用同步执行方法，Spring 会在异步线程中执行
        ExecutionResult result = executeSync(scriptName, args);
        
        // 包装成 CompletableFuture 返回
        return CompletableFuture.completedFuture(result);
    }
    
    /**
     * 取消正在执行的任务
     * 
     * 功能：
     * 1. 从缓存中查找任务
     * 2. 获取 Process 对象
     * 3. 终止进程（优雅 + 强制）
     * 4. 更新任务状态
     * 
     * @param taskId 任务ID
     */
    @Override
    public void cancelTask(Long taskId) {
        log.info("开始取消任务，taskId={}", taskId);
        
        // 从缓存中获取任务
        ExecutionResult result = runningTasks.get(taskId);
        
        if (result == null) {
            log.warn("任务不存在或已完成，taskId={}", taskId);
            return;
        }
        
        // 检查任务是否正在运行
        if (!result.isRunning()) {
            log.warn("任务已结束，无需取消，taskId={}", taskId);
            return;
        }
        
        // 获取进程对象
        Process process = result.getProcess();
        if (process == null || !process.isAlive()) {
            log.warn("进程不存在或已结束，taskId={}", taskId);
            return;
        }
        
        // 终止进程
        boolean terminated = processManager.terminateProcess(process);
        
        if (terminated) {
            log.info("任务已取消，taskId={}", taskId);
            
            // 更新任务状态
            result.setEndTime(LocalDateTime.now());
            result.setExitCode(130);  // SIGINT
            result.setSuccess(false);
            result.setErrorMessage("任务被用户取消");
            
            // 从缓存中移除
            runningTasks.remove(taskId);
            
        } else {
            log.error("无法取消任务，taskId={}", taskId);
        }
    }
    
    /**
     * 获取任务状态
     * 
     * 功能：
     * 1. 从缓存中查找任务
     * 2. 根据 ExecutionResult 判断状态
     * 3. 返回状态枚举
     * 
     * @param taskId 任务ID
     * @return 任务状态枚举
     */
    @Override
    public TaskStatusEnum getTaskStatus(Long taskId) {
        ExecutionResult result = runningTasks.get(taskId);
        
        if (result == null) {
            // 任务不存在，可能已完成并从缓存中移除
            log.debug("任务不在运行缓存中，taskId={}", taskId);
            return null;
        }
        
        // 使用 ExecutionResult 的方法判断状态
        return result.getTaskStatus();
    }
    
    /**
     * 构建完整的命令行
     * 
     * 功能：
     * 1. 拼接 Python 解释器路径
     * 2. 拼接脚本完整路径
     * 3. 拼接参数列表
     * 
     * @param scriptName 脚本名称（如：feature_manager.py）
     * @param args 参数列表（如：["calc", "--start", "20250301"]）
     * @return 完整命令列表
     * 
     * 示例：
     * 输入：scriptName="feature_manager.py", args=["calc", "--start", "20250301"]
     * 输出：["C:/Python/python.exe", "C:/Scripts/feature_manager.py", "calc", "--start", "20250301"]
     * 
     * 注意：
     * - CommandBuilder 目前只有 buildFeatureCalcCommand() 方法
     * - 这里实现通用的命令构建逻辑
     * - 未来可以扩展 CommandBuilder，支持更多脚本类型
     */
    private List<String> buildCommand(String scriptName, List<String> args) {
        // 创建命令列表
        List<String> command = new java.util.ArrayList<>();
        
        // 添加 Python 解释器路径
        command.add(pythonPath);
        
        // 添加脚本完整路径
        String scriptFullPath = Paths.get(scriptPath, scriptName).toString();
        command.add(scriptFullPath);
        
        // 添加参数列表
        if (args != null && !args.isEmpty()) {
            command.addAll(args);
        }
        
        return command;
    }
    
    /**
     * 获取所有正在运行的任务ID列表
     * 
     * 用途：
     * - 监控当前运行的任务
     * - 应用关闭时清理所有任务
     * 
     * @return 任务ID列表
     */
    public List<Long> getRunningTaskIds() {
        return new java.util.ArrayList<>(runningTasks.keySet());
    }
    
    /**
     * 获取正在运行的任务数量
     * 
     * @return 任务数量
     */
    public int getRunningTaskCount() {
        return runningTasks.size();
    }
    
    /**
     * 取消所有正在运行的任务
     * 
     * 用途：
     * - 应用关闭时清理所有任务
     * - 紧急停止所有任务
     * 
     * @return 取消的任务数量
     */
    public int cancelAllTasks() {
        log.warn("开始取消所有正在运行的任务，共 {} 个", runningTasks.size());
        
        int count = 0;
        for (Long taskId : getRunningTaskIds()) {
            try {
                cancelTask(taskId);
                count++;
            } catch (Exception e) {
                log.error("取消任务失败，taskId={}", taskId, e);
            }
        }
        
        log.info("已取消 {} 个任务", count);
        return count;
    }
}
