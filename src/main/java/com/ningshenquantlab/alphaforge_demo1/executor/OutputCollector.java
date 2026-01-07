package com.ningshenquantlab.alphaforge_demo1.executor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 进程输出收集器
 * 
 * 职责：
 * 1. 实时读取进程的标准输出（stdout）和错误输出（stderr）
 * 2. 将输出转换为字符串，供后续处理
 * 3. 处理大量输出时的缓冲区管理
 * 4. 防止缓冲区满导致进程挂起
 * 
 * 核心方法：
 * - collectOutput(process)      - 收集进程的所有输出
 * - collectStream(inputStream)  - 收集单个输入流的输出
 * 
 * 使用示例：
 * <pre>
 * Process process = processManager.startProcess(command);
 * 
 * // 在单独线程中收集输出（避免阻塞）
 * CompletableFuture<String> outputFuture = CompletableFuture.supplyAsync(() -> {
 *     return outputCollector.collectOutput(process);
 * });
 * 
 * // 等待进程完成
 * int exitCode = process.waitFor();
 * 
 * // 获取输出
 * String output = outputFuture.get();
 * </pre>
 * 
 * 重要说明：
 * - 必须在进程启动后立即开始收集输出
 * - 如果不及时读取，缓冲区满会导致进程挂起（deadlock）
 * - 建议在单独线程中收集输出
 */
@Slf4j
@Component
public class OutputCollector {
    
    /**
     * 单次读取的最大字符数
     * 
     * 说明：
     * - 缓冲区大小，每次最多读取 8192 字符
     * - 太小：频繁读取，影响性能
     * - 太大：内存占用高
     * - 8KB 是常见的缓冲区大小
     */
    private static final int BUFFER_SIZE = 8192;
    
    /**
     * 输出内容的最大长度（字符数）
     * 
     * 说明：
     * - 限制总输出长度，防止内存溢出
     * - 1MB = 1,048,576 字符（约等于 1MB）
     * - 超过限制后只保留前面的内容
     * 
     * 为什么需要限制？
     * - Python 脚本可能输出大量日志（如：打印每条数据）
     * - 无限制会导致内存溢出（OOM）
     * - 完整日志应该写入文件，数据库只存摘要
     */
    private static final int MAX_OUTPUT_LENGTH = 1024 * 1024;  // 1MB
    
    /**
     * 收集进程的所有输出
     * 
     * 功能：
     * 1. 读取进程的标准输出流（stdout）
     * 2. 实时收集，直到进程结束
     * 3. 返回完整的输出字符串
     * 
     * @param process 进程对象
     * @return 进程的所有输出内容
     * 
     * 注意事项：
     * - 该方法会阻塞，直到进程结束或输出流关闭
     * - 建议在单独线程中调用
     * - 如果使用 ProcessBuilder.redirectErrorStream(true)，
     *   stderr 会合并到 stdout，只需读取一个流
     */
    public String collectOutput(Process process) {
        if (process == null) {
            log.warn("进程对象为空，无法收集输出");
            return "";
        }
        
        log.debug("开始收集进程输出");
        
        // 获取进程的输入流（实际上是进程的标准输出）
        // 这里的命名有点反直觉：
        // - process.getInputStream() 获取的是进程的 stdout
        // - process.getErrorStream() 获取的是进程的 stderr
        // - process.getOutputStream() 是向进程的 stdin 写入
        InputStream inputStream = process.getInputStream();
        
        String output = collectStream(inputStream);
        
        log.debug("输出收集完成，共 {} 字符", output.length());
        
        return output;
    }
    
    /**
     * 收集错误输出（stderr）
     * 
     * 说明：
     * - 如果使用了 redirectErrorStream(true)，stderr 已经合并到 stdout，无需单独收集
     * - 如果没有合并，需要单独收集 stderr
     * 
     * @param process 进程对象
     * @return 错误输出内容
     */
    public String collectErrorOutput(Process process) {
        if (process == null) {
            log.warn("进程对象为空，无法收集错误输出");
            return "";
        }
        
        log.debug("开始收集进程错误输出");
        
        InputStream errorStream = process.getErrorStream();
        String errorOutput = collectStream(errorStream);
        
        log.debug("错误输出收集完成，共 {} 字符", errorOutput.length());
        
        return errorOutput;
    }
    
    /**
     * 收集输入流的所有内容
     * 
     * 实现细节：
     * 1. 使用 BufferedReader 按行读取
     * 2. 使用 StringBuilder 拼接所有行
     * 3. 限制总长度，防止内存溢出
     * 4. 使用 UTF-8 编码
     * 
     * @param inputStream 输入流（可能是 stdout 或 stderr）
     * @return 流的所有内容
     * 
     * 为什么按行读取？
     * - Python 的 print() 输出是按行的
     * - 方便处理日志（每行一条日志）
     * - 保留换行符，便于前端展示
     * 
     * 为什么使用 UTF-8？
     * - Python 脚本可能输出中文
     * - UTF-8 是通用编码，兼容性好
     * - 避免乱码问题
     */
    private String collectStream(InputStream inputStream) {
        StringBuilder output = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8), BUFFER_SIZE)) {
            
            String line;
            int totalLength = 0;
            
            // 按行读取
            while ((line = reader.readLine()) != null) {
                
                // 检查是否超过最大长度
                if (totalLength + line.length() > MAX_OUTPUT_LENGTH) {
                    log.warn("输出超过最大长度限制（{}字符），将截断", MAX_OUTPUT_LENGTH);
                    output.append("\n... [输出过长，已截断] ...");
                    break;
                }
                
                // 拼接行（保留换行符）
                if (output.length() > 0) {
                    output.append("\n");
                }
                output.append(line);
                
                totalLength += line.length() + 1;  // +1 for newline
                
                // 可选：实时打印日志（用于调试）
                // log.trace("进程输出：{}", line);
            }
            
        } catch (IOException e) {
            log.error("读取进程输出时发生错误", e);
            output.append("\n[ERROR] 读取输出失败：").append(e.getMessage());
        }
        
        return output.toString();
    }
    
    /**
     * 收集输出并实时回调（高级用法）
     * 
     * 功能：
     * - 边读取边处理（如：实时写入数据库、推送到前端）
     * - 不等到全部读取完才处理
     * 
     * @param inputStream 输入流
     * @param lineCallback 每读取一行就调用的回调函数
     * @return 完整输出
     * 
     * 使用场景：
     * - WebSocket 实时推送日志到前端
     * - 每读取一行就写入数据库
     * - 监控关键字（如："ERROR"、"成功"）
     */
    public String collectStreamWithCallback(InputStream inputStream, LineCallback lineCallback) {
        StringBuilder output = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8), BUFFER_SIZE)) {
            
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // 拼接输出
                if (output.length() > 0) {
                    output.append("\n");
                }
                output.append(line);
                
                // 调用回调函数
                if (lineCallback != null) {
                    try {
                        lineCallback.onLine(lineNumber, line);
                    } catch (Exception e) {
                        log.error("回调函数执行失败，行号：{}，内容：{}", lineNumber, line, e);
                    }
                }
            }
            
        } catch (IOException e) {
            log.error("读取输出时发生错误", e);
        }
        
        return output.toString();
    }
    
    /**
     * 行回调接口
     * 
     * 用于实时处理每一行输出
     */
    @FunctionalInterface
    public interface LineCallback {
        /**
         * 每读取一行时调用
         * 
         * @param lineNumber 行号（从1开始）
         * @param line 行内容
         */
        void onLine(int lineNumber, String line);
    }
}
