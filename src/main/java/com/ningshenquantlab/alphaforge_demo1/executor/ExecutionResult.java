package com.ningshenquantlab.alphaforge_demo1.executor;

import com.ningshenquantlab.alphaforge_demo1.common.enums.TaskStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.Duration;
/**
 * 建议的优化版本（你可以选择是否采纳）
 * 删除 success 字段
 * cancelled() → exitCode=null
 * stdout/stderr 限制长度
 * errorMessage 提取更智能
 * 增加一个字段：durationMillis（缓存）
 * 增加一个字段：pid（进程号）
 */
/**
 * Python 脚本执行结果封装类
 * 
 * 用途：
 * - 封装 Python 脚本执行后的所有信息
 * - 作为 PythonExecutor.executeSync() 和 executeAsync() 的返回值
 * - 提供执行状态判断、时长计算等实用方法
 * 
 * 生命周期：
 * 1. 执行前：创建对象，设置 taskId、startTime
 * 2. 执行中：实时收集 stdout、stderr
 * 3. 执行后：设置 exitCode、endTime，计算 success 状态
 * 
 * 使用示例：
 * <pre>
 * ExecutionResult result = ExecutionResult.builder()
 *     .taskId(12345L)
 *     .exitCode(0)
 *     .stdout("计算完成，特征数量：1024")
 *     .stderr("")
 *     .startTime(LocalDateTime.now().minusMinutes(5))
 *     .endTime(LocalDateTime.now())
 *     .build();
 * 
 * if (result.isSuccess()) {
 *     System.out.println("执行成功，耗时：" + result.getDurationSeconds() + "秒");
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionResult {
    
    /**
     * 关联的任务ID
     * 
     * 说明：对应 task_record 表的主键 id
     * 用途：将执行结果与数据库任务记录关联
     * 
     * 注意：
     * - 同步执行时，任务ID在执行前就已经创建
     * - 异步执行时，任务ID立即返回给前端，用于查询进度
     */
    private Long taskId;
    
    /**
     * 进程退出码（Exit Code）
     * 
     * 说明：
     * - 0：表示正常退出，脚本执行成功
     * - 非0：表示异常退出，脚本执行失败
     * 
     * 常见退出码：
     * - 0：成功
     * - 1：一般性错误（Python 异常、逻辑错误）
     * - 2：脚本使用错误（参数错误）
     * - 126：权限问题，无法执行
     * - 127：命令找不到（Python 路径错误）
     * - 130：Ctrl+C 中断（用户取消）
     * - 137：进程被 kill -9 强制终止
     * - 143：进程被 kill -15 正常终止
     * 
     * 判断成功失败：
     * success = (exitCode == 0)
     */
    private Integer exitCode;
    
    /**
     * 标准输出（stdout）
     * 
     * 说明：捕获 Python 脚本中所有 print() 的内容
     * 
     * 内容示例：
     * <pre>
     * [INFO] 开始计算特征...
     * [INFO] 日期范围：20250301 - 20250305
     * [INFO] 计算完成，特征数量：1024
     * [SUCCESS] 任务执行成功
     * </pre>
     * 
     * 注意：
     * - 如果内容过大（>1MB），建议只存储摘要，完整日志写入文件
     * - 前端展示时需要处理换行符（\n）
     * - 可能包含 ANSI 颜色码，需要前端支持或后端过滤
     */
    private String stdout;
    
    /**
     * 错误输出（stderr）
     * 
     * 说明：捕获 Python 脚本的错误信息和异常堆栈
     * 
     * 内容示例：
     * <pre>
     * Traceback (most recent call last):
     *   File "feature_manager.py", line 125, in calc_features
     *     result = compute_alpha(data)
     * ValueError: 数据为空，无法计算特征
     * </pre>
     * 
     * 用途：
     * - 调试时查看完整的错误堆栈
     * - 提取关键错误信息存入 error_message
     * - 记录到 task_log 表供后续分析
     * 
     * 注意：
     * - 有些程序会将正常日志也输出到 stderr
     * - 建议使用 ProcessBuilder.redirectErrorStream(true) 合并到 stdout
     */
    private String stderr;
    
    /**
     * 是否执行成功
     * 
     * 说明：根据 exitCode 计算得出
     * 判断逻辑：success = (exitCode != null && exitCode == 0)
     * 
     * 用途：
     * - 快速判断任务成功失败
     * - 更新 task_record 表的 status 字段
     * - 触发后续流程（成功发送通知、失败自动重试）
     * 
     * 注意：
     * - exitCode 为 null 时，表示任务还未结束或异常终止
     * - 建议不要直接设置该字段，而是通过 exitCode 自动计算
     */
    private Boolean success;
    
    /**
     * 任务开始时间
     * 
     * 说明：Python 进程启动的时间
     * 时区：使用服务器本地时区（Asia/Shanghai）
     * 
     * 用途：
     * - 计算任务执行时长
     * - 记录到 task_record.start_time
     * - 监控任务队列等待时间
     */
    private LocalDateTime startTime;
    
    /**
     * 任务结束时间
     * 
     * 说明：Python 进程退出的时间
     * 时区：使用服务器本地时区（Asia/Shanghai）
     * 
     * 用途：
     * - 计算任务执行时长
     * - 记录到 task_record.end_time
     * - 统计任务平均耗时
     */
    private LocalDateTime endTime;
    
    /**
     * 错误信息摘要
     * 
     * 说明：从 stderr 中提取的关键错误信息（简短版）
     * 
     * 提取规则：
     * - Python 异常：提取异常类型和错误描述
     * - 超时：记录 "任务执行超时（3600秒）"
     * - 取消：记录 "任务被用户取消"
     * - 其他：提取 stderr 最后几行
     * 
     * 示例：
     * - "ValueError: 数据为空，无法计算特征"
     * - "ConnectionError: 无法连接到数据库"
     * - "任务执行超时（3600秒）"
     * 
     * 用途：
     * - 存入 task_record.error_message（数据库字段长度有限）
     * - 前端列表页快速展示错误原因
     * - 告警通知中的错误描述
     * 
     * 注意：
     * - 建议限制长度（如200字符），完整错误存 stderr
     * - 不包含完整堆栈，详细信息查看 stderr
     */
    private String errorMessage;
    
    /**
     * Python 进程对象（可选）
     * 
     * 说明：保存正在执行的 Process 对象
     * 用途：
     * - 取消任务时调用 process.destroy() 终止进程
     * - 监控任务时检查进程是否存活
     * 
     * 注意：
     * - 仅在任务执行中有效，执行结束后为 null
     * - 不会序列化到数据库或 JSON 响应
     * - 使用 transient 关键字避免序列化
     */
    private transient Process process;
    
    /**
     * 执行的完整命令（用于日志和调试）
     * 
     * 说明：实际执行的命令行字符串
     * 示例：python C:\Scripts\feature_manager.py calc --start 20250301 --end 20250305
     * 
     * 用途：
     * - 记录到日志，便于排查问题
     * - 调试时手动重现问题
     * - 存入 task_record.command 字段
     */
    private String command;
    
    // ==================== 实用方法 ====================
    
    /**
     * 判断任务是否执行成功
     * 
     * 判断逻辑：exitCode 存在且等于 0
     * 
     * @return true-成功，false-失败或未完成
     */
    public boolean isSuccess() {
        return exitCode != null && exitCode == 0;
    }
    
    /**
     * 判断任务是否执行失败
     * 
     * 判断逻辑：exitCode 存在且不等于 0
     * 
     * @return true-失败，false-成功或未完成
     */
    public boolean isFailure() {
        return exitCode != null && exitCode != 0;
    }
    
    /**
     * 判断任务是否还在执行中
     * 
     * 判断逻辑：
     * - startTime 已设置
     * - endTime 未设置
     * - exitCode 未设置
     * 
     * @return true-执行中，false-已结束或未开始
     */
    public boolean isRunning() {
        return startTime != null && endTime == null && exitCode == null;
    }
    
    /**
     * 计算任务执行时长（秒）
     * 
     * 计算逻辑：
     * - 如果已结束：endTime - startTime
     * - 如果执行中：当前时间 - startTime
     * - 如果未开始：返回 0
     * 
     * @return 执行时长（秒），如果未开始返回 0
     */
    public long getDurationSeconds() {
        if (startTime == null) {
            return 0;
        }
        
        LocalDateTime end = (endTime != null) ? endTime : LocalDateTime.now();
        return Duration.between(startTime, end).getSeconds();
    }
    
    /**
     * 计算任务执行时长（毫秒）
     * 
     * 用途：更精确的时长统计
     * 
     * @return 执行时长（毫秒）
     */
    public long getDurationMillis() {
        if (startTime == null) {
            return 0;
        }
        
        LocalDateTime end = (endTime != null) ? endTime : LocalDateTime.now();
        return Duration.between(startTime, end).toMillis();
    }
    
    /**
     * 获取格式化的执行时长
     * 
     * 格式：
     * - 小于1分钟：X秒
     * - 小于1小时：X分Y秒
     * - 大于1小时：X小时Y分
     * 
     * @return 格式化的时长字符串，如 "5分30秒"、"1小时20分"
     */
    public String getFormattedDuration() {
        long seconds = getDurationSeconds();
        
        if (seconds < 60) {
            return seconds + "秒";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            return minutes + "分" + remainingSeconds + "秒";
        } else {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return hours + "小时" + minutes + "分";
        }
    }
    
    /**
     * 根据执行结果获取任务状态
     * 
     * 状态映射：
     * - exitCode == 0 → SUCCESS
     * - exitCode != 0 → FAILED
     * - exitCode == null && endTime != null → CANCELLED
     * - exitCode == null && endTime == null → RUNNING
     * 
     * @return 任务状态枚举
     */
    public TaskStatusEnum getTaskStatus() {
        if (exitCode == null) {
            if (endTime != null) {
                return TaskStatusEnum.CANCELLED;  // 无退出码但已结束，可能被取消
            } else {
                return TaskStatusEnum.RUNNING;    // 无退出码且未结束，正在执行
            }
        }
        
        return exitCode == 0 ? TaskStatusEnum.SUCCESS : TaskStatusEnum.FAILED;
    }
    
    /**
     * 获取 stdout 的前 N 行（用于预览）
     * 
     * @param lines 行数
     * @return 前 N 行内容，使用 \n 分隔
     */
    public String getStdoutPreview(int lines) {
        if (stdout == null || stdout.isEmpty()) {
            return "";
        }
        
        String[] allLines = stdout.split("\n");
        int count = Math.min(lines, allLines.length);
        
        StringBuilder preview = new StringBuilder();
        for (int i = 0; i < count; i++) {
            preview.append(allLines[i]);
            if (i < count - 1) {
                preview.append("\n");
            }
        }
        
        if (allLines.length > lines) {
            preview.append("\n... (还有 ").append(allLines.length - lines).append(" 行)");
        }
        
        return preview.toString();
    }
    
    /**
     * 从 stderr 中提取错误信息摘要
     * 
     * 提取逻辑：
     * 1. 如果 stderr 为空，返回空字符串
     * 2. 查找 Python 异常信息（最后一个非空行）
     * 3. 限制长度为 400 字符
     * 
     * @return 错误信息摘要
     */
    public String extractErrorMessage() {
        if (stderr == null || stderr.trim().isEmpty()) {
            return "";
        }
        
        // 按行分割，找到最后一个非空行（通常是错误描述）
        String[] lines = stderr.split("\n");
        for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines[i].trim();
            if (!line.isEmpty()) {
                // 限制长度为 400 字符
                return line.length() > 400 ? line.substring(0, 400) + "..." : line;
            }
        }
        
        return stderr.length() > 400 ? stderr.substring(0, 400) + "..." : stderr;
    }
    
    // ==================== 静态工厂方法 ====================
    
    /**
     * 创建一个表示成功的执行结果
     * 
     * @param taskId 任务ID
     * @param stdout 标准输出
     * @return ExecutionResult 对象
     */
    public static ExecutionResult success(Long taskId, String stdout) {
        return ExecutionResult.builder()
                .taskId(taskId)
                .exitCode(0)
                .stdout(stdout)
                .stderr("")
                .success(true)
                .endTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建一个表示失败的执行结果
     * 
     * @param taskId 任务ID
     * @param exitCode 退出码
     * @param stderr 错误输出
     * @return ExecutionResult 对象
     */
    public static ExecutionResult failure(Long taskId, int exitCode, String stderr) {
        ExecutionResult result = ExecutionResult.builder()
                .taskId(taskId)
                .exitCode(exitCode)
                .stdout("")
                .stderr(stderr)
                .success(false)
                .endTime(LocalDateTime.now())
                .build();
        
        result.setErrorMessage(result.extractErrorMessage());
        return result;
    }
    
    /**
     * 创建一个表示取消的执行结果
     * 
     * @param taskId 任务ID
     * @return ExecutionResult 对象
     */
    public static ExecutionResult cancelled(Long taskId) {
        return ExecutionResult.builder()
                .taskId(taskId)
                .exitCode(130)  // SIGINT 的退出码
                .stdout("")
                .stderr("任务被用户取消")
                .success(false)
                .errorMessage("任务被用户取消")
                .endTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建一个表示超时的执行结果
     * 
     * @param taskId 任务ID
     * @param timeoutSeconds 超时时间（秒）
     * @return ExecutionResult 对象
     */
    public static ExecutionResult timeout(Long taskId, int timeoutSeconds) {
        String message = String.format("任务执行超时（%d秒）", timeoutSeconds);
        return ExecutionResult.builder()
                .taskId(taskId)
                .exitCode(143)  // SIGTERM 的退出码
                .stdout("")
                .stderr(message)
                .success(false)
                .errorMessage(message)
                .endTime(LocalDateTime.now())
                .build();
    }
}

