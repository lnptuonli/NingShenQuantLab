package com.ningshenquantlab.alphaforge_demo1.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Python 进程管理器
 * 
 * 职责：
 * 1. 启动 Python 进程（使用 ProcessBuilder）
 * 2. 管理进程生命周期（等待、超时、终止）
 * 3. 检查进程状态（是否存活、退出码）
 * 4. 处理进程异常（超时、强制终止）
 * 
 * 核心方法：
 * - startProcess(command)        - 启动进程
 * - waitForCompletion(timeout)   - 等待进程完成（带超时）
 * - terminateProcess(process)    - 终止进程（优雅 + 强制）
 * - isProcessAlive(process)      - 检查进程是否存活
 * 
 * 使用示例：
 * <pre>
 * ProcessManager manager = new ProcessManager();
 * 
 * // 1. 构建命令
 * List<String> command = Arrays.asList("python", "script.py", "--arg", "value");
 * 
 * // 2. 启动进程
 * Process process = manager.startProcess(command);
 * 
 * // 3. 等待完成（带超时）
 * try {
 *     int exitCode = manager.waitForCompletion(process, 3600); // 1小时超时
 *     System.out.println("执行成功，退出码：" + exitCode);
 * } catch (TimeoutException e) {
 *     // 超时，强制终止
 *     manager.terminateProcess(process);
 * }
 * </pre>
 * 
 * 注意事项：
 * - Windows 和 Linux 进程终止方式不同
 * - 超时后需要强制终止进程
 * - 工作目录必须正确，否则相对路径会失败
 * - 输出流必须及时读取，否则缓冲区满会导致进程挂起
 */
@Slf4j
@Component
public class ProcessManager {
    
    /**
     * Python 脚本根目录
     * 
     * 说明：所有 Python 脚本的所在目录
     * 用途：设置为进程的工作目录（working directory）
     * 
     * 配置：application.yml 中的 python.script-path
     * 示例：C:/Users/NING/PythonProject/Quantification-Researching-Platform/quantification_stock
     * 
     * 注意：
     * - 必须是绝对路径
     * - 路径分隔符：Windows 用 \ 或 /，Linux 用 /
     * - 如果脚本使用相对路径导入模块，工作目录必须正确
     */
    @Value("${python.script-path}")
    private String scriptPath;
    
    /**
     * 启动 Python 进程
     * 
     * 功能：
     * 1. 使用 ProcessBuilder 启动进程
     * 2. 设置工作目录为脚本根目录
     * 3. 合并标准错误流到标准输出流
     * 4. 启动进程并返回 Process 对象
     * 
     * @param command 完整的命令行参数列表
     *                示例：["python", "C:\Scripts\feature_manager.py", "calc", "--start", "20250301"]
     * @return Process 对象，用于后续管理（等待、终止）
     * @throws IOException 进程启动失败时抛出（如：Python 路径错误、权限不足）
     * 
     * 实现细节：
     * - ProcessBuilder.directory()：设置工作目录
     * - ProcessBuilder.redirectErrorStream(true)：合并 stderr 到 stdout
     * - Process.start()：启动进程
     * 
     * 为什么要合并错误流？
     * - Python 的日志可能同时输出到 stdout 和 stderr
     * - 合并后只需要读取一个流，简化代码
     * - 避免 stderr 缓冲区满导致进程挂起
     * 
     * 为什么要设置工作目录？
     * - Python 脚本可能使用相对路径导入模块
     * - 脚本可能读写相对路径的文件
     * - 工作目录错误会导致 "ModuleNotFoundError" 或 "FileNotFoundError"
     */
    public Process startProcess(List<String> command) throws IOException {
        log.info("启动 Python 进程，命令：{}", String.join(" ", command));
        
        // 1. 创建 ProcessBuilder
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        
        // 2. 设置工作目录（重要！）
        // 将工作目录设置为脚本所在目录，避免相对路径问题
        File workingDir = new File(scriptPath);
        if (!workingDir.exists() || !workingDir.isDirectory()) {
            log.error("工作目录不存在或不是目录：{}", scriptPath);
            throw new IOException("工作目录不存在：" + scriptPath);
        }
        processBuilder.directory(workingDir);
        log.debug("设置工作目录：{}", scriptPath);
        
        // 3. 合并错误流到标准输出流
        // 这样只需要读取一个流（stdout），简化日志收集逻辑
        processBuilder.redirectErrorStream(true);
        
        // 4. 启动进程
        Process process = processBuilder.start();
        log.info("进程已启动，PID：{}", getProcessId(process));
        
        return process;
    }
    
    /**
     * 等待进程完成（带超时控制）
     * 
     * 功能：
     * 1. 等待进程执行完成
     * 2. 如果超时，抛出 TimeoutException
     * 3. 返回进程的退出码
     * 
     * @param process 要等待的进程对象
     * @param timeoutSeconds 超时时间（秒），0 表示无限等待
     * @return 进程的退出码（0=成功，非0=失败）
     * @throws InterruptedException 等待过程中线程被中断
     * @throws TimeoutException 等待超时
     * 
     * 退出码说明：
     * - 0：成功
     * - 1：一般性错误
     * - 2：参数错误
     * - 130：用户取消（Ctrl+C）
     * - 137：被 kill -9 强制终止
     * - 143：被 kill -15 正常终止
     * 
     * 超时处理：
     * - 如果超时，抛出 TimeoutException
     * - 调用方应该捕获异常并调用 terminateProcess() 强制终止进程
     * 
     * 注意事项：
     * - 必须先读取进程的输出流，否则缓冲区满会导致进程挂起
     * - 建议在另一个线程中读取输出，然后在主线程中等待进程完成
     * - 如果 timeoutSeconds=0，表示无限等待（不推荐，可能导致死锁）
     */
    public int waitForCompletion(Process process, int timeoutSeconds) 
            throws InterruptedException, TimeoutException {
        
        log.info("等待进程完成，超时时间：{}秒", timeoutSeconds);
        
        boolean finished;
        
        if (timeoutSeconds <= 0) {
            // 无超时限制，一直等待
            log.warn("未设置超时时间，将无限等待进程完成（不推荐）");
            int exitCode = process.waitFor();
            log.info("进程执行完成，退出码：{}", exitCode);
            return exitCode;
        }
        
        // 带超时的等待
        finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
        
        if (!finished) {
            // 超时了，进程还在运行
            log.error("进程执行超时（{}秒），进程仍在运行", timeoutSeconds);
            throw new TimeoutException("进程执行超时（" + timeoutSeconds + "秒）");
        }
        
        // 正常完成，获取退出码
        int exitCode = process.exitValue();
        log.info("进程执行完成，退出码：{}", exitCode);
        
        return exitCode;
    }
    
    /**
     * 终止进程（优雅终止 + 强制终止）
     * 
     * 功能：
     * 1. 先尝试优雅终止（destroy）
     * 2. 等待 2 秒
     * 3. 如果还没终止，强制终止（destroyForcibly）
     * 
     * @param process 要终止的进程对象
     * @return true-成功终止，false-进程已经结束
     * 
     * 两种终止方式：
     * 
     * 1. 优雅终止（destroy）
     *    - Windows：发送 Ctrl+C 信号
     *    - Linux：发送 SIGTERM（15）信号
     *    - 进程有机会清理资源（关闭文件、释放锁）
     *    - 进程可以捕获信号并拒绝退出
     * 
     * 2. 强制终止（destroyForcibly）
     *    - Windows：调用 TerminateProcess
     *    - Linux：发送 SIGKILL（9）信号
     *    - 进程无法捕获，必定终止
     *    - 可能导致资源泄漏（文件未关闭、锁未释放）
     * 
     * 为什么要分两步？
     * - 优先尝试优雅终止，给进程清理资源的机会
     * - 如果优雅终止失败，再强制终止，确保进程一定被杀死
     * - 避免僵尸进程（zombie process）
     * 
     * 使用场景：
     * - 用户取消任务
     * - 任务执行超时
     * - 应用关闭时清理所有运行中的任务
     */
    public boolean terminateProcess(Process process) {
        if (process == null) {
            log.warn("进程对象为空，无需终止");
            return false;
        }
        
        if (!process.isAlive()) {
            log.info("进程已经结束，无需终止");
            return false;
        }
        
        log.info("开始终止进程，PID：{}", getProcessId(process));
        
        try {
            // 第一步：优雅终止
            log.debug("尝试优雅终止进程...");
            process.destroy();
            
            // 等待 2 秒，看进程是否终止
            boolean terminated = process.waitFor(2, TimeUnit.SECONDS);
            
            if (terminated) {
                log.info("进程已优雅终止，退出码：{}", process.exitValue());
                return true;
            }
            
            // 第二步：强制终止
            log.warn("优雅终止失败，开始强制终止进程...");
            process.destroyForcibly();
            
            // 再等待 2 秒
            terminated = process.waitFor(2, TimeUnit.SECONDS);
            
            if (terminated) {
                log.info("进程已强制终止，退出码：{}", process.exitValue());
                return true;
            }
            
            // 强制终止也失败（极少情况）
            log.error("无法终止进程，可能需要手动处理，PID：{}", getProcessId(process));
            return false;
            
        } catch (InterruptedException e) {
            log.error("终止进程时被中断", e);
            Thread.currentThread().interrupt();
            return false;
        }
    }
    
    /**
     * 检查进程是否存活
     * 
     * @param process 进程对象
     * @return true-进程正在运行，false-进程已结束
     */
    public boolean isProcessAlive(Process process) {
        if (process == null) {
            return false;
        }
        return process.isAlive();
    }
    
    /**
     * 获取进程的退出码
     * 
     * 注意：只有在进程已结束时才能调用，否则会抛出 IllegalThreadStateException
     * 
     * @param process 进程对象
     * @return 退出码，如果进程还在运行返回 null
     */
    public Integer getExitCode(Process process) {
        if (process == null || process.isAlive()) {
            return null;
        }
        return process.exitValue();
    }
    
    /**
     * 获取进程ID（PID）
     * 
     * 说明：Java 9+ 提供了 Process.pid() 方法
     * 用途：日志记录、进程监控
     * 
     * @param process 进程对象
     * @return 进程ID，如果无法获取返回 -1
     */
    public long getProcessId(Process process) {
        if (process == null) {
            return -1;
        }
        
        try {
            // Java 9+ 新增的方法
            return process.pid();
        } catch (Exception e) {
            // 兼容 Java 8（虽然项目用的是 Java 17）
            log.debug("无法获取进程ID（需要 Java 9+）");
            return -1;
        }
    }
    
    /**
     * 销毁所有子进程（递归）
     * 
     * 说明：
     * - Python 脚本可能会启动子进程
     * - 只终止父进程可能导致子进程变成僵尸进程
     * - 需要递归终止所有子孙进程
     * 
     * @param process 父进程对象
     * @return 终止的进程数量
     */
    public int destroyProcessTree(Process process) {
        if (process == null || !process.isAlive()) {
            return 0;
        }
        
        int count = 0;
        
        try {
            // Java 9+ 提供了 descendants() 方法获取所有子孙进程
            var descendants = process.descendants();
            
            // 先终止所有子进程
            descendants.forEach(p -> {
                log.debug("终止子进程，PID：{}", p.pid());
                p.destroy();
            });
            
            count = (int) descendants.count();
            
            // 最后终止父进程
            process.destroy();
            count++;
            
            log.info("已终止进程树，共 {} 个进程", count);
            
        } catch (Exception e) {
            log.error("终止进程树失败", e);
            // 降级方案：只终止父进程
            process.destroy();
            count = 1;
        }
        
        return count;
    }
}

