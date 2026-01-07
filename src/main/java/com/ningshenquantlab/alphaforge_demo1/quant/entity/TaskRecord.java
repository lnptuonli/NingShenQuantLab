package com.ningshenquantlab.alphaforge_demo1.quant.entity;

import com.ningshenquantlab.alphaforge_demo1.common.enums.TaskStatusEnum;
import com.ningshenquantlab.alphaforge_demo1.common.enums.TaskTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 任务记录实体
 * 
 * 对应数据库表：task_record
 * 
 * 用途：
 * - 记录所有 Python 脚本执行任务的信息
 * - 追踪任务的生命周期（创建、执行、完成）
 * - 存储任务执行结果和错误信息
 * 
 * 生命周期：
 * 1. 创建任务：status = PENDING
 * 2. 开始执行：status = RUNNING，startTime 设置
 * 3. 执行完成：status = SUCCESS/FAILED，endTime 设置
 * 4. 查询历史：支持按任务类型、状态、时间范围查询
 * 
 * 注意事项：
 * - taskId 作为主键，与 ExecutionResult 中的 taskId 对应
 * - params 存储 JSON 格式的参数（如：{"startDate":"20240101","endDate":"20240201"}）
 * - result 存储执行结果摘要
 * - stdout/stderr 可能很大，建议分离到 TaskLog 表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRecord {
    
    /**
     * 任务ID（主键，自增）
     */
    private Long id;
    
    /**
     * 任务类型
     * 
     * 说明：对应 TaskTypeEnum（FEATURE_CALC, DATA_FETCH, MODEL_TRAIN, BACKTEST）
     * 用途：区分不同业务功能的任务
     * 存储：枚举的 code 值（如 "DATA_FETCH"）
     */
    private String taskType;
    
    /**
     * 任务名称
     * 
     * 说明：任务的可读名称，方便前端展示
     * 示例：
     * - "数据获取：20240101-20240201"
     * - "特征计算：全量更新"
     * - "模型训练：RandomForest v1.0"
     */
    private String taskName;
    
    /**
     * 任务状态
     * 
     * 说明：对应 TaskStatusEnum（PENDING, RUNNING, SUCCESS, FAILED, CANCELLED, TIMEOUT）
     * 用途：追踪任务执行进度
     * 存储：枚举的 code 值（如 "RUNNING"）
     */
    private String status;
    
    /**
     * 任务开始时间
     * 
     * 说明：Python 进程启动的时间
     * 用途：计算任务执行时长
     */
    private LocalDateTime startTime;
    
    /**
     * 任务结束时间
     * 
     * 说明：Python 进程退出的时间
     * 用途：计算任务执行时长
     */
    private LocalDateTime endTime;
    
    /**
     * 任务参数（JSON 格式）
     * 
     * 说明：存储任务的输入参数
     * 示例：
     * - 数据获取：{"startDate":"20240101","endDate":"20240201"}
     * - 特征计算：{"featureType":"alpha","calcMode":"incremental"}
     * - 模型训练：{"modelName":"RandomForest","epochs":100}
     * 
     * 用途：
     * - 记录任务执行条件，便于复现
     * - 支持任务重试时使用相同参数
     */
    private String params;
    
    /**
     * 执行结果摘要
     * 
     * 说明：任务执行的主要结果（简短版）
     * 示例：
     * - "成功获取 1000 条数据"
     * - "计算了 50 个特征，耗时 300 秒"
     * - "模型准确率：0.85"
     * 
     * 用途：前端列表页快速展示结果
     * 注意：详细日志存储在 TaskLog 表
     */
    private String result;
    
    /**
     * 错误信息
     * 
     * 说明：任务失败时的错误描述（简短版）
     * 示例：
     * - "ValueError: 数据为空"
     * - "ConnectionError: 无法连接到数据库"
     * - "任务执行超时（3600秒）"
     * 
     * 用途：前端展示错误原因
     * 注意：完整错误堆栈存储在 TaskLog 表
     */
    private String errorMessage;
    
    /**
     * 进程退出码
     * 
     * 说明：Python 进程的退出码（0=成功，非0=失败）
     * 用途：判断任务是否成功
     */
    private Integer exitCode;
    
    /**
     * 任务执行时长（秒）
     * 
     * 说明：endTime - startTime 的秒数
     * 用途：统计任务性能，监控异常任务
     */
    private Long duration;
    
    /**
     * 任务创建时间
     * 
     * 说明：任务提交到系统的时间
     * 默认值：当前时间戳
     */
    private LocalDateTime createTime;
    
    /**
     * 任务更新时间
     * 
     * 说明：任务状态最后更新的时间
     * 默认值：当前时间戳，状态变更时自动更新
     */
    private LocalDateTime updateTime;
    
    /**
     * 创建人ID（可选）
     * 
     * 说明：提交任务的用户ID
     * 用途：权限控制、任务归属
     */
    private Long createdBy;
    
    // ==================== 实用方法 ====================
    
    /**
     * 判断任务是否已完成（终态）
     */
    public boolean isTerminal() {
        if (status == null) {
            return false;
        }
        TaskStatusEnum statusEnum = TaskStatusEnum.fromCode(status);
        return statusEnum != null && statusEnum.isTerminal();
    }
    
    /**
     * 判断任务是否成功
     */
    public boolean isSuccess() {
        return TaskStatusEnum.SUCCESS.getCode().equals(status);
    }
    
    /**
     * 判断任务是否失败
     */
    public boolean isFailure() {
        if (status == null) {
            return false;
        }
        TaskStatusEnum statusEnum = TaskStatusEnum.fromCode(status);
        return statusEnum != null && statusEnum.isFailure();
    }
    
    /**
     * 获取任务类型枚举
     */
    public TaskTypeEnum getTaskTypeEnum() {
        return TaskTypeEnum.fromCode(taskType);
    }
    
    /**
     * 获取任务状态枚举
     */
    public TaskStatusEnum getTaskStatusEnum() {
        return TaskStatusEnum.fromCode(status);
    }
}
