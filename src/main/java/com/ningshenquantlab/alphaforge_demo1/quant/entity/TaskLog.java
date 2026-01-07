package com.ningshenquantlab.alphaforge_demo1.quant.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 任务日志实体
 * 
 * 对应数据库表：task_log
 * 
 * 用途：
 * - 记录任务执行过程中的详细日志
 * - 存储 Python 脚本的 stdout 和 stderr 输出
 * - 支持日志分级查询和分析
 * 
 * 日志类型：
 * - INFO：正常信息日志
 * - WARN：警告日志
 * - ERROR：错误日志
 * - DEBUG：调试日志
 * - STDOUT：Python 标准输出
 * - STDERR：Python 错误输出
 * 
 * 注意事项：
 * - 与 TaskRecord 是一对多关系（一个任务有多条日志）
 * - logContent 可能很大，建议限制单条日志大小（如 10KB）
 * - 生产环境可考虑使用 ELK 等日志系统替代数据库存储
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskLog {
    
    /**
     * 日志ID（主键，自增）
     */
    private Long id;
    
    /**
     * 关联的任务ID
     * 
     * 说明：对应 TaskRecord 的 id
     * 用途：将日志与任务关联，支持按任务查询日志
     * 
     * 外键约束：FOREIGN KEY (task_id) REFERENCES task_record(id)
     */
    private Long taskId;
    
    /**
     * 日志级别
     * 
     * 说明：日志的严重程度
     * 可选值：
     * - INFO：一般信息
     * - WARN：警告信息
     * - ERROR：错误信息
     * - DEBUG：调试信息
     * - STDOUT：Python 标准输出
     * - STDERR：Python 错误输出
     * 
     * 用途：
     * - 前端可按级别筛选日志
     * - 监控系统可针对 ERROR 级别发送告警
     */
    private String logLevel;
    
    /**
     * 日志内容
     * 
     * 说明：具体的日志信息
     * 示例：
     * - "[INFO] 开始获取数据，时间范围：20240101-20240201"
     * - "[ERROR] 数据库连接失败：Connection timeout"
     * - "[STDOUT] 成功获取 1000 条数据"
     * 
     * 注意：
     * - 建议限制单条日志大小（如 10KB）
     * - 过大的输出可以分批插入多条日志
     * - 数据库字段类型建议使用 TEXT 或 LONGTEXT
     */
    private String logContent;
    
    /**
     * 日志来源
     * 
     * 说明：日志的产生来源
     * 可选值：
     * - SYSTEM：系统日志（如任务状态变更）
     * - PYTHON：Python 脚本输出
     * - EXECUTOR：执行器日志（如进程启动、终止）
     * 
     * 用途：区分不同来源的日志，便于排查问题
     */
    private String logSource;
    
    /**
     * 日志创建时间
     * 
     * 说明：日志产生的时间
     * 默认值：当前时间戳
     * 
     * 用途：
     * - 按时间顺序查看日志
     * - 分析任务执行的时间线
     */
    private LocalDateTime createTime;
    
    /**
     * 日志序号（可选）
     * 
     * 说明：同一任务内的日志序号，从 1 开始递增
     * 用途：确保日志按顺序展示，即使 createTime 相同
     */
    private Integer logSequence;
    
    // ==================== 实用方法 ====================
    
    /**
     * 判断是否为错误日志
     */
    public boolean isError() {
        return "ERROR".equals(logLevel) || "STDERR".equals(logLevel);
    }
    
    /**
     * 判断是否为警告日志
     */
    public boolean isWarning() {
        return "WARN".equals(logLevel);
    }
    
    /**
     * 判断是否为系统日志
     */
    public boolean isSystemLog() {
        return "SYSTEM".equals(logSource);
    }
    
    /**
     * 判断是否为 Python 输出
     */
    public boolean isPythonOutput() {
        return "STDOUT".equals(logLevel) || "STDERR".equals(logLevel);
    }
    
    /**
     * 获取日志内容摘要（前 200 个字符）
     */
    public String getLogContentSummary() {
        if (logContent == null || logContent.isEmpty()) {
            return "";
        }
        return logContent.length() > 200 
            ? logContent.substring(0, 200) + "..." 
            : logContent;
    }
}
