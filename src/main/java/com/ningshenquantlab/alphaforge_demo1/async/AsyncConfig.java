package com.ningshenquantlab.alphaforge_demo1.async;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置
 * 
 * 功能：
 * 1. 开启 Spring 异步支持（@EnableAsync）
 * 2. 配置自定义线程池（用于执行 Python 任务）
 * 3. 设置线程池参数（核心线程数、最大线程数、队列容量等）
 * 
 * 使用场景：
 * - LocalPythonExecutor.executeAsync() 使用该线程池执行任务
 * - 避免阻塞主线程
 * - 支持并发执行多个 Python 脚本
 * 
 * 注意事项：
 * - 线程池参数需要根据实际情况调整
 * - 核心线程数不宜过大（占用内存）
 * - 队列容量不宜过小（任务会被拒绝）
 */
@Slf4j
@Configuration
@EnableAsync(proxyTargetClass = true)  // 开启异步支持，强制使用 CGLIB 代理（解决 JDK 动态代理注入问题）
public class AsyncConfig {
    
    /**
     * 核心线程数
     * 
     * 说明：
     * - 线程池初始化时创建的线程数
     * - 即使线程空闲也不会回收
     * - 建议值：CPU 核心数
     * 
     * 配置：application.yml 中的 async.executor.core-pool-size
     * 默认值：5
     */
    @Value("${async.executor.core-pool-size:5}")
    private int corePoolSize;
    
    /**
     * 最大线程数
     * 
     * 说明：
     * - 线程池最多可以创建的线程数
     * - 超过核心线程数时，如果队列满了，会创建新线程
     * - 建议值：CPU 核心数 * 2
     * 
     * 配置：application.yml 中的 async.executor.max-pool-size
     * 默认值：10
     */
    @Value("${async.executor.max-pool-size:10}")
    private int maxPoolSize;
    
    /**
     * 队列容量
     * 
     * 说明：
     * - 任务队列的最大容量
     * - 当核心线程都在忙时，新任务会放入队列
     * - 队列满后才会创建新线程（直到达到最大线程数）
     * 
     * 配置：application.yml 中的 async.executor.queue-capacity
     * 默认值：100
     */
    @Value("${async.executor.queue-capacity:100}")
    private int queueCapacity;
    
    /**
     * 线程名称前缀
     * 
     * 说明：
     * - 线程名称格式：quant-async-1, quant-async-2, ...
     * - 便于日志追踪和问题排查
     * 
     * 配置：application.yml 中的 async.executor.thread-name-prefix
     * 默认值：quant-async-
     */
    @Value("${async.executor.thread-name-prefix:quant-async-}")
    private String threadNamePrefix;
    
    /**
     * 创建异步任务执行器
     * 
     * Bean 名称：quantTaskExecutor
     * 用途：LocalPythonExecutor 使用该执行器执行异步任务
     * 
     * @return 线程池任务执行器
     */
    @Bean(name = "quantTaskExecutor")
    public ThreadPoolTaskExecutor quantTaskExecutor() {
        log.info("初始化异步任务执行器，corePoolSize={}，maxPoolSize={}，queueCapacity={}", 
                corePoolSize, maxPoolSize, queueCapacity);
        
        // 创建线程池
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 1. 核心线程数
        executor.setCorePoolSize(corePoolSize);
        
        // 2. 最大线程数
        executor.setMaxPoolSize(maxPoolSize);
        
        // 3. 队列容量
        executor.setQueueCapacity(queueCapacity);
        
        // 4. 线程名称前缀
        executor.setThreadNamePrefix(threadNamePrefix);
        
        // 5. 拒绝策略：CallerRunsPolicy
        // 当线程池和队列都满时，在调用者线程中执行任务
        // 这样可以限流，避免任务丢失
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 6. 等待所有任务完成后再关闭线程池
        // 应用关闭时，等待正在执行的任务完成（最多等待60秒）
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        // 7. 初始化线程池
        executor.initialize();
        
        log.info("异步任务执行器初始化完成");
        
        return executor;
    }
}
